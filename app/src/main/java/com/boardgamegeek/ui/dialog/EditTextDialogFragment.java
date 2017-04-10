package com.boardgamegeek.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.boardgamegeek.R;
import com.boardgamegeek.util.DialogUtils;
import com.boardgamegeek.util.PresentationUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditTextDialogFragment extends DialogFragment {
	public interface EditTextDialogListener {
		void onFinishEditDialog(String inputText);
	}

	private static final String KEY_TITLE_ID = "title_id";
	@StringRes private int titleResId;
	private String title;
	private ViewGroup root;
	private EditTextDialogListener listener;
	private boolean isUsername;
	private boolean isLongForm;

	private Unbinder unbinder;
	@BindView(R.id.edit_text) EditText editText;
	private String existingText;

	@NonNull
	public static EditTextDialogFragment newInstance(
		@StringRes int titleResId,
		@Nullable ViewGroup root,
		EditTextDialogListener listener) {

		EditTextDialogFragment fragment = new EditTextDialogFragment();
		fragment.initialize(titleResId, null, root, listener, false, false);
		return fragment;
	}

	@NonNull
	public static EditTextDialogFragment newLongFormInstance(
		@StringRes int titleResId,
		@Nullable ViewGroup root,
		EditTextDialogListener listener) {

		EditTextDialogFragment fragment = new EditTextDialogFragment();
		fragment.initialize(titleResId, null, root, listener, false, true);
		return fragment;
	}

	@NonNull
	public static EditTextDialogFragment newLongFormInstance(
		String title,
		@Nullable ViewGroup root,
		EditTextDialogListener listener) {

		EditTextDialogFragment fragment = new EditTextDialogFragment();
		fragment.initialize(0, title, root, listener, false, true);
		return fragment;
	}

	@NonNull
	public static EditTextDialogFragment newUsernameInstance(
		@StringRes int titleResId,
		@Nullable ViewGroup root,
		EditTextDialogListener listener) {

		EditTextDialogFragment fragment = new EditTextDialogFragment();
		fragment.initialize(titleResId, null, root, listener, true, false);
		return fragment;
	}

	private void initialize(
		@StringRes int titleResId,
		String title,
		@Nullable ViewGroup root,
		EditTextDialogListener listener,
		boolean isUsername,
		boolean isLongForm) {

		this.titleResId = titleResId;
		this.title = title;
		this.root = root;
		this.listener = listener;
		this.isUsername = isUsername;
		this.isLongForm = isLongForm;
		setArguments(this.titleResId);
	}

	public void setArguments(@StringRes int titleResId) {
		Bundle bundle = new Bundle();
		bundle.putInt(KEY_TITLE_ID, titleResId);
		setArguments(bundle);
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
		View rootView = layoutInflater.inflate(R.layout.dialog_edit_text, root, false);
		unbinder = ButterKnife.bind(this, rootView);

		if (getArguments() != null) {
			titleResId = getArguments().getInt(KEY_TITLE_ID);
		}

		PresentationUtils.setAndSelectExistingText(editText, existingText);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_bgglight_Dialog_Alert);
		if (titleResId > 0) {
			builder.setTitle(titleResId);
		} else if (!TextUtils.isEmpty(title)) {
			builder.setTitle(title);
		}
		builder.setView(rootView)
			.setNegativeButton(R.string.cancel, null)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (listener != null) {
						listener.onFinishEditDialog(editText.getText().toString().trim());
					}
				}
			});

		final AlertDialog dialog = builder.create();
		int inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS;
		if (isUsername) {
			inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
		}
		if (isLongForm) {
			inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
		}
		editText.setInputType(editText.getInputType() | inputType);
		DialogUtils.requestFocus(dialog, editText);
		return dialog;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (unbinder != null) unbinder.unbind();
	}

	public void setText(String text) {
		this.existingText = text;
	}
}
