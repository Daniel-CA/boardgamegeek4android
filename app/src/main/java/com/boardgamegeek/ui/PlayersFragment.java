package com.boardgamegeek.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boardgamegeek.R;
import com.boardgamegeek.events.PlayerSelectedEvent;
import com.boardgamegeek.events.PlayersCountChangedEvent;
import com.boardgamegeek.provider.BggContract.Plays;
import com.boardgamegeek.sorter.PlayersSorter;
import com.boardgamegeek.sorter.PlayersSorterFactory;
import com.boardgamegeek.ui.model.Player;
import com.boardgamegeek.util.StringUtils;
import com.boardgamegeek.util.fabric.SortEvent;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.NonNull;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import butterknife.BindView;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import timber.log.Timber;

public class PlayersFragment extends StickyHeaderListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String STATE_SELECTED_NAME = "selectedName";
	private static final String STATE_SELECTED_USERNAME = "selectedUsername";
	private static final String STATE_SORT_TYPE = "sortType";
	private static final int TOKEN = 0;
	private PlayersAdapter adapter;
	private String selectedName;
	private String selectedUsername;
	private PlayersSorter sorter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			selectedName = savedInstanceState.getString(STATE_SELECTED_NAME);
			selectedUsername = savedInstanceState.getString(STATE_SELECTED_USERNAME);
		}
	}


	@DebugLog
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		int sortType = PlayersSorterFactory.TYPE_DEFAULT;
		if (savedInstanceState != null) {
			sortType = savedInstanceState.getInt(STATE_SORT_TYPE);
		}
		sorter = PlayersSorterFactory.create(getContext(), sortType);

		setEmptyText(getString(R.string.empty_players));
		requery();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		if (!TextUtils.isEmpty(selectedName) || !TextUtils.isEmpty(selectedUsername)) {
			outState.putString(STATE_SELECTED_NAME, selectedName);
			outState.putString(STATE_SELECTED_USERNAME, selectedUsername);
		}
		outState.putInt(STATE_SORT_TYPE, sorter.getType());
	}

	@Override
	public void onListItemClick(View v, int position, long id) {
		final String name = (String) v.getTag(R.id.name);
		final String username = (String) v.getTag(R.id.username);
		EventBus.getDefault().post(new PlayerSelectedEvent(name, username));
		setSelectedPlayer(name, username);
	}

	@DebugLog
	public void requery() {
		getLoaderManager().restartLoader(TOKEN, getArguments(), this);
	}

	public int getSort() {
		return sorter.getType();
	}


	@DebugLog
	public void setSort(int sortType) {
		if (sorter.getType() != sortType) {
			SortEvent.log("Players", String.valueOf(sortType));
			sorter = PlayersSorterFactory.create(getContext(), sortType);
			requery();
		}
	}

	public void setSelectedPlayer(String name, String username) {
		selectedName = name;
		selectedUsername = username;
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data) {
		return new CursorLoader(getActivity(), Plays.buildPlayersByUniquePlayerUri(),
			StringUtils.unionArrays(Player.PROJECTION, sorter.getColumns()),
			null, null, sorter.getOrderByClause());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}

		int token = loader.getId();
		if (token == TOKEN) {
			if (adapter == null) {
				adapter = new PlayersAdapter(getActivity());
				setListAdapter(adapter);
			}
			adapter.changeCursor(cursor);
			EventBus.getDefault().postSticky(new PlayersCountChangedEvent(cursor.getCount()));
			restoreScrollState();
		} else {
			Timber.d("Query complete, Not Actionable: %s", token);
			cursor.close();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.changeCursor(null);
	}

	public class PlayersAdapter extends CursorAdapter implements StickyListHeadersAdapter {
		private final LayoutInflater inflater;

		@DebugLog
		public PlayersAdapter(Context context) {
			super(context, null, false);
			inflater = LayoutInflater.from(context);
		}

		@DebugLog
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View row = inflater.inflate(R.layout.row_players_player, parent, false);
			ViewHolder holder = new ViewHolder(row);
			row.setTag(holder);
			return row;
		}

		@DebugLog
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();

			Player player = Player.fromCursor(cursor);

			view.setActivated(player.getName().equals(selectedName) && player.getUsername().equals(selectedUsername));

			holder.name.setText(player.getName());
			holder.username.setText(player.getUsername());
			holder.username.setVisibility(TextUtils.isEmpty(player.getUsername()) ? View.GONE : View.VISIBLE);
			holder.quantity.setText(sorter.getDisplayInfo(cursor));

			view.setTag(R.id.name, player.getName());
			view.setTag(R.id.username, player.getUsername());
		}

		@DebugLog
		@Override
		public long getHeaderId(int position) {
			return sorter.getHeaderId(getCursor(), position);
		}

		@DebugLog
		@Override
		public View getHeaderView(int position, View convertView, ViewGroup parent) {
			HeaderViewHolder holder;
			if (convertView == null) {
				holder = new HeaderViewHolder();
				convertView = inflater.inflate(R.layout.row_header, parent, false);
				holder.text = convertView.findViewById(android.R.id.title);
				convertView.setTag(holder);
			} else {
				holder = (HeaderViewHolder) convertView.getTag();
			}
			holder.text.setText(getHeaderText(position));
			return convertView;
		}


		@DebugLog
		private String getHeaderText(int position) {
			return sorter.getHeaderText(getCursor(), position);
		}

		class ViewHolder {
			@BindView(android.R.id.title) TextView name;
			@BindView(android.R.id.text1) TextView username;
			@BindView(android.R.id.text2) TextView quantity;

			public ViewHolder(View view) {
				ButterKnife.bind(this, view);
			}
		}

		class HeaderViewHolder {
			TextView text;
		}
	}
}
