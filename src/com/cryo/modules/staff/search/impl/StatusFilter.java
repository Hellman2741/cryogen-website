package com.cryo.modules.staff.search.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.cryo.modules.account.support.punish.PunishDAO;
import com.cryo.modules.staff.search.Filter;

import lombok.*;

/**
 * @author Cody Thompson <eldo.imo.rs@hotmail.com>
 *
 * Created on: May 03, 2017 at 9:04:52 PM
 */
public class StatusFilter extends Filter {

	public StatusFilter() {
		super("status");
	}

	@Override
	public boolean setValue(String mod, String value) {
		value = value.toLowerCase();
		if(!value.equals("declined") && !value.equals("accepted") && !value.equals("pending") && !value.equals("none"))
			return false;
		int status = value.equals("declined") ? 2 : value.equals("accepted") ? 1 : value.equals("none") ? -1 : 0;
		if(mod.equals("appeal") && status == -1)
			return false;
		this.value = status;
		return true;
	}

	@Override
	public String getFilter(String mod) {
		if(mod.equals("punish") || !(value instanceof Integer) || (int) value == -1)
			return null;
		if(mod.equals("recover"))
			return "status=?";
		return "active=?";
	}
	
	@SuppressWarnings("unchecked")
	public List<?> filterList(List<?> list) {
		if(list.size() == 0) return list;
		if(!(list.get(0) instanceof PunishDAO))
			return list;
		List<PunishDAO> punishments = (List<PunishDAO>) list;
		return punishments.stream()
				.filter(this::hasDesiredStatus)
				.collect(Collectors.toList());
	}
	
	public boolean hasDesiredStatus(PunishDAO punish) {
		int intVal = (this.value instanceof Integer) ? (int) this.value : 0;
		if(punish.getAppeal() == null && intVal != 0)
			return intVal == -1;
		return punish.getAppeal().getActive() == intVal;
	}

	@Override
	public boolean appliesTo(String mod) {
		return mod.equals("punish") || mod.equals("appeal") || mod.equals("recover");
	}
	
}
