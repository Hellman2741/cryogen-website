package com.cryo.entities.accounts;

import com.cryo.Website;
import com.cryo.entities.*;
import com.cryo.entities.accounts.discord.Discord;
import com.cryo.entities.accounts.email.Email;
import com.cryo.entities.accounts.support.RecoveryQuestion;
import com.cryo.entities.forums.UserGroup;
import com.cryo.entities.forums.VisitorMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import static com.cryo.Website.getConnection;

@RequiredArgsConstructor
@Data
public class Account extends MySQLDao {

	@MySQLDefault
	private final int id;
    @MySQLRead
	private String username;
    @MySQLRead
	private String password;
    @MySQLRead
	private String salt;
    @MySQLRead
	private int rights;
    @MySQLRead
	private int donator;
    @MySQLDefault
    @MySQLRead("recovery_questions")
    private String questions;
    @MySQLRead
	private String avatarUrl;
    @MySQLRead
	private String customUserTitle;
	@MySQLDefault
    @MySQLRead
	private int displayGroup;
    @MySQLRead
	private String usergroups;
    @MySQLRead("creation_ip")
    private String creationIP;
	@MySQLDefault
	private final Timestamp added;

	private HashMap<Integer, ArrayList<Object>> recoveries;

	public Account(int id, String username, String password, String salt, int rights, int donator, String questions, String avatarUrl, String customUserTitle, int displayGroup, String usergroups, String creationIP, Timestamp added) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.salt = salt;
		this.rights = rights;
		this.donator = donator;
		this.questions = questions;
		this.avatarUrl = avatarUrl;
		this.customUserTitle = customUserTitle;
		this.displayGroup = displayGroup;
		this.usergroups = usergroups;
		this.creationIP = creationIP;
		this.added = added;
	}

	public String getEmail() {
		Email email = getConnection("cryogen_email").selectClass("linked", "username=?", Email.class, username);
		if(email == null) return "";
		return email.getEmail();
	}

    public void setEmail(String email) {
        if(email.equals("")) {
			getConnection("cryogen_email").delete("linked", "username=?", username);
            return;
        }
        Email dao = getConnection("cryogen_email").selectClass("linked", "username=?", Email.class, username);
        if(dao == null) {
            dao = new Email(-1, username, email, null, null);
			getConnection("cryogen_email").insert("linked", dao.data());
        } else
			getConnection("cryogen_email").set("linked", "email=?", "username=?", email, username);
    }

    public int getPostCount() {
        return getConnection("cryogen_forum").selectCount("posts", "author_id=?", id);
    }

	public String getUserTitle() {
		if(customUserTitle != null) return customUserTitle;
		if (getDisplayGroup() != null && getDisplayGroup().getUserTitle() != null)
			return getDisplayGroup().getUserTitle();
		return null;
	}

	public HashMap<Integer, ArrayList<Object>> getRecoveryQuestions() {
		if(questions == null) return null;
		if(recoveries != null) return recoveries;
		HashMap<String, ArrayList<Object>> recoveries = Website.getGson().fromJson(questions, HashMap.class);
		if(recoveries == null) return null;
		HashMap<Integer, ArrayList<Object>> real = new HashMap<>();
		recoveries.keySet().forEach(key -> real.put(Integer.parseInt(key), recoveries.get(key)));
		this.recoveries = real;
		return real;
	}

	public ArrayList<Object> getQuestion(int index) {
		if(getRecoveryQuestions() == null || !getRecoveryQuestions().containsKey(index) || getRecoveryQuestions().get(index) == null)
			return null;
		return getRecoveryQuestions().get(index);
	}

	public ArrayList<Object> getQuestionById(int id) {
		for(ArrayList<Object> obj : getRecoveryQuestions().values()) {
			if ((int) Math.floor((double) obj.get(0)) == id) return obj;
		}
		return null;
	}

	public PreviousPassList getPreviousPasswords() {
		return getConnection("cryogen_previous").selectClass("passwords", "username=?", PreviousPassList.class, username);
	}

	public String getDisplayName() {
		DisplayName name = getConnection("cryogen_display").selectClass("current_names", "username=?", DisplayName.class, username);
		if (name == null) return username;
		return name.getDisplayName();
	}

	public String getNameColour() {
		if (getDisplayGroup() != null) {
			if (getDisplayGroup().getColour() != null) return getDisplayGroup().getColour();
		}
		for (UserGroup group : getUsergroups()) {
			if (group.getColour() != null) return group.getColour();
		}
		return null;
	}

	public String getImageBefore() {
		if (getDisplayGroup() != null) {
			if (getDisplayGroup().getImageBefore() != null) return getDisplayGroup().getImageBefore();
		}
		for (UserGroup group : getUsergroups()) {
			if (group.getImageBefore() != null) return group.getImageBefore();
		}
		return null;
	}

	public String getImageAfter() {
		if (getDisplayGroup() != null) {
			if (getDisplayGroup().getImageAfter() != null) return getDisplayGroup().getImageAfter();
		}
		for (UserGroup group : getUsergroups()) {
			if (group.getImageAfter() != null) return group.getImageAfter();
		}
		return null;
	}

	public UserGroup getDisplayGroup() {
		return getUserGroup(displayGroup);
	}

	public UserGroup getUserGroup(int id) {
		return getConnection("cryogen_forum").selectClass("usergroups", "id=?", UserGroup.class, id);
	}

	public ArrayList<UserGroup> getUsergroups() {
		ArrayList<UserGroup> groups = new ArrayList<>();
		ArrayList<Integer> ids = Website.getGson().fromJson(usergroups, ArrayList.class);
		if (ids != null) {
			for (int id : ids) {
				UserGroup group = getUserGroup(id);
				if (group == null) continue;
				groups.add(group);
			}
		}
		return groups;
	}

	public User getDiscordUser() {
		Discord discord = getConnection("cryogen_discord").selectClass("linked", "username=?", Discord.class, username);
		if(discord == null) return null;
		return Website.getJDA().retrieveUserById(discord.getDiscordId()).complete();
	}

    public String getUsergroupsJoined(String delimiter) {
        return getUsergroups()
                    .stream()
                    .map(UserGroup::getId)
                    .map(i -> Integer.toString(i))
                    .collect(Collectors.joining(delimiter));
    }

    public ArrayList<VisitorMessage> getVisitorMessages() {
		return getConnection("cryogen_forum").selectList("visitor_messages", "account_id=?", "ORDER BY added DESC", VisitorMessage.class, id);
	}

    public void setStatus(int index, int forumId, int userId, int threadId) {
        long millis = System.currentTimeMillis() + (1000 * 60 * 5);
        AccountStatus status = new AccountStatus(-1, id, index, forumId, userId, threadId, new Timestamp(millis));
		getConnection("cryogen_forum").delete("account_statuses", "account_id=?", id);
		getConnection("cryogen_forum").insert("account_statuses", status.data());
    }

    public AccountStatus getStatus() {
        return getConnection("cryogen_forum").selectClass("account_statuses", "account_id=? && expiry > CURRENT_TIMESTAMP()", AccountStatus.class, id);
    }

    public String getMemberStatus() {
		if(donator == 0) return "Regular Player";
		if(donator == 1) return "Donator";
		if(donator == 2) return "Contributor";
		if(donator == 3) return "VIP";
		return "";
	}
	
}