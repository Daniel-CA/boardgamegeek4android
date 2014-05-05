package com.boardgamegeek.service;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.boardgamegeek.io.RemoteBggHandler;
import com.boardgamegeek.io.RemoteExecutor;

public abstract class UpdateTask extends ServiceTask {
	protected RemoteExecutor mExecutor;
	private String mErrorMessage;

	public String getErrorMessage() {
		return mErrorMessage;
	}

	public abstract void execute(Context context);

	public void setExecutor(RemoteExecutor executor) {
		mExecutor = executor;
	}

	protected void safelyExecuteGet(RemoteExecutor executor, String url, RemoteBggHandler handler) {
		try {
			executor.executeGet(url, handler);
		} catch (IOException e) {
			mErrorMessage = e.toString();
		} catch (XmlPullParserException e) {
			mErrorMessage = e.toString();
		}
	}
}
