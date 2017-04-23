package com.boardgamegeek.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.boardgamegeek.R;
import com.boardgamegeek.filterer.CollectionFilterer;
import com.boardgamegeek.filterer.CollectionStatusFilterer;

public class CollectionStatusFilterDialog implements CollectionFilterDialog {

	private String[] statusEntries;
	private boolean[] selectedStatuses;

	@Override
	public void createDialog(final Context context, final OnFilterChangedListener listener, CollectionFilterer filter) {
		init(context, (CollectionStatusFilterer) filter);

		new AlertDialog.Builder(context, R.style.Theme_bgglight_Dialog_Alert)
			.setTitle(R.string.menu_collection_status)
			.setMultiChoiceItems(statusEntries, selectedStatuses, new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					selectedStatuses[which] = isChecked;
				}
			})
			.setNegativeButton(R.string.or, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (listener != null)
						listener.addFilter(new CollectionStatusFilterer(context, selectedStatuses, true));
				}
			})
			.setPositiveButton(R.string.and, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (listener != null)
						listener.addFilter(new CollectionStatusFilterer(context, selectedStatuses, false));
				}
			})
			.setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (listener != null) listener.removeFilter(getType(context));
				}
			})
			.create()
			.show();
	}

	@Override
	public int getType(Context context) {
		return new CollectionStatusFilterer(context).getType();
	}

	private void init(final Context context, CollectionStatusFilterer filter) {
		statusEntries = context.getResources().getStringArray(R.array.collection_status_filter_entries);
		if (filter == null) {
			selectedStatuses = new boolean[statusEntries.length];
		} else {
			selectedStatuses = filter.getSelectedStatuses();
		}
	}
}
