package com.boardgamegeek.ui;

import android.content.Intent;
import android.os.Bundle;

import com.boardgamegeek.auth.Authenticator;
import com.boardgamegeek.util.PreferencesUtils;

import hugo.weaving.DebugLog;

public class HomeActivity extends TopLevelActivity {
	@DebugLog
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(this, HotnessActivity.class);
		if (Authenticator.isSignedIn(this)) {
			if (Authenticator.isOldAuth(this)) {
				Authenticator.signOut(this);
			} else {
				if (PreferencesUtils.isCollectionSetToSync(this)) {
					intent = new Intent(this, CollectionActivity.class);
				} else if (PreferencesUtils.getSyncPlays(this)) {
					intent = new Intent(this, PlaysSummaryActivity.class);
				} else if (PreferencesUtils.getSyncBuddies(this)) {
					intent = new Intent(this, BuddiesActivity.class);
				}
			}
		}
		startActivity(intent);
		finish();
	}
}