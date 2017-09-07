package com.boardgamegeek.tasks.sync;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.boardgamegeek.R;
import com.boardgamegeek.model.ThingResponse;
import com.boardgamegeek.model.persister.GamePersister;
import com.boardgamegeek.provider.BggContract;
import com.boardgamegeek.tasks.sync.SyncGameTask.CompletedEvent;

import java.util.Locale;

import retrofit2.Call;
import timber.log.Timber;

public class SyncGameTask extends SyncTask<ThingResponse, CompletedEvent> {
	private final int gameId;

	public SyncGameTask(Context context, int gameId) {
		super(context);
		this.gameId = gameId;
	}

	@Override
	@StringRes
	protected int getTypeDescriptionResId() {
		return R.string.title_game;
	}

	@Override
	protected Call<ThingResponse> createCall() {
		return bggService.thing(gameId, 1);
	}

	@Override
	protected boolean isRequestParamsValid() {
		return super.isRequestParamsValid() && gameId != BggContract.INVALID_ID;
	}

	@Override
	protected void persistResponse(ThingResponse thing) {
		int rowCount = new GamePersister(context).save(thing.getGames(), String.format(Locale.getDefault(), "Game %d", gameId));
		Timber.i("Synced game '%s' (%,d rows)", gameId, rowCount);
	}

	@NonNull
	@Override
	protected CompletedEvent createEvent(String errorMessage) {
		return new CompletedEvent(errorMessage, gameId);
	}

	public class CompletedEvent extends SyncTask.CompletedEvent {
		private final int gameId;

		public CompletedEvent(String errorMessage, int gameId) {
			super(errorMessage);
			this.gameId = gameId;
		}

		public int getGameId() {
			return gameId;
		}
	}
}
