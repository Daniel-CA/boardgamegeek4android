package com.boardgamegeek.pref;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.boardgamegeek.R;
import com.boardgamegeek.auth.Authenticator;
import com.boardgamegeek.service.SyncService;

public class SignOutPreference extends DialogPreference {

	public SignOutPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedValue typedValue = new TypedValue();
		getContext().getTheme().resolveAttribute(android.R.attr.alertDialogIcon, typedValue, true);
		setDialogIcon(typedValue.resourceId);
		setDialogLayoutResource(R.layout.widget_dialogpreference_textview);
	}

	@Override
	public CharSequence getDialogMessage() {
		return getContext().getString(R.string.pref_sync_sign_out_are_you_sure);
	}

	@Override
	public boolean isEnabled() {
		return Authenticator.isSignedIn(getContext());
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			SyncService.cancelSync(getContext());
			Authenticator.signOut(getContext());
			notifyChanged();
		}
	}
}
