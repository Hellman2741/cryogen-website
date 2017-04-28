package com.cryo.modules.staff.appeals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.cryo.Website;
import com.cryo.cookies.CookieManager;
import com.cryo.db.impl.PunishmentConnection;
import com.cryo.db.impl.ReportsConnection;
import com.cryo.modules.WebModule;
import com.cryo.modules.account.support.BugReportDAO;
import com.cryo.modules.account.support.punish.AppealDAO;
import com.cryo.modules.account.support.punish.PunishUtils;
import com.cryo.modules.account.support.punish.PunishUtils.ReportType;
import com.cryo.utils.CommentDAO;

import lombok.*;
import spark.Request;
import spark.Response;

/**
 * @author Cody Thompson <eldo.imo.rs@hotmail.com>
 *
 * Created on: April 18, 2017 at 2:09:07 AM
 */
public class StaffAppealModule {
	
	@SuppressWarnings("unchecked")
	public static ArrayList<CommentDAO> getComments(int id) {
		ArrayList<CommentDAO> comments = new ArrayList<>();
		Object[] data = PunishmentConnection.connection().handleRequest("get-comments", id);
		if(data == null)
			return comments;
		return (ArrayList<CommentDAO>) data[0];
	}
	
	public static Properties handleRequest(String action, Request request, Response response, Properties prop, WebModule module) {
		String username = CookieManager.getUsername(request);
		switch(action) {
			case "view-list":
				HashMap<String, Object> model = new HashMap<>();
				boolean archived = Boolean.parseBoolean(request.queryParams("archived"));
				model.put("archive", archived);
				model.put("appeals", new PunishUtils().getAppeals(null, archived));
				model.put("utils", new PunishUtils());
				prop.put("success", true);
				prop.put("html", module.render("./source/modules/staff/appeals/appeal_list.jade", model, request, response));
				break;
			case "submit-com":
				int id = Integer.parseInt(request.queryParams("id"));
				String comment = request.queryParams("comment");
				PunishmentConnection.connection().handleRequest("add-comment", username, id, comment);
				model = new HashMap<>();
				model.put("comments", getComments(id));
				String html = module.render("./source/modules/utils/comments.jade", model, request, response);
				prop.put("success", true);
				prop.put("comments", html);
				break;
			case "click-pin":
				id = Integer.parseInt(request.queryParams("id"));
				PunishUtils.pinReport(id, username, ReportType.APPEAL);
				model = new HashMap<>();
				PunishUtils utils = new PunishUtils();
				val appeals = utils.getAppeals(username, false);
				val preports = utils.getPlayerReports(username, false);
				val breports = utils.getBugReports(username, false);
				int total = appeals.size() + preports.size() + breports.size();
				model.put("total", total);
				model.put("i_appeals", appeals);
				model.put("i_preports", preports);
				model.put("i_breports", breports);
				model.put("appeals", utils.getAppeals(null, false));
				model.put("utils", new PunishUtils());
				html = module.render("./source/modules/staff/overview/immediate.jade", model, request, response);
				prop.put("success", true);
				prop.put("immediate", html);
				html = module.render("./source/modules/staff/appeals/appeal_list.jade", model, request, response);
				prop.put("appeals", html);
				break;
			case "view-appeal":
				id = Integer.parseInt(request.queryParams("id"));
				Object[] data = PunishmentConnection.connection().handleRequest("get-appeal", id);
				if(data == null) {
					prop.put("success", false);
					prop.put("error", "Invalid appeal ID. Please reload the page and contact an Admin if this persists.");
					break;
				}
				AppealDAO appeal = (AppealDAO) data[0];
				model = new HashMap<>();
				model.put("appeal", appeal);
				model.put("comments", getComments(appeal.getId()));
				html = module.render("./source/modules/staff/appeals/view_appeal.jade", model, request, response);
				prop.put("success", true);
				prop.put("html", html);
				break;
		}
		return prop;
	}
	
}