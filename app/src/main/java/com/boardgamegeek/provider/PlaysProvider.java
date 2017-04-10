package com.boardgamegeek.provider;

import android.net.Uri;
import android.text.TextUtils;

import com.boardgamegeek.provider.BggContract.Plays;
import com.boardgamegeek.provider.BggDatabase.Tables;
import com.boardgamegeek.util.SelectionBuilder;

public class PlaysProvider extends BasicProvider {

	@Override
	protected SelectionBuilder buildExpandedSelection(Uri uri) {
		SelectionBuilder builder = new SelectionBuilder();

		if (BggContract.FRAGMENT_SIMPLE.equals(uri.getFragment())) {
			builder.table(Tables.PLAYS);
		} else {
			builder.table(Tables.PLAYS_JOIN_GAMES);
		}

		builder
			.mapToTable(Plays._ID, Tables.PLAYS)
			.mapToTable(Plays.PLAY_ID, Tables.PLAYS)
			.mapToTable(Plays.SYNC_TIMESTAMP, Tables.PLAYS)
			.mapAsSum(Plays.SUM_QUANTITY, Plays.QUANTITY)
			.mapAsMax(Plays.MAX_DATE, Plays.DATE);

		String groupBy = uri.getQueryParameter(BggContract.QUERY_KEY_GROUP_BY);
		if (!TextUtils.isEmpty(groupBy)) {
			builder.groupBy(groupBy);
		}
		return builder;
	}

	@Override
	protected String getDefaultSortOrder() {
		return Plays.DEFAULT_SORT;
	}

	@Override
	protected String getPath() {
		return BggContract.PATH_PLAYS;
	}

	@Override
	protected String getTable() {
		return Tables.PLAYS;
	}

	@Override
	protected String getType(Uri uri) {
		return Plays.CONTENT_TYPE;
	}
}
