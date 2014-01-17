package com.yayo.warriors.module.task.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.yayo.warriors.module.props.entity.BackpackEntry;

public class LoopResult {

	private Set<Object> attributes = new HashSet<Object>();

	private Collection<Long> playerIds = new HashSet<Long>();
	
	private List<BackpackEntry> entries = new ArrayList<BackpackEntry>();

	public Collection<Long> getPlayerIds() {
		return this.playerIds;
	}

	public void addPlayerIds(Long...playerIds) {
		for (Long playerId : playerIds) {
			this.playerIds.add(playerId);
		}
	}
	
	public void addPlayerIds(Collection<Long> playerIds) {
		if(playerIds != null && !playerIds.isEmpty()) {
			for (Long playerId : playerIds) {
				this.playerIds.add(playerId);
			}
		}
	}
	
	public List<BackpackEntry> getEntries() {
		return entries;
	}

	public Set<Object> getAttributes() {
		return this.attributes;
	}
	
	public void addBackpackEntries(BackpackEntry...entries) {
		for (BackpackEntry backpackEntry: entries) {
			this.entries.add(backpackEntry);
		}
	}

	public void addBackpackEntries(Collection<BackpackEntry> entries) {
		this.entries.addAll(entries);
	}
	
	public void addAttributes(Object...attributes) {
		for (Object attribute : attributes) {
			this.attributes.add(attribute);
		}
	}
	
	public static LoopResult valueOf() {
		return new LoopResult();
	}
	
}
