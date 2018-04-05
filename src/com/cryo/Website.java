package com.cryo;

import static spark.Spark.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.cryo.cache.CachingManager;
import com.cryo.db.DBConnectionManager;
import com.cryo.db.DBConnectionManager.Connection;
import com.cryo.db.impl.GlobalConnection;
import com.cryo.modules.TestModule;
import com.cryo.modules.account.AccountDAO;
import com.cryo.modules.account.AccountOverviewModule;
import com.cryo.modules.account.AccountUtils;
import com.cryo.modules.account.recovery.RecoveryModule;
import com.cryo.modules.account.register.RegisterModule;
import com.cryo.modules.account.shop.ShopManager;
import com.cryo.modules.account.support.AccountSupportModule;
import com.cryo.modules.highscores.HighscoresModule;
import com.cryo.modules.index.IndexModule;
import com.cryo.modules.live.LiveModule;
import com.cryo.modules.login.LoginModule;
import com.cryo.modules.login.LogoutModule;
import com.cryo.modules.staff.StaffModule;
import com.cryo.modules.staff.announcements.AnnouncementUtils;
import com.cryo.modules.staff.search.SearchManager;
import com.cryo.paypal.PaypalManager;
import com.cryo.server.ServerConnection;
import com.cryo.server.ServerItem;
import com.cryo.tasks.TaskManager;
import com.cryo.tasks.impl.EmailVerifyTask;
import com.cryo.utils.CookieManager;
import com.cryo.utils.Utilities;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.paypal.api.payments.Payment;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.exceptions.JadeCompilerException;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * @author Cody Thompson <eldo.imo.rs@hotmail.com>
 *
 *         Created on: Mar 7, 2017 at 1:45:54 AM
 */
public class Website {

	public static String PATH = "http://cryogen-rsps.com/";

	private static Website INSTANCE;
	
	public static @Getter @Setter int SHUTDOWN_TIME;

	public static volatile boolean LOADED;

	private static @Getter Properties properties;

	private @Getter final DBConnectionManager connectionManager;

	private @Getter final PaypalManager paypalManager;
	
	private @Getter final CachingManager cachingManager;

	private final Timer fastExecutor;

	private final @Getter SearchManager searchManager;

	private static File FAVICON = null;

	public Website() {
		loadProperties();
		FAVICON = new File(properties.getProperty("favico"));
		connectionManager = new DBConnectionManager();
		searchManager = new SearchManager();
		paypalManager = new PaypalManager(this);
		cachingManager = new CachingManager();
		searchManager.load();
		cachingManager.loadCachedItems();
		fastExecutor = new Timer();
		ShopManager.load(this);
		port(Integer.parseInt(properties.getProperty("port")));
		PaypalManager.createAPIContext();
		staticFiles.externalLocation("source/");
		staticFiles.expireTime(0); // ten minutes
		options("/*",
		        (request, response) -> {

		            String accessControlRequestHeaders = request
		                    .headers("Access-Control-Request-Headers");
		            if (accessControlRequestHeaders != null) {
		                response.header("Access-Control-Allow-Headers",
		                        accessControlRequestHeaders);
		            }

		            String accessControlRequestMethod = request
		                    .headers("Access-Control-Request-Method");
		            if (accessControlRequestMethod != null) {
		                response.header("Access-Control-Allow-Methods",
		                        accessControlRequestMethod);
		            }

		            return "OK";
		        });

		before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
		get(SamsungTVModule.PATH, (req, res) -> {
			return new SamsungTVModule(this).decodeRequest(req, res, RequestType.GET);
		});
		get(IndexModule.PATH, (req, res) -> {
			return new IndexModule(this).decodeRequest(req, res, RequestType.GET);
		});
		get(AccountOverviewModule.PATH, (req, res) -> {
			return new AccountOverviewModule(this).decodeRequest(req, res, RequestType.GET);
		});
		post(AccountOverviewModule.PATH, (req, res) -> {
			return new AccountOverviewModule(this).decodeRequest(req, res, RequestType.POST);
		});
		get(AccountSupportModule.PATH, (req, res) -> {
			return new AccountSupportModule(this).decodeRequest(req, res, RequestType.GET);
		});
		post(AccountSupportModule.PATH, (req, res) -> {
			return new AccountSupportModule(this).decodeRequest(req, res, RequestType.POST);
		});
		post("/vote", (req, res) -> {
			return new AccountOverviewModule(this).decodeVotePost(req, res);
		});
		get(LoginModule.PATH, (req, res) -> {
			return new LoginModule(this).decodeRequest(req, res, RequestType.GET);
		});
		post(LoginModule.PATH, (req, res) -> {
			return new LoginModule(this).decodeRequest(req, res, RequestType.POST);
		});
		post(LogoutModule.PATH, (req, res) -> {
			return new LogoutModule(this).decodeRequest(req, res, RequestType.POST);
		});
		get(HighscoresModule.PATH, (req, res) -> {
			return new HighscoresModule(this).decodeRequest(req, res, RequestType.GET);
		});
		post(HighscoresModule.PATH, (req, res) -> {
			return new HighscoresModule(this).decodeRequest(req, res, RequestType.POST);
		});
		get(LogoutModule.PATH, (req, res) -> {
			return new LogoutModule(this).decodeRequest(req, res, RequestType.GET);
		});
		post(StaffModule.PATH, (req, res) -> {
			return new StaffModule(this).decodeRequest(req, res, RequestType.POST);
		});
		get(StaffModule.PATH, (req, res) -> {
			return new StaffModule(this).decodeRequest(req, res, RequestType.GET);
		});
		get("/kill_web", (req, res) -> {
			AccountDAO account = CookieManager.getAccount(req);
			if(account == null || account.getRights() < 2)
				return error("Insufficient permissions.");
			if(SHUTDOWN_TIME > 0)
				return "Website already being shutdown. Please wait.";
			String time = req.queryParams("delay");
			int delay = 30;
			if(time != null)
				delay = Integer.parseInt(time);
			SHUTDOWN_TIME = delay;
			fastExecutor.schedule(new TimerTask() {

				@Override
				public void run() {
					Website.SHUTDOWN_TIME--;
					if(Website.SHUTDOWN_TIME == 0)
						System.exit(0);
				}
				
			}, 0, 1000);
			return "Shutting down website in "+delay+" seconds.";
		});
		get("/paypal_error", (req, res) -> {
			return error("Error getting payment URL");
		});
		get("/process_payment", (req, res) -> {
			return new PaypalManager(this).decodeRequest(req, res, RequestType.GET);
		});
		post("/grab_data", (req, res) -> {
			return grab_data(req, res);
		});
		get("/grab_data", (req, res) -> {
			return grab_data(req, res);
		});
		get("/test", (req, res) -> {
			return new TestModule(this).decodeRequest(req, res, RequestType.GET);
		});
		post("/test", (req, res) -> {
			return new TestModule(this).decodeRequest(req, res, RequestType.POST);
		});
		get("/redirect", (req, res) -> {
			HashMap<String, Object> model = new HashMap<>();
			model.put("redirect", "/logout");
			return Jade4J.render("./source/modules/redirect.jade", model);
		});
		get(LiveModule.PATH, (req, res) -> {
			return new LiveModule(this).decodeRequest(req, res, RequestType.GET);
		});
		get("/recover", (req, res) -> {
			return new RecoveryModule(this, "recover").decodeRequest(req, res, RequestType.GET);
		});
		post("/recover", (req, res) -> {
			return new RecoveryModule(this, "recover").decodeRequest(req, res, RequestType.POST);
		});
		get("/view_status", (req, res) -> {
			return new RecoveryModule(this, "view_status").decodeRequest(req, res, RequestType.GET);
		});
		post("/view_status", (req, res) -> {
			return new RecoveryModule(this, "view_status").decodeRequest(req, res, RequestType.POST);
		});
		get(RegisterModule.PATH, (req, res) -> {
			return new RegisterModule(this).decodeRequest(req, res, RequestType.GET);
		});
		post(RegisterModule.PATH, (req, res) -> {
			return new RegisterModule(this).decodeRequest(req, res, RequestType.POST);
		});
		get("/players", (req, res) -> {
			return Integer.toString(Utilities.getOnlinePlayers());
		});
		get("/online", (req, res) -> {
			SocketAddress addr = new InetSocketAddress("localhost", 43594);
			@Cleanup Socket socket = new Socket();
			try {
				socket.connect(addr, 5_000);
			} catch(IOException e) {
				return "offline";
			}
			return "online";
		});
		get("/create", (req, res) -> {
			return Jade4J.render("./source/modules/staff/punishments/create_punish.jade", new HashMap<String, Object>());
		});
		get("/favicon.ico", (req, response) -> {
                try {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = new BufferedInputStream(new FileInputStream(FAVICON));
                        out = new BufferedOutputStream(response.raw().getOutputStream());
                        response.raw().setContentType(MediaType.ICO.toString());
                        response.status(200);
                        ByteStreams.copy(in, out);
                        out.flush();
                        return "";
                    } finally {
                        in.close();
                    }
                } catch (FileNotFoundException ex) {
                    response.status(404);
                    return ex.getMessage();
                } catch (IOException ex) {
                    response.status(500);
                    return ex.getMessage();
                }
        });
		get("*", Website::render404);
		after("*", (req, res) -> {

		});
		fastExecutor.schedule(new TaskManager(), 0, 1000);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				LOADED = false;
				System.out.println("Stopping spark.");
				Spark.stop();
			}

		});
		System.out.println("Listening on port: "+properties.getProperty("port"));
	}
	
	public static String grab_data(Request req, Response res) {
		Properties prop = new Properties();
		String action = req.queryParams("action");
		HashMap<String, Object> model = new HashMap<>();
		switch(action) {
			case "get-drops-feed":
				Object data = INSTANCE.getCachingManager().get("drops-feed-cache").getCachedData();
				if(data == null || !(data instanceof Properties))
					return error("Unable to retrieve from drops-feed-cache.");
				return new Gson().toJson((Properties) data);
			case "get-online-users":
				if(INSTANCE.getCachingManager().get("online-users-cache") == null)
					return error("Unable to retrieve from online-users-cache.");
				data  = INSTANCE.getCachingManager().get("online-users-cache").getCachedData();
				if(data == null || !(data instanceof String))
					return error("Unable to retrieve from online-users-cache.");
				return (String) data;
			case "get-item-div":
				String item = req.queryParams("item");
				data = INSTANCE.getCachingManager().get("server-item-cache").getCachedData(item);
				if(data == null || !(data instanceof ServerItem)) {
					prop.put("success", false);
					prop.put("error", "Unable to get server item.");
					break;
				}
				ServerItem server_item = (ServerItem) data;
				model.put("item", server_item);
				try {
					prop.put("success", true);
					prop.put("html", Jade4J.render("./source/forums/item_market.jade", model));
				} catch (JadeCompilerException | IOException e) {
					e.printStackTrace();
					prop.put("success", false);
					prop.put("error", "Error retrieving item-div, please contact Cody if this problem persists.");
				}
				break;
		}
		return new Gson().toJson(prop);
	}

	public static String render404(Request request, Response response) {
		response.status(404);
		HashMap<String, Object> model = new HashMap<>();
		model.put("random", getRandomImageLink());
		try {
			return Jade4J.render("./source/modules/404.jade", model);
		} catch (JadeCompilerException | IOException e) {
			e.printStackTrace();
		}
		return error("Error rendering 404 page! Don't worry, we have put the hamsters back on their wheels! Shouldn't be long...");
	}
	
	public static HttpServletResponse sendFile(File file, Response res, MediaType type) {
		res.header("Content-Disposition", "attachment; filename="+file.getName());
		res.type("application/force-download");
		try {
			byte[] bytes = Files.toByteArray(file);
			HttpServletResponse raw = res.raw();
			raw.getOutputStream().write(bytes);
			raw.getOutputStream().flush();
			raw.getOutputStream().close();
			return res.raw();
		} catch(Exception e) { e.printStackTrace(); }
		return null;
	}

	public static String getRandomImageLink() {
		File[] files = new File("./source/images/404/").listFiles();
		File random = files[new Random().nextInt(files.length)];
		return String.format("%simages/404/%s", PATH, random.getName());
	}

	public static String error(String error) {
		HashMap<String, Object> data = new HashMap<>();
		data.put("code", 100);
		data.put("status", error);
		return new Gson().toJson(data);
	}

	public static void loadProperties() {
		File file = new File("props.json");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String json = reader.readLine();
			Gson gson = new Gson();
			properties = gson.fromJson(json, Properties.class);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Website instance() {
		return INSTANCE;
	}

	public static void main(String[] args) {
		INSTANCE = new Website();

	}

	public static enum RequestType {
		POST, GET
	}

}
