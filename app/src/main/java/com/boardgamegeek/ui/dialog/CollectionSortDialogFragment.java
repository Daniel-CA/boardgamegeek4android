package com.boardgamegeek.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;

import com.boardgamegeek.R;
import com.boardgamegeek.sorter.CollectionSorter;
import com.boardgamegeek.sorter.CollectionSorterFactory;
import com.boardgamegeek.util.PreferencesUtils;
import com.boardgamegeek.util.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class CollectionSortDialogFragment extends DialogFragment implements OnCheckedChangeListener {
	public interface Listener {
		void onSortSelected(int sortType);
	}

	private Unbinder unbinder;
	private ViewGroup root;
	private Listener listener;
	private int selectedType;
	@BindView(R.id.scroll_container) ScrollView scrollContainer;
	@BindView(R.id.radio_group) RadioGroup radioGroup;
	@BindViews({
		R.id.name,
		R.id.rank,
		R.id.geek_rating,
		R.id.average_rating,
		R.id.my_rating,
		R.id.last_viewed,
		R.id.wishlist_priority,
		R.id.play_count_asc,
		R.id.play_count_desc,
		R.id.play_date_max,
		R.id.play_date_min,
		R.id.year_published_asc,
		R.id.year_published_desc,
		R.id.play_time_asc,
		R.id.play_time_desc,
		R.id.suggested_age_asc,
		R.id.suggested_age_desc,
		R.id.average_weight_asc,
		R.id.average_weight_desc,
		R.id.acquisition_date,
		R.id.acquired_from,
		R.id.price_paid,
		R.id.current_value,
		R.id.inventory_location
	}) List<RadioButton> radioButtons;
	@BindView(R.id.play_date_max) RadioButton playDateMaxRadioButton;
	@BindView(R.id.play_date_min) RadioButton playDateMinRadioButton;

	@DebugLog
	public CollectionSortDialogFragment() {
	}

	@DebugLog
	public static CollectionSortDialogFragment newInstance(@Nullable ViewGroup root, @Nullable Listener listener) {
		final CollectionSortDialogFragment fragment = new CollectionSortDialogFragment();
		fragment.initialize(root, listener);
		return fragment;
	}

	@DebugLog
	private void initialize(@Nullable ViewGroup root, @Nullable Listener listener) {
		this.root = root;
		this.listener = listener;
	}

	@DebugLog
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_collection_sort, root, false);

		unbinder = ButterKnife.bind(this, rootView);
		if (!PreferencesUtils.getSyncPlays(getContext())) {
			hideRadioButtonIfNotSelected(playDateMaxRadioButton);
			hideRadioButtonIfNotSelected(playDateMinRadioButton);
		}
		setChecked();
		radioGroup.setOnCheckedChangeListener(this);
		createNames();

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setView(rootView);
		builder.setTitle(R.string.title_sort);
		return builder.create();
	}

	private void hideRadioButtonIfNotSelected(RadioButton radioButton) {
		if (findSelectedRadioButton() != radioButton) {
			radioButton.setVisibility(View.GONE);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (unbinder != null) unbinder.unbind();
	}

	@DebugLog
	private void createNames() {
		CollectionSorterFactory factory = new CollectionSorterFactory(getContext());
		for (RadioButton radioButton : radioButtons) {
			int sortType = getTypeFromView(radioButton);
			CollectionSorter sorter = factory.create(sortType);
			if (sorter != null) {
				radioButton.setText(sorter.getDescription());
			}
		}
	}

	@DebugLog
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		int sortType = getTypeFromView(group.findViewById(checkedId));
		Timber.d("Sort by %s", sortType);
		if (listener != null) {
			listener.onSortSelected(sortType);
		}
		dismiss();
	}

	@DebugLog
	public void setSelection(int type) {
		selectedType = type;
		setChecked();
	}

	@DebugLog
	private void setChecked() {
		RadioButton radioButton = findSelectedRadioButton();
		if (radioButton != null) {
			checkRadioButton(radioButton);
			focusRadioButton(radioButton);
		}
	}

	@DebugLog
	private RadioButton findSelectedRadioButton() {
		if (radioButtons != null) {
			for (RadioButton radioButton : radioButtons) {
				int type = getTypeFromView(radioButton);
				if (type == selectedType) {
					return radioButton;
				}
			}
		}
		return null;
	}

	@DebugLog
	private void checkRadioButton(RadioButton radioButton) {
		radioGroup.setOnCheckedChangeListener(null);
		radioButton.setChecked(true);
		radioGroup.setOnCheckedChangeListener(this);
	}

	@DebugLog
	private void focusRadioButton(final RadioButton radioButton) {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				if (scrollContainer == null) return;
				scrollContainer.scrollTo(0, (int) radioButton.getY());
			}
		});
	}

	@DebugLog
	private int getTypeFromView(View view) {
		return StringUtils.parseInt(view.getTag().toString(), CollectionSorterFactory.TYPE_DEFAULT);
	}
}
