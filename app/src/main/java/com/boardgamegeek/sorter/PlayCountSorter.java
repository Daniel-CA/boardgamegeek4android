package com.boardgamegeek.sorter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.boardgamegeek.R;
import com.boardgamegeek.provider.BggContract.Collection;

public abstract class PlayCountSorter extends CollectionSorter {
	public PlayCountSorter(@NonNull Context context) {
		super(context);
	}

	@Override
	protected String getSortColumn() {
		return Collection.NUM_PLAYS;
	}

	@Override
	public String getHeaderText(@NonNull Cursor cursor) {
		return getIntAsString(cursor, Collection.NUM_PLAYS, "0");
	}

	@NonNull
	@Override
	public String getDisplayInfo(@NonNull Cursor cursor) {
		return getHeaderText(cursor) + " " + getContext().getString(R.string.plays);
	}
}
