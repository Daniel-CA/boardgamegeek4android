package com.boardgamegeek.sorter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.format.DateUtils;

import com.boardgamegeek.R;
import com.boardgamegeek.provider.BggContract.Games;

public class LastViewedSorter extends CollectionSorter {
	@NonNull private final String never;

	public LastViewedSorter(@NonNull Context context) {
		super(context);
		never = context.getString(R.string.never);
	}

	@StringRes
	@Override
	protected int getDescriptionId() {
		return R.string.collection_sort_last_viewed;
	}

	@StringRes
	@Override
	public int getTypeResource() {
		return R.string.collection_sort_type_last_viewed;
	}

	@Override
	protected String getSortColumn() {
		return Games.LAST_VIEWED;
	}

	@Override
	protected boolean isSortDescending() {
		return true;
	}

	@NonNull
	@Override
	public String getHeaderText(@NonNull Cursor cursor) {
		long time = getLong(cursor, Games.LAST_VIEWED);
		if (time == 0) {
			return never;
		}
		return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString();
	}

	@NonNull
	@Override
	public String getDisplayInfo(@NonNull Cursor cursor) {
		return "";
	}

	@Override
	public long getTimestamp(Cursor cursor) {
		return getLong(cursor, Games.LAST_VIEWED);
	}
}
