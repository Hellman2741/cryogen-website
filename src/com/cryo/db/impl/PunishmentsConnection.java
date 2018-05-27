package com.cryo.db.impl;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.cryo.Website;
import com.cryo.db.DBConnectionManager.Connection;
import com.cryo.db.DatabaseConnection;
import com.cryo.db.SQLQuery;
import com.cryo.modules.account.entities.Appeal;
import com.cryo.modules.account.entities.Punishment;
import com.cryo.modules.search.Filter;
import com.cryo.utils.Utilities;
import com.google.gson.Gson;

/**
 * @author Cody Thompson <eldo.imo.rs@hotmail.com>
 *
 * Created on: March 22, 2017 at 2:28:33 AM
 */
public class PunishmentsConnection extends DatabaseConnection {

	public PunishmentsConnection() {
		super("cryogen_punish");
	}
	
	public static PunishmentsConnection connection() {
		return (PunishmentsConnection) Website.instance().getConnectionManager().getConnection(Connection.PUNISH);
	}
	
	private final SQLQuery GET_PUNISHMENTS = (set) -> {
		if(wasNull(set)) return null;
		List<Punishment> punishments = new ArrayList<>();
		while(next(set)) {
			Punishment punish = loadPunishment(set);
			punishments.add(punish);
		}
		return new Object[] { punishments };
	};
	
	private final SQLQuery GET_APPEALS = (set) -> {
		if(wasNull(set)) return null;
		List<Appeal> appeals = new ArrayList<>();
		while(next(set)) {
			Appeal appeal = loadAppeal(set);
			appeals.add(appeal);
		}
		return new Object[] { appeals };
	};
	
	private final SQLQuery GET_PUNISHMENT = (set) -> {
		if(empty(set)) return null;
		return new Object[] { loadPunishment(set) };
	};
	
	private final SQLQuery GET_APPEAL = (set) -> {
		if(empty(set)) return null;
		return new Object[] { loadAppeal(set) };
	};

	@SuppressWarnings("unchecked")
	@Override
	public Object[] handleRequest(Object... data) {
		String opcode = (String) data[0];
		switch(opcode) {
		case "get-total-punish-results":
			String username = (String) data[1];
			boolean archived = (boolean) data[2];
			int active = archived ? 0 : 1;
			return new Object[] { (int) Utilities.roundUp(selectCount("punishments", "username=? AND active=?", username, active), 10) };
		case "search-results":
			Properties queryValues = (Properties) data[1];
			archived = (boolean) data[2];
			HashMap<String, String> params = (HashMap<String, String>) data[3];
			if(params == null) System.out.println("null");
			String query = (String) queryValues.get("query");
			Object[] values = (Object[]) queryValues.get("values");
			query += " AND active="+(archived ? 0 : 1);
			query += " ORDER BY time DESC";
			return new Object[] { selectCount("punishments", query, GET_PUNISHMENTS, values) };
		case "search":
			queryValues = (Properties) data[1];
			int page = (int) data[2];
			archived = (boolean) data[3];
			params = (HashMap<String, String>) data[4];
			if(params == null) System.out.println("null");
			query = (String) queryValues.get("query");
			values = (Object[]) queryValues.get("values");
			if(page == 0)
				page = 1;
			int offset = (page - 1) * 10;
			query += " AND active="+(archived ? 0 : 1);
			query += " ORDER BY time DESC";
			query += " LIMIT "+ offset + ",10";
			return new Object[] { select("punishments", query, GET_PUNISHMENTS, values) };
		case "search-appeal":
			case "search-punish":
				boolean isAppeal = opcode.contains("appeal");
				queryValues = (Properties) data[1];
				page = (int) data[2];
				query = (String) queryValues.get("query");
				values = (Object[]) queryValues.get("values");
				if(page == 0) page = 1;
				offset = (page - 1) * 10;
				query += " LIMIT "+offset+",10";
				data = select(isAppeal ? "appeals" : "punishments", query, isAppeal ? GET_APPEALS : GET_PUNISHMENTS, values);
				return data == null ? null : new Object[] { isAppeal ? (ArrayList<Appeal>) data[0] : (ArrayList<Punishment>) data[0] };
			case "search-results-appeal":
			case "search-results-punish":
				isAppeal = opcode.contains("appeal");
				queryValues = (Properties) data[1];
				query = (String) queryValues.get("query");
				values = (Object[]) queryValues.get("values");
				int total = selectCount(isAppeal ? "appeals" : "punishments", query, values);
				return new Object[] { (int) Utilities.roundUp(total, 10) };
			case "get-punishments":
				username = (String) data[1];
				archived = (boolean) data[2];
				page = (int) data[3];
				if(page == 0) page = 1;
				offset = (page - 1) * 10;
				StringBuilder builder = new StringBuilder();
				if(username != null)
					builder.append("username=? AND ");
				if(archived)
					builder.append(" active=0 OR expiry < NOW()");
				else
					builder.append(" active!=0 AND (expiry IS NULL OR expiry > NOW())");
				builder.append(" ORDER BY date DESC LIMIT "+offset+",10");
				if(username != null)
					data = select("punishments", builder.toString(), GET_PUNISHMENTS, username);
				else
					data = select("punishments", builder.toString(), GET_PUNISHMENTS);
				return new Object[] { data == null ? new ArrayList<Punishment>() : (ArrayList<Punishment>) data[0] };
			case "get-total-results":
				String table = (String) data[1];
				isAppeal = table.contains("appeals");
				boolean archive = table.contains("-a");
				if(table.contains("-a"))
					table = table.replaceAll("-a", "");
				query = "";
				if(archive) {
					query+= "active!=0";
					if(!isAppeal)
						query+= " OR expiry > NOW()";
				} else {
					query += "active=0";
					if(!isAppeal)
						query += " AND (expiry IS NULL OR expiry < NOW())";
				}
				return new Object[] { selectCount(table, query) };
			case "get-punishment-from-appeal":
				int appealId = (int) data[1];
				data = select("punishments", "appeal_id=?", GET_PUNISHMENT, appealId);
				if(data == null) return null;
				return new Object[] { (Punishment) data[0] };
			case "get-punishment":
				int id = (int) data[1];
				if(id == 0)
					return null;
				data = select("punishments", "id=?", GET_PUNISHMENT, id);
				if(data == null) return null;
				return new Object[] { (Punishment) data[0] };
			case "create-punishment":
				Punishment punish = (Punishment) data[1];
				try {
					insert("punishments", punish.data());
					return new Object[] { };
				} catch(Exception e) {
					return null;
				}
			case "extend-punishment":
				Timestamp expiry = (Timestamp) data[1];
				id = (int) data[2];
				data = new Object[expiry == null ? 1 : 2];
				data[0] = expiry == null ? id : expiry;
				if(expiry != null)
					data[1] = id;
				set("punishments", "expiry="+(expiry == null ? "NULL" : "?"), "id=?", data);
				break;
			case "create-appeal":
				Appeal appeal = (Appeal) data[1];
				int appeal_id = insert("appeals", appeal.data());
				set("punishments", "appeal_id=?", "id=?", appeal_id, appeal.getPunishId());
				break;
			case "get-appeals":
				archived = (boolean) data[1];
				page = (int) data[2];
				if(page == 0) page = 1;
				offset = (page - 1) * 10;
				ArrayList<Appeal> appeals = new ArrayList<>();
				data = select("appeals", (archived ? "active!=?" : "active=?")+" ORDER BY time DESC LIMIT "+offset+",10", GET_APPEALS, 0);
				return new Object[] { data == null ? appeals : (ArrayList<Appeal>) data[0] };
			case "get-appeal":
				id = (int) data[1];
				if(id == 0)
					return null;
				data = select("appeals", "id=?", GET_APPEAL, id);
				return data == null ? null : new Object[] { (Appeal) data[0] };
			case "close-appeal":
				id = (int) data[1];
				int status = (int) data[2];
				username = (String) data[3];
				String reason = data.length == 5 ? (String) data[4] : "";
				if(status == 2 && reason.equals(""))
					return null;
				String action = "Appeal "+(status == 1 ? "accepted" : "declined")+" by $for-name="+username+"$end";
				set("appeals", "active=?, reason=?, action=?, answered=?", "id=?", status, reason, action, "DEFAULT", id);
				if(status == 1) //accept, need to set punishment to inactive
					set("punishments", "active=0", "appeal_id=?", id);
				break;
		}
		return null;
	}
	
	public static Appeal getAppeal(int id) {
		Object[] data = connection().handleRequest("get-appeal", id);
		if(data == null) return null;
		return (Appeal) data[0];
	}
	
	public Punishment loadPunishment(ResultSet set) {
		int id = getInt(set, "id");
		int type = getInt(set, "type");
		String username = getString(set, "username");
		Timestamp date = getTimestamp(set, "date");
		Timestamp expiry = getTimestamp(set, "expiry");
		String punisher = getString(set, "punisher");
		String reason = getString(set, "reason");
		boolean active = getInt(set, "active") == 1;
		int appealId = getInt(set, "appeal_id");
		Timestamp archived = getTimestamp(set, "archived");
		String archiver = getString(set, "archiver");
		Punishment punish = new Punishment(id, username, type, date, expiry, punisher, reason, active, appealId, archived, archiver);
		Appeal appeal = getAppeal(appealId);
		if(appeal != null)
			punish.setAppeal(appeal);
		return punish;
	}
	
	@SuppressWarnings("unchecked")
	public Appeal loadAppeal(ResultSet set) {
		int id = getInt(set, "id");
		int punishId = getInt(set, "punish_id");
		String username = getString(set, "username");
		String title = getString(set, "title");
		String message = getString(set, "message");
		String reason = getString(set, "reason");
		String lastAction = getString(set, "action");
		int activei = getInt(set, "active");
		Timestamp time = getTimestamp(set, "time");
		Appeal appeal = new Appeal(id, username, title, message, lastAction, activei, punishId, time);
		String read = getString(set, "read");
		ArrayList<String> list = read.equals("") ? new ArrayList<String>() : (ArrayList<String>) new Gson().fromJson(read, ArrayList.class);
		if(!reason.equals(""))
			appeal.setReason(reason);
		appeal.setUsersRead(list);
		return appeal;
	}
	
}
