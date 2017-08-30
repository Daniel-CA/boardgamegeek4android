package com.boardgamegeek.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.boardgamegeek.R;
import com.boardgamegeek.events.LocationSelectedEvent;
import com.boardgamegeek.events.LocationSortChangedEvent;
import com.boardgamegeek.events.LocationsCountChangedEvent;
import com.boardgamegeek.sorter.LocationsSorterFactory;
import com.boardgamegeek.util.ToolbarUtils;
import com.boardgamegeek.util.UIUtils;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import hugo.weaving.DebugLog;

public class LocationsActivity extends SimpleSinglePaneActivity {
	private int locationCount = -1;
	private int sortType = LocationsSorterFactory.TYPE_DEFAULT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			Answers.getInstance().logContentView(new ContentViewEvent().putContentType("Locations"));
		}
	}

	@DebugLog
	@Override
	protected Fragment onCreatePane(Intent intent) {
		return new LocationsFragment();
	}

	@DebugLog
	@Override
	protected int getOptionsMenuId() {
		return R.menu.locations;
	}

	@DebugLog
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_sort).setVisible(true);
		if (sortType == LocationsSorterFactory.TYPE_QUANTITY) {
			UIUtils.checkMenuItem(menu, R.id.menu_sort_quantity);
		} else {
			UIUtils.checkMenuItem(menu, R.id.menu_sort_name);
		}
		ToolbarUtils.setActionBarText(menu, R.id.menu_list_count, locationCount <= 0 ? "" : String.format("%,d", locationCount));
		return super.onPrepareOptionsMenu(menu);
	}

	@DebugLog
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_sort_name:
				EventBus.getDefault().postSticky(new LocationSortChangedEvent(LocationsSorterFactory.TYPE_NAME));
				return true;
			case R.id.menu_sort_quantity:
				EventBus.getDefault().postSticky(new LocationSortChangedEvent(LocationsSorterFactory.TYPE_QUANTITY));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("unused")
	@DebugLog
	@Subscribe(sticky = true)
	public void onEvent(LocationsCountChangedEvent event) {
		locationCount = event.getCount();
		supportInvalidateOptionsMenu();
	}

	@SuppressWarnings("unused")
	@DebugLog
	@Subscribe
	public void onEvent(LocationSelectedEvent event) {
		LocationActivity.start(this, event.getLocationName());
	}

	@SuppressWarnings("unused")
	@DebugLog
	@Subscribe(sticky = true)
	public void onEvent(LocationSortChangedEvent event) {
		sortType = event.getSortType();
	}
}
