package com.boardgamegeek.pref;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.preference.Preference;
import android.util.AttributeSet;

import com.boardgamegeek.BuildConfig;

public class ChangeLogPreference extends Preference {
	public ChangeLogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		String changeLogUrl = String.format("https://github.com/ccomeaux/boardgamegeek4android/blob/%s/CHANGELOG.md", BuildConfig.GIT_BRANCH);
		final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(changeLogUrl));
		setIntent(intent);
	}
}
