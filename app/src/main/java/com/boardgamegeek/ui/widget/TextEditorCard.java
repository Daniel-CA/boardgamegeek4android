package com.boardgamegeek.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.boardgamegeek.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TextEditorCard extends FrameLayout {
	@BindView(R.id.text_editor_header) TextView headerView;
	@BindView(R.id.text_editor_content) TextView contentView;
	@BindView(R.id.text_editor_timestamp) TimestampView timestampView;
	@BindView(R.id.text_editor_image) ImageView imageView;
	boolean isInEditMode;

	public TextEditorCard(Context context) {
		super(context);
		init(null);
	}

	public TextEditorCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		LayoutInflater.from(getContext()).inflate(R.layout.widget_text_editor_card, this, true);
		ButterKnife.bind(this);

		setVisibility(View.GONE);

		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TextEditorCard);
			try {
				String title = a.getString(R.styleable.TextEditorCard_headerText);
				headerView.setText(title);
			} finally {
				a.recycle();
			}
		}
	}

	public void setContentText(CharSequence text) {
		contentView.setText(text);
		contentView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
		setEditMode();
	}

	public void setTimestamp(long timestamp) {
		timestampView.setTimestamp(timestamp);
		setEditMode();
	}

	public String getContentText() {
		return contentView.getText().toString();
	}

	public String getHeaderText() {
		return headerView.getText().toString();
	}

	public void setHeaderColor(@ColorInt int color) {
		headerView.setTextColor(color);
	}

	public void enableEditMode(boolean enable) {
		isInEditMode = enable;
		setEditMode();
	}

	public static final ButterKnife.Setter<TextEditorCard, Palette.Swatch> headerColorSetter =
		new ButterKnife.Setter<TextEditorCard, Palette.Swatch>() {
			@Override
			public void set(@NonNull TextEditorCard view, Palette.Swatch value, int index) {
				if (value != null) {
					view.setHeaderColor(value.getRgb());
				}
			}
		};

	private void setEditMode() {
		if (isInEditMode) {
			imageView.setVisibility(VISIBLE);
			setVisibility(View.VISIBLE);
			setClickable(true);
		} else {
			imageView.setVisibility(GONE);
			setVisibility(TextUtils.isEmpty(contentView.getText()) && timestampView.getTimestamp() == 0 ? View.GONE : View.VISIBLE);
			setClickable(false);
		}
	}
}
