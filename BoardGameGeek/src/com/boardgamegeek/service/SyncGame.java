package com.boardgamegeek.service;

import static com.boardgamegeek.util.LogUtils.LOGI;
import static com.boardgamegeek.util.LogUtils.makeLogTag;
import android.content.Context;

import com.boardgamegeek.io.RemoteGameHandler;
import com.boardgamegeek.util.PreferencesUtils;
import com.boardgamegeek.util.url.GameUrlBuilder;

public class SyncGame extends UpdateTask {
	private static final String TAG = makeLogTag(SyncGame.class);
	private int mGameId;

	public SyncGame(int gameId) {
		mGameId = gameId;
	}
	
	@Override
	public void execute(Context context) {
		RemoteGameHandler handler = new RemoteGameHandler(System.currentTimeMillis());
		if (PreferencesUtils.getPolls(context)) {
			handler.setParsePolls();
		}
		String url = new GameUrlBuilder(mGameId).stats().build();
		mExecutor.safelyExecuteGet(url, handler);
		LOGI(TAG, "Synced Game " + mGameId);
	}
}
