package com.boardgamegeek.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SyncResult;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.format.DateUtils;

import com.boardgamegeek.R;
import com.boardgamegeek.auth.Authenticator;
import com.boardgamegeek.io.BggService;
import com.boardgamegeek.io.CollectionRequest;
import com.boardgamegeek.io.CollectionResponse;
import com.boardgamegeek.model.persister.CollectionPersister;

import java.util.Date;

import timber.log.Timber;

/**
 * Syncs the user's collection modified since the date stored in the sync service.
 */
public class SyncCollectionModifiedSince extends SyncTask {
	public SyncCollectionModifiedSince(Context context, BggService service) {
		super(context, service);
	}

	@Override
	public int getSyncType() {
		return SyncService.FLAG_SYNC_COLLECTION_DOWNLOAD;
	}

	@Override
	public void execute(@NonNull Account account, @NonNull SyncResult syncResult) {
		AccountManager accountManager = AccountManager.get(context);

		try {
			if (isCancelled()) return;

			CollectionPersister persister = new CollectionPersister.Builder(context)
				.includeStats()
				.includePrivateInfo()
				.validStatusesOnly()
				.build();

			long date = Authenticator.getLong(accountManager, account, SyncService.TIMESTAMP_COLLECTION_PARTIAL);
			String modifiedSince = BggService.COLLECTION_QUERY_DATE_TIME_FORMAT.format(new Date(date));

			updateProgressNotification(context.getString(R.string.sync_notification_collection_items_since,
				DateUtils.formatDateTime(context, date, DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
			ArrayMap<String, String> options = new ArrayMap<>();
			options.put(BggService.COLLECTION_QUERY_KEY_STATS, "1");
			options.put(BggService.COLLECTION_QUERY_KEY_SHOW_PRIVATE, "1");
			options.put(BggService.COLLECTION_QUERY_KEY_MODIFIED_SINCE, modifiedSince);

			CollectionResponse response = new CollectionRequest(service, account.name, options).execute();
			if (response.hasError()) {
				showError(response.getError());
				return;
			} else if (response.getNumberOfItems() > 0) {
				int count = persister.save(response.getItems()).getRecordCount();
				syncResult.stats.numUpdates += response.getNumberOfItems();
				Timber.i("...saved %,d records for %,d collection items", count, response.getNumberOfItems());
			} else {
				Timber.i("...no new collection modifications");
			}

			if (isCancelled()) return;
			if (wasSleepInterrupted(2000)) return;

			updateProgressNotification(context.getString(R.string.sync_notification_collection_accessories_since,
				DateUtils.formatDateTime(context, date, DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME)));
			options.put(BggService.COLLECTION_QUERY_KEY_SUBTYPE, BggService.THING_SUBTYPE_BOARDGAME_ACCESSORY);
			response = new CollectionRequest(service, account.name, options).execute();
			if (response.hasError()) {
				showError(response.getError());
				syncResult.stats.numIoExceptions++;
				return;
			} else if (response.getNumberOfItems() > 0) {
				int count = persister.save(response.getItems()).getRecordCount();
				syncResult.stats.numUpdates += response.getNumberOfItems();
				Timber.i("...saved %,d records for %,d collection accessories", count, response.getNumberOfItems());
			} else {
				Timber.i("...no new collection modifications");
			}

			Authenticator.putLong(context, SyncService.TIMESTAMP_COLLECTION_PARTIAL, persister.getInitialTimestamp());
		} finally {
			Timber.i("...complete!");
		}
	}

	@Override
	public int getNotificationSummaryMessageId() {
		return R.string.sync_notification_collection_partial;
	}
}
