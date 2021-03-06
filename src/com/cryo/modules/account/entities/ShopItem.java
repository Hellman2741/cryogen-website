package com.cryo.modules.account.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Cody Thompson <eldo.imo.rs@hotmail.com>
 *
 * Created on: March 14, 2017 at 12:30:51 AM
 */
@RequiredArgsConstructor
public class ShopItem {
	
	private final @Getter int id;
	
	private final @Getter String name, imageName;
	
	private final @Getter int price;
	
	private final @Getter String type, description;
	
	public String getImageLink() {
		return "/images/shop/"+imageName;
	}
	
	@Override
	public boolean equals(Object object) {
		if(!(object instanceof ShopItem))
			return false;
		ShopItem item = (ShopItem) object;
		return id == item.id;
	}
	
}
