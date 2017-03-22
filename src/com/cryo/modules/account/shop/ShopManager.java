package com.cryo.modules.account.shop;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.cryo.Website;
import com.cryo.Website.RequestType;
import com.cryo.db.impl.ShopConnection;
import com.google.gson.Gson;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.exceptions.JadeCompilerException;
import lombok.Getter;
import spark.Request;
import spark.Response;

/**
 * @author Cody Thompson <eldo.imo.rs@hotmail.com>
 *
 * Created on: March 14, 2017 at 12:30:18 AM
 */
public class ShopManager {
	
	public static HashMap<Integer, ShopItem> cached;
	
	@SuppressWarnings("unchecked")
	public static void load(Website website) {
		Object[] data = ShopConnection.connection(website).handleRequest("get-items");
		if(data == null) {
			cached = new HashMap<>();
			return;
		}
		cached = (HashMap<Integer, ShopItem>) data[0];
	}
	
	public ShopManager() {
	}
	
	public ShopItem getShopItem(int id) {
		return cached.get(id);
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<Integer, Integer> pushCartUpdate(String username, HashMap<Integer, Integer> items) {
		Object[] data = ShopConnection.connection().handleRequest("get-cart", username);
		HashMap<Integer, Integer> cart = new HashMap<Integer, Integer>();
		if(data != null)
			cart = (HashMap<Integer, Integer>) data[0];
		for(int id : items.keySet()) {
			if(!cart.containsKey(id)) {
				cart.put(id, items.get(id));
				continue;
			}
			int amount = cart.get(id);
			if(amount != items.get(id))
				amount = items.get(id);
			cart.put(id, amount);
		}
		for(int id : items.keySet())
			if(!items.containsKey(id))
				cart.remove(id);
		ShopConnection.connection().handleRequest("set-cart", username, cart);
		return cart;
	}
	
	public static String processRequest(String action, Request request, Response response, RequestType type) {
		Properties prop = new Properties();
		String username = request.session().attribute("cryo-user");
		switch(action) {
			case "chg-quant":
				int id = Integer.parseInt(request.queryParams("id"));
				int quant = Integer.parseInt(request.queryParams("quant"));
				String carts = "";
				if(quant == 1)
					carts = ShopUtils.toString(ShopUtils.increaseQuantity(username, id));
				else
					carts = ShopUtils.toString(ShopUtils.decreaseQuantity(username, id));
				prop.put("cart", carts);
				break;
			case "get-cart":
				String ret = ShopUtils.toString(ShopUtils.getCart(username));
				return ret;
			case "get-checkout-conf":
				String html = "";
				try {
					HashMap<String, Object> notyM = new HashMap<>();
					HashMap<Integer, Integer> cart = ShopUtils.getCart(username);
					notyM.put("shopManager", new ShopManager());
					notyM.put("cart", cart);
					html = Jade4J.render("./source/modules/account/sections/shop/shop_noty.jade", notyM);
					prop.put("html", html);
				} catch (JadeCompilerException | IOException e) {
					e.printStackTrace();
				}
				break;
		}
		return new Gson().toJson(prop);
	}
	
	public static int[] getData(HashMap<Integer, Integer> cart) {
		int price = 0;
		int total = 0;
		for(int id : cart.keySet()) {
			ShopItem item = cached.get(id);
			if(item == null)
				continue;
			int amount = cart.get(id);
			price += (item.getPrice()*amount);
			total += amount;
		}
		return new int[] { price, total };
	}
	
}