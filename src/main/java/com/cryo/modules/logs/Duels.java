package com.cryo.modules.logs;

import com.cryo.Website;
import com.cryo.entities.accounts.Account;
import com.cryo.entities.annotations.Endpoint;
import com.cryo.entities.annotations.EndpointSubscriber;
import com.cryo.entities.logs.Death;
import com.cryo.entities.logs.Duel;
import com.cryo.managers.ListManager;
import com.cryo.modules.account.AccountUtils;
import com.cryo.utils.Utilities;
import org.apache.commons.lang3.math.NumberUtils;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.cryo.Website.getConnection;
import static com.cryo.utils.Utilities.*;

@EndpointSubscriber
public class Duels {

    @Endpoint(method = "POST", endpoint = "/logs/duel/load")
    public static String renderDeathLogs(Request request, Response response) {
        Account account = AccountUtils.getAccount(request);
        if(account == null) return com.cryo.modules.account.Login.renderLoginPage("/logs/duel", request, response);
        if(account.getRights() < 2) return Utilities.redirect("/", "Invalid permissions", null, null, request, response);
        HashMap<String, Object> model = new HashMap<>();
        model.put("refreshable", true);
        model.put("sortable", true);
        model.put("filterable", true);
        model.put("title", "Duel Logs");
        model.put("module", "/logs/duel");
        model.put("moduleId", "duel");
        model.put("info", new ArrayList<String>() {{
            add("The following page lists all in-game duels by players on Cryogen.");
            add("You can filter this list using the buttons on the right.");
        }});
        return renderPage("utils/list/list-page", model, request, response);
    }

    @SuppressWarnings("unchecked")
    @Endpoint(method = "POST", endpoint = "/logs/duel/table")
    public static String renderTable(Request request, Response response) {
        Account account = AccountUtils.getAccount(request);
        if(account == null) return error("Session has expired. Please refresh the page and try again.");
        if(account.getRights() < 2) return Utilities.redirect("/", "Invalid permissions", null, null, request, response);
        HashMap<String, Object> model = new HashMap<>();
        model.put("moduleId", "duel");
        ArrayList<ArrayList<Object>> sortValues = new ArrayList<>();
        if(request.queryParams().contains("sortValues"))
            sortValues = Website.getGson().fromJson(request.queryParams("sortValues"), ArrayList.class);
        ArrayList<ArrayList<Object>> filterValues = new ArrayList<>();
        if(request.queryParams().contains("filterValues"))
            filterValues = Website.getGson().fromJson(request.queryParams("filterValues"), ArrayList.class);
        if(!request.queryParams().contains("page") || !NumberUtils.isDigits(request.queryParams("page")))
            return error("Unable to parse page number. Please refresh the page and try again.");
        int page = Integer.parseInt(request.queryParams("page"));

        ArrayList<Object> values = new ArrayList<>();
        String query = "";

        Object[] condition = ListManager.getCondition(filterValues, Death.class, false);
        if(condition.length == 2 && condition[0] != null) {
            query += (String) condition[0];
            values.addAll((ArrayList<Object>) condition[1]);
        }
        int total = getConnection("cryogen_logs").selectCount("duel", query, values.toArray());
        String order = ListManager.getOrder(model, sortValues, Duel.class, page, total, false);
        List<Duel> duels = getConnection("cryogen_logs").selectList("duel", query, order, Duel.class, values.toArray());
        if(duels == null)
            return error("Error loading duels. Please try again.");
        ListManager.buildTable(model, "logs", duels, Duel.class, account, sortValues, filterValues, false);
        return renderList(model, request, response);
    }

    @Endpoint(method = "POST", endpoint = "/logs/duel/view")
    public static String viewTrade(Request request, Response response) {
        Account account = AccountUtils.getAccount(request);
        if(account == null) return error("Session has expired. Please refresh the page and try again.");
        if(account.getRights() < 2) return Utilities.redirect("/", "Invalid permissions", null, null, request, response);
        if(!request.queryParams().contains("id") || !NumberUtils.isDigits(request.queryParams("id")))
            return error("Unable to parse id. Please refresh the page and try again.");
        int id = Integer.parseInt(request.queryParams("id"));
        Duel duel = getConnection("cryogen_logs").selectClass("duel", "id=?", Duel.class, id);
        if(duel == null)
            return error("Unable to find trade. Please refresh the page and try again.");
        HashMap<String, Object> model = new HashMap<>();
        model.put("duel", duel);
        return renderPage("logs/views/duel", model, request, response);
    }
}
