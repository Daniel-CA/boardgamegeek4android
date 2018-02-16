package com.boardgamegeek.sorter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.boardgamegeek.R;
import com.boardgamegeek.provider.BggContract.Collection;

public abstract class SuggestedAgeSorter extends CollectionSorter {
	private static final String DEFAULT_VALUE = "?";

	public SuggestedAgeSorter(@NonNull Context context) {
		super(context);
	}

	@StringRes
	@Override
	protected int getDescriptionId() {
		return R.string.collection_sort_suggested_age;
	}

	@Override
	protected String getSortColumn() {
		return Collection.MINIMUM_AGE;
	}

	@Override
	public String getHeaderText(@NonNull Cursor cursor) {
		return getIntAsString(cursor, Collection.MINIMUM_AGE, DEFAULT_VALUE, true);
	}

	@NonNull
	@Override
	public String getDisplayInfo(@NonNull Cursor cursor) {
		String info = getHeaderText(cursor);
		if (!DEFAULT_VALUE.equals(info)) {
			info += "+";
		}
		return getContext().getString(R.string.ages) + " " + info;
	}
}
