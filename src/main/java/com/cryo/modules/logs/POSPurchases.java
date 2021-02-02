package com.cryo.modules.logs;

import com.cryo.Website;
import com.cryo.entities.accounts.Account;
import com.cryo.entities.annotations.Endpoint;
import com.cryo.entities.annotations.EndpointSubscriber;
import com.cryo.entities.logs.POSPurchase;
import com.cryo.entities.logs.ShopPurchase;
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
public class POSPurchases {

    @Endpoint(method = "POST", endpoint = "/logs/pos/load")
    public static String renderShopLogs(Request request, Response response) {
        Account account = AccountUtils.getAccount(request);
        if(account == null) return com.cryo.modules.account.Login.renderLoginPage("/logs/pos", request, response);
        if(account.getRights() < 2) return Utilities.redirect("/", "Invalid permissions", null, null, request, response);
        HashMap<String, Object> model = new HashMap<>();
        model.put("refreshable", true);
        model.put("sortable", true);
        model.put("filterable", true);
        model.put("title", "POS Logs");
        model.put("module", "/logs/pos");
        model.put("moduleId", "pos");
        model.put("info", new ArrayList<String>() {{
            add("The following page lists all in-game pos purchases from shops by players on Cryogen.");
            add("You can filter this list using the buttons on the right.");
        }});
        return renderPage("utils/list/list-page", model, request, response);
    }

    @SuppressWarnings("unchecked")
    @Endpoint(method = "POST", endpoint = "/logs/pos/table")
    public static String renderTable(Request request, Response response) {
        Account account = AccountUtils.getAccount(request);
        if(account == null) return error("Session has expired. Please refresh the page and try again.");
        if(account.getRights() < 2) return Utilities.redirect("/", "Invalid permissions", null, null, request, response);
        HashMap<String, Object> model = new HashMap<>();
        model.put("moduleId", "pos");
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

        Object[] condition = ListManager.getCondition(filterValues, ShopPurchase.class, false);
        if(condition.length == 2 && condition[0] != null) {
            query += (String) condition[0];
            values.addAll((ArrayList<Object>) condition[1]);
        }
        int total = getConnection("cryogen_logs").selectCount("pos", query, values.toArray());
        String order = ListManager.getOrder(model, sortValues, POSPurchase.class, page, total, false);
        List<POSPurchase> purchases = getConnection("cryogen_logs").selectList("pos", query, order, POSPurchase.class, values.toArray());
        if(purchases == null)
            return error("Error loading pos logs. Please try again.");
        ListManager.buildTable(model, "logs", purchases, POSPurchase.class, account, sortValues, filterValues, false);
        return renderList(model, request, response);
    }
}
