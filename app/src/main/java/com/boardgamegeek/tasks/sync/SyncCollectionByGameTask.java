package com.boardgamegeek.tasks.sync;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.boardgamegeek.R;
import com.boardgamegeek.auth.AccountUtils;
import com.boardgamegeek.db.CollectionDao;
import com.boardgamegeek.io.Adapter;
import com.boardgamegeek.io.BggService;
import com.boardgamegeek.io.model.CollectionItem;
import com.boardgamegeek.io.model.CollectionResponse;
import com.boardgamegeek.mappers.CollectionItemMapper;
import com.boardgamegeek.provider.BggContract;
import com.boardgamegeek.tasks.sync.SyncCollectionByGameTask.CompletedEvent;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import timber.log.Timber;

public class SyncCollectionByGameTask extends SyncTask<CollectionResponse, CompletedEvent> {
	private final int gameId;
	private final String username;
	private final CollectionDao dao;
	private final List<Integer> results = new ArrayList<>();
	private long timestamp;

	public SyncCollectionByGameTask(Context context, int gameId) {
		super(context);
		this.gameId = gameId;
		username = AccountUtils.getUsername(context);
		dao = new CollectionDao(context);
	}

	@Override
	@StringRes
	protected int getTypeDescriptionResId() {
		return R.string.title_collection;
	}

	@Override
	protected BggService createService() {
		return Adapter.createForXmlWithAuth(context);
	}

	@Override
	protected Call<CollectionResponse> createCall() {
		timestamp = System.currentTimeMillis();
		ArrayMap<String, String> options = new ArrayMap<>();
		options.put(BggService.COLLECTION_QUERY_KEY_SHOW_PRIVATE, "1");
		options.put(BggService.COLLECTION_QUERY_KEY_STATS, "1");
		options.put(BggService.COLLECTION_QUERY_KEY_ID, String.valueOf(gameId));
		return bggService.collection(username, options);
	}

	@Override
	protected boolean isRequestParamsValid() {
		return super.isRequestParamsValid() &&
			gameId != BggContract.INVALID_ID &&
			!TextUtils.isEmpty(username);
	}

	@Override
	protected void persistResponse(CollectionResponse body) {
		results.clear();
		if (body != null && body.items != null) {
			CollectionItemMapper mapper = new CollectionItemMapper();
			for (CollectionItem item : body.items) {
				int collectionId = dao.saveItem(mapper.map(item), timestamp, true, true, false);
				results.add(collectionId);
			}
			Timber.i("Synced %,d collection item(s) for game '%s'", body.items.size(), gameId);
		} else {
			Timber.i("No collection items for game '%s'", gameId);
		}
	}

	@Override
	protected void finishSync() {
		int deleteCount = dao.delete(gameId, results);
		Timber.i("Removed %,d collection item(s) for game '%s'", deleteCount, gameId);
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
