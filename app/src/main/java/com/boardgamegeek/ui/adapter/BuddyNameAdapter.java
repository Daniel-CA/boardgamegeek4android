package com.boardgamegeek.ui.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.boardgamegeek.R;
import com.boardgamegeek.provider.BggContract.Buddies;
import com.boardgamegeek.provider.BggContract.PlayPlayers;
import com.boardgamegeek.provider.BggContract.Plays;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

public class BuddyNameAdapter extends ArrayAdapter<BuddyNameAdapter.Result> implements Filterable {
	public static class Result {
		private final String username;

		public Result(String username) {
			this.username = username;
		}

		@Override
		public String toString() {
			return username;
		}
	}

	private static final ArrayList<Result> EMPTY_LIST = new ArrayList<>();
	private final ContentResolver resolver;
	private final LayoutInflater inflater;
	private final ArrayList<Result> resultList = new ArrayList<>();

	public BuddyNameAdapter(Context context) {
		super(context, R.layout.autocomplete_item, EMPTY_LIST);
		resolver = context.getContentResolver();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return resultList.size();
	}

	@Override
	public Result getItem(int index) {
		if (index < resultList.size()) {
			return resultList.get(index);
		} else {
			return null;
		}
	}

	@NonNull
	@Override
	public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.autocomplete_item, parent, false);
		}
		final Result result = getItem(position);
		if (result == null) {
			return view;
		}

		TextView titleView = (TextView) view.findViewById(R.id.autocomplete_item);
		if (titleView != null) {
			if (result.username == null) {
				titleView.setVisibility(View.GONE);
			} else {
				titleView.setVisibility(View.VISIBLE);
				titleView.setText(result.username);
			}
		}

		return view;
	}

	@NonNull
	@Override
	public Filter getFilter() {
		return new PlayerFilter();
	}

	public class PlayerFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			final String filter = constraint == null ? "" : constraint.toString();
			if (filter.length() == 0) {
				return null;
			}

			AsyncTask<Void, Void, List<Result>> playerQueryTask = new AsyncTask<Void, Void, List<Result>>() {
				@Override
				protected List<Result> doInBackground(Void... params) {
					return queryPlayerHistory(resolver, filter);
				}
			}.execute();

			HashSet<String> buddyUsernames = new HashSet<>();
			List<Result> buddies = queryBuddies(resolver, filter, buddyUsernames);

			ArrayList<Result> resultList = new ArrayList<>();
			if (buddies != null) {
				resultList.addAll(buddies);
			}

			try {
				List<Result> players = playerQueryTask.get();

				for (Result player : players) {
					if (TextUtils.isEmpty(player.username) || !buddyUsernames.contains(player.username))
						resultList.add(player);
				}
			} catch (ExecutionException | InterruptedException e) {
				Timber.e(e, "Failed waiting for player query results.");
			}

			final FilterResults filterResults = new FilterResults();
			filterResults.values = resultList;
			filterResults.count = resultList.size();
			return filterResults;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			resultList.clear();
			if (results != null && results.count > 0) {
				resultList.addAll((ArrayList<Result>) results.values);
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	private static final String PLAYER_SELECTION = PlayPlayers.USER_NAME + " LIKE ?";
	private static final String[] PLAYER_PROJECTION = new String[] { PlayPlayers._ID, PlayPlayers.USER_NAME };
	private static final int PLAYER_USERNAME = 1;

	private static List<Result> queryPlayerHistory(ContentResolver resolver, String input) {
		String where = null;
		String[] whereArgs = null;

		if (!TextUtils.isEmpty(input)) {
			where = PLAYER_SELECTION;
			String param = input + "%";
			whereArgs = new String[] { param };
		}

		Cursor cursor = resolver.query(Plays.buildPlayersByUniqueUserUri(), PLAYER_PROJECTION, where, whereArgs, PlayPlayers.NAME);
		try {
			List<Result> results = new ArrayList<>();
			if (cursor != null) {
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					String username = cursor.getString(PLAYER_USERNAME);

					results.add(new Result(username));
				}
			}
			return results;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private static final String BUDDY_SELECTION = Buddies.BUDDY_NAME + " LIKE ?";
	private static final String[] BUDDY_PROJECTION = { Buddies._ID, Buddies.BUDDY_NAME };
	private static final int BUDDY_NAME = 1;

	private static List<Result> queryBuddies(ContentResolver resolver, String input, HashSet<String> usernames) {
		String where = null;
		String[] whereArgs = null;

		if (!TextUtils.isEmpty(input)) {
			where = BUDDY_SELECTION;
			String param = input + "%";
			whereArgs = new String[] { param };
		}
		Cursor cursor = resolver.query(Buddies.CONTENT_URI, BUDDY_PROJECTION, where, whereArgs, Buddies.NAME_SORT);
		try {
			List<Result> results = new ArrayList<>();
			if (cursor != null) {
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					String userName = cursor.getString(BUDDY_NAME);
					results.add(new Result(userName));
					usernames.add(userName);
				}
			}
			return results;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
}