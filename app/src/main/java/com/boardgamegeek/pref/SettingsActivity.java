package com.boardgamegeek.pref;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.boardgamegeek.R;
import com.boardgamegeek.service.SyncService;
import com.boardgamegeek.ui.DrawerActivity;
import com.boardgamegeek.util.PreferencesUtils;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.util.Set;

import butterknife.BindArray;
import butterknife.ButterKnife;

public class SettingsActivity extends DrawerActivity {
	private static final String TAG_SINGLE_PANE = "single_pane";
	private static final String KEY_SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";

	private static final String ACTION_PREFIX = "com.boardgamegeek.prefs.";
	private static final String ACTION_LOG = ACTION_PREFIX + "LOG";
	private static final String ACTION_SYNC = ACTION_PREFIX + "SYNC";
	private static final String ACTION_ADVANCED = ACTION_PREFIX + "ADVANCED";
	private static final String ACTION_ABOUT = ACTION_PREFIX + "ABOUT";
	private static final String ACTION_AUTHORS = ACTION_PREFIX + "AUTHORS";
	private static final ArrayMap<String, Integer> FRAGMENT_MAP = buildFragmentMap();

	private static ArrayMap<String, Integer> buildFragmentMap() {
		ArrayMap<String, Integer> map = new ArrayMap<>();
		map.put(ACTION_LOG, R.xml.preference_log);
		map.put(ACTION_SYNC, R.xml.preference_sync);
		map.put(ACTION_ADVANCED, R.xml.preference_advanced);
		map.put(ACTION_ABOUT, R.xml.preference_about);
		map.put(ACTION_AUTHORS, R.xml.preference_authors);
		return map;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.root_container, new PrefFragment(), TAG_SINGLE_PANE).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (!getFragmentManager().popBackStackImmediate()) {
			super.onBackPressed();
		}
	}

	void replaceFragment(String key) {
		Bundle args = new Bundle();
		args.putString(KEY_SETTINGS_FRAGMENT, key);
		Fragment fragment = new PrefFragment();
		fragment.setArguments(args);
		getFragmentManager().beginTransaction().replace(R.id.root_container, fragment).addToBackStack(null).commitAllowingStateLoss();
	}

	public static class PrefFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
		@BindArray(R.array.pref_sync_status_values) String[] entryValues;
		@BindArray(R.array.pref_sync_status_entries) String[] entries;
		private int syncType = 0;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			final String fragment = getArguments() == null ? null : getArguments().getString(KEY_SETTINGS_FRAGMENT);
			if (fragment == null) {
				addPreferencesFromResource(R.xml.preference_headers);
			} else {
				Integer fragmentId = FRAGMENT_MAP.get(fragment);
				if (fragmentId != null) {
					addPreferencesFromResource(fragmentId);
				}
			}

			ButterKnife.bind(this, getActivity());

			updateSyncStatusSummary(PreferencesUtils.KEY_SYNC_STATUSES);

			Preference oslPref = findPreference("open_source_licenses");
			if (oslPref != null) {
				oslPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						new LibsBuilder()
							.withFields(R.string.class.getFields())
							.withLibraries(
								"AndroidIcons",
								"Hugo",
								"Jsoup",
								"LeakCanary",
								"MaterialRangeBar",
								"MPAndroidChart",
								"PhotoView")
							.withAutoDetect(true)
							.withLicenseShown(true)
							.withActivityTitle(getString(R.string.pref_about_licenses))
							.withActivityTheme(R.style.Theme_bgglight_About)
							.withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
							.withAboutIconShown(true)
							.withAboutAppName(getString(R.string.app_name))
							.withAboutVersionShownName(true)
							.start(getActivity());
						return true;
					}
				});
			}
		}

		@Override
		public void onResume() {
			super.onResume();
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			super.onPause();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onStop() {
			super.onStop();
			if (syncType > 0) {
				SyncService.sync(getActivity(), syncType);
				syncType = 0;
			}
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			switch (key) {
				case PreferencesUtils.KEY_SYNC_STATUSES:
					updateSyncStatusSummary(key);
					break;
				case "syncStatuses":
					SyncService.clearCollection(getActivity());
					syncType |= SyncService.FLAG_SYNC_COLLECTION;
					break;
				case "syncPlays":
					SyncService.clearPlays(getActivity());
					syncType |= SyncService.FLAG_SYNC_PLAYS;
					break;
				case "syncBuddies":
					SyncService.clearBuddies(getActivity());
					syncType |= SyncService.FLAG_SYNC_BUDDIES;
					break;
			}
		}

		private void updateSyncStatusSummary(String key) {
			Preference pref = findPreference(key);
			if (pref == null) return;
			Set<String> statuses = PreferencesUtils.getSyncStatuses(getActivity());
			if (statuses == null || statuses.size() == 0) {
				pref.setSummary(R.string.pref_list_empty);
			} else {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < entryValues.length; i++) {
					for (CharSequence status : statuses) {
						CharSequence entry = entryValues[i];
						if (entry.equals(status)) {
							sb.append(entries[i]).append(", ");
							break;
						}
					}
				}
				pref.setSummary(sb.length() > 2 ? sb.substring(0, sb.length() - 2) : sb.toString());
			}
		}

		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
			String key = preference.getKey();
			if (key != null && key.startsWith(ACTION_PREFIX)) {
				((SettingsActivity) getActivity()).replaceFragment(key);
				return true;
			}
			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}
	}
}