package com.boardgamegeek.model;

import android.support.annotation.NonNull;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

public class ForumListResponse {
	@Attribute
	private String type;

	@Attribute
	private int id;

	@ElementList(inline = true)
	private List<Forum> forums;

	@NonNull
	public List<Forum> getForums() {
		if (forums == null) {
			return new ArrayList<>();
		}
		return forums;
	}
}
