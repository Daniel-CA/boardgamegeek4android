package com.boardgamegeek.ui.model;

import android.database.Cursor;

import com.boardgamegeek.provider.BggContract.PlayPlayers;
import com.boardgamegeek.util.PresentationUtils;

import java.util.Locale;

public class Player {

	public static final String[] PROJECTION = {
		PlayPlayers._ID,
		PlayPlayers.NAME,
		PlayPlayers.USER_NAME,
		PlayPlayers.SUM_QUANTITY,
		PlayPlayers.SUM_WINS
	};

	private static final int NAME = 1;
	private static final int USER_NAME = 2;
	private static final int SUM_QUANTITY = 3;
	private static final int SUM_WINS = 4;

	private String name;
	private String username;
	private int playCount;
	private int winCount;

	public static Player fromCursor(Cursor cursor) {
		Player location = new Player();
		location.name = cursor.getString(NAME);
		location.username = cursor.getString(USER_NAME);
		location.playCount = cursor.getInt(SUM_QUANTITY);
		location.winCount = cursor.getInt(SUM_WINS);
		return location;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getDescription() {
		return PresentationUtils.describePlayer(name, username);
	}

	public int getPlayCount() {
		return playCount;
	}

	public int getWinCount() {
		return winCount;
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "%1$s (%2$,d/%3$,d)", getDescription(), winCount, playCount);
	}
}
