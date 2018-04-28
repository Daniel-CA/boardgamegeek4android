package com.boardgamegeek.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;

import com.boardgamegeek.R;

/**
 * A simple adapter to use for {@link android.widget.AutoCompleteTextView}.
 */
public class AutoCompleteAdapter extends SimpleCursorAdapter {
	private final Context context;
	private final String columnName;
	private final Uri uri;

	public AutoCompleteAdapter(@NonNull Context context, String columnName, Uri uri) {
		super(context, R.layout.autocomplete_item, null,
			new String[] { BaseColumns._ID, columnName },
			new int[] { 0, R.id.autocomplete_item }, 0);
		this.context = context;
		this.columnName = columnName;
		this.uri = uri;
	}

	@Override
	public int getStringConversionColumn() {
		return 1;
	}

	@Override
	public CharSequence convertToString(@NonNull Cursor cursor) {
		return cursor.getString(1);
	}

	@Nullable
	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		String selection = null;
		String[] selectionArgs = null;
		if (!TextUtils.isEmpty(constraint)) {
			selection = columnName + " LIKE ?";
			selectionArgs = new String[] { constraint + "%" };
		}
		return context.getContentResolver().query(uri, new String[] { BaseColumns._ID, columnName }, selection, selectionArgs, null);
	}
}
