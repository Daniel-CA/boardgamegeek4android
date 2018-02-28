package com.boardgamegeek.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;

import com.boardgamegeek.R;
import com.boardgamegeek.auth.Authenticator;
import com.boardgamegeek.io.BggService;
import com.boardgamegeek.provider.BggContract;
import com.boardgamegeek.provider.BggContract.Collection;
import com.boardgamegeek.service.model.CollectionItem;
import com.boardgamegeek.tasks.sync.SyncCollectionByGameTask;
import com.boardgamegeek.ui.CollectionActivity;
import com.boardgamegeek.ui.GameActivity;
import com.boardgamegeek.util.HttpUtils;
import com.boardgamegeek.util.NotificationUtils;
import com.boardgamegeek.util.SelectionBuilder;
import com.boardgamegeek.util.TaskUtils;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class SyncCollectionUpload extends SyncUploadTask {
	private ContentResolver resolver;
	@NonNull private final OkHttpClient okHttpClient;
	@NonNull private final CollectionDeleteTask deleteTask;
	@NonNull private final CollectionAddTask addTask;
	@NonNull private final List<CollectionUploadTask> uploadTasks;
	private int currentGameId;
	private String currentGameName;

	@DebugLog
	public SyncCollectionUpload(Context context, BggService service, @NonNull SyncResult syncResult) {
		super(context, service, syncResult);
		okHttpClient = HttpUtils.getHttpClientWithAuth(context);
		deleteTask = new CollectionDeleteTask(okHttpClient);
		addTask = new CollectionAddTask(okHttpClient);
		uploadTasks = createUploadTasks();
	}

	@NonNull
	private List<CollectionUploadTask> createUploadTasks() {
		List<CollectionUploadTask> tasks = new ArrayList<>();
		tasks.add(new CollectionStatusUploadTask(okHttpClient));
		tasks.add(new CollectionRatingUploadTask(okHttpClient));
		tasks.add(new CollectionCommentUploadTask(okHttpClient));
		tasks.add(new CollectionPrivateInfoUploadTask(okHttpClient));
		tasks.add(new CollectionWishlistCommentUploadTask(okHttpClient));
		tasks.add(new CollectionTradeConditionUploadTask(okHttpClient));
		tasks.add(new CollectionWantPartsUploadTask(okHttpClient));
		tasks.add(new CollectionHasPartsUploadTask(okHttpClient));
		return tasks;
	}

	@DebugLog
	@Override
	public int getSyncType() {
		return SyncService.FLAG_SYNC_COLLECTION_UPLOAD;
	}

	@DebugLog
	@Override
	protected int getNotificationTitleResId() {
		return R.string.sync_notification_title_collection_upload;
	}

	@NonNull
	@DebugLog
	@Override
	protected Intent getNotificationSummaryIntent() {
		return new Intent(context, CollectionActivity.class);
	}

	@Nullable
	@DebugLog
	@Override
	protected Intent getNotificationIntent() {
		if (currentGameId != BggContract.INVALID_ID) {
			return GameActivity.createIntent(currentGameId, currentGameName);
		}
		return super.getNotificationIntent();
	}

	@DebugLog
	@Override
	protected String getNotificationMessageTag() {
		return NotificationUtils.TAG_UPLOAD_COLLECTION;
	}

	@DebugLog
	@Override
	protected String getNotificationErrorTag() {
		return NotificationUtils.TAG_UPLOAD_COLLECTION_ERROR;
	}

	@DebugLog
	@Override
	public void execute() {
		resolver = context.getContentResolver();

		Cursor cursor = null;
		try {
			cursor = fetchDeletedCollectionItems();
			while (cursor != null && cursor.moveToNext()) {
				if (isCancelled()) break;
				if (wasSleepInterrupted(1000)) break;
				processDeletedCollectionItem(cursor);
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			NotificationUtils.cancel(context, NotificationUtils.TAG_SYNC_PROGRESS);
		}

		cursor = null;
		try {
			cursor = fetchNewCollectionItems();
			while (cursor != null && cursor.moveToNext()) {
				if (isCancelled()) break;
				if (wasSleepInterrupted(1000)) break;
				processNewCollectionItem(cursor);
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			NotificationUtils.cancel(context, NotificationUtils.TAG_SYNC_PROGRESS);
		}

		cursor = null;
		try {
			cursor = fetchDirtyCollectionItems();
			while (cursor != null && cursor.moveToNext()) {
				if (isCancelled()) break;
				if (wasSleepInterrupted(1000)) break;
				processDirtyCollectionItem(cursor);
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			NotificationUtils.cancel(context, NotificationUtils.TAG_SYNC_PROGRESS);
		}
	}

	@Nullable
	private Cursor fetchDeletedCollectionItems() {
		return getCollectionItems(isGreaterThanZero(Collection.COLLECTION_DELETE_TIMESTAMP), R.plurals.sync_notification_collection_deleting);
	}

	@Nullable
	private Cursor fetchNewCollectionItems() {
		String selection = "(" + getDirtyColumnSelection(isGreaterThanZero(Collection.COLLECTION_DIRTY_TIMESTAMP)) + ") AND " +
			SelectionBuilder.whereNullOrEmpty(Collection.COLLECTION_ID);
		return getCollectionItems(selection, R.plurals.sync_notification_collection_adding);
	}

	@Nullable
	private Cursor fetchDirtyCollectionItems() {
		String selection = getDirtyColumnSelection("");
		return getCollectionItems(selection, R.plurals.sync_notification_collection_uploading);
	}

	private String getDirtyColumnSelection(String existingSelection) {
		StringBuilder sb = new StringBuilder(existingSelection);
		for (CollectionUploadTask task : uploadTasks) {
			if (sb.length() > 0) sb.append(" OR ");
			sb.append(isGreaterThanZero(task.getTimestampColumn()));
		}
		return sb.toString();
	}

	private static String isGreaterThanZero(String columnName) {
		return columnName + ">0";
	}

	@Nullable
	private Cursor getCollectionItems(String selection, @PluralsRes int messageResId) {
		Cursor cursor = context.getContentResolver().query(Collection.CONTENT_URI,
			CollectionItem.PROJECTION,
			selection,
			null,
			null);
		final int count = cursor != null ? cursor.getCount() : 0;
		String detail = context.getResources().getQuantityString(messageResId, count, count);
		Timber.i(detail);
		if (count > 0) updateProgressNotification(detail);
		return cursor;
	}

	private void processDeletedCollectionItem(Cursor cursor) {
		CollectionItem item = CollectionItem.fromCursor(cursor);
		deleteTask.addCollectionItem(item);
		deleteTask.post();
		if (processResponseForError(deleteTask)) {
			return;
		}
		resolver.delete(Collection.buildUri(item.getInternalId()), null, null);
		notifySuccess(item, item.getCollectionId(), R.string.sync_notification_collection_deleted);
	}

	private void processNewCollectionItem(Cursor cursor) {
		CollectionItem item = CollectionItem.fromCursor(cursor);
		addTask.addCollectionItem(item);
		addTask.post();
		if (processResponseForError(addTask)) {
			return;
		}
		ContentValues contentValues = new ContentValues();
		addTask.appendContentValues(contentValues);
		resolver.update(Collection.buildUri(item.getInternalId()), contentValues, null, null);
		TaskUtils.executeAsyncTask(new SyncCollectionByGameTask(context, item.getGameId()));
		notifySuccess(item, item.getGameId() * -1, R.string.sync_notification_collection_added);
	}

	private void processDirtyCollectionItem(Cursor cursor) {
		CollectionItem item = CollectionItem.fromCursor(cursor);
		if (item.getCollectionId() != BggContract.INVALID_ID) {
			ContentValues contentValues = new ContentValues();
			for (CollectionUploadTask task : uploadTasks) {
				if (processUploadTask(task, item, contentValues)) return;
			}
			if (contentValues.size() > 0) {
				resolver.update(Collection.buildUri(item.getInternalId()), contentValues, null, null);
				notifySuccess(item, item.getCollectionId(), R.string.sync_notification_collection_updated);
			}
		} else {
			Timber.d("Invalid collectionItem ID for internal ID %1$s; game ID %2$s", item.getInternalId(), item.getGameId());
		}
	}

	private boolean processUploadTask(@NonNull CollectionUploadTask task, CollectionItem collectionItem, ContentValues contentValues) {
		task.addCollectionItem(collectionItem);
		if (task.isDirty()) {
			task.post();
			if (processResponseForError(task)) {
				return true;
			}
			task.appendContentValues(contentValues);
		}
		return false;
	}

	private void notifySuccess(@NonNull CollectionItem item, int id, @StringRes int messageResId) {
		syncResult.stats.numUpdates++;
		currentGameId = item.getGameId();
		currentGameName = item.getCollectionName();
		notifyUser(item.getCollectionName(), context.getString(messageResId), id, item.getImageUrl(), item.getThumbnailUrl());
	}

	private boolean processResponseForError(@NonNull CollectionTask response) {
		if (response.hasAuthError()) {
			Timber.w("Auth error; clearing password");
			syncResult.stats.numAuthExceptions++;
			Authenticator.clearPassword(context);
			return true;
		} else if (response.hasError()) {
			syncResult.stats.numIoExceptions++;
			notifyUploadError(response.getErrorMessage());
			return true;
		}
		return false;
	}
}
