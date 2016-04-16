package com.boardgamegeek.io;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.text.TextUtils;

import com.boardgamegeek.BuildConfig;
import com.boardgamegeek.auth.Authenticator;
import com.boardgamegeek.util.HttpUtils;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;
import retrofit.RestAdapter.LogLevel;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.SimpleXMLConverter;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class Adapter {
	private static final boolean DEBUG = BuildConfig.DEBUG;

	public static BggService create() {
		return createBuilder().build().create(BggService.class);
	}

	public static BoardGameGeekService create2() {
		okhttp3.OkHttpClient okHttpClient = HttpUtils.getHttpClient();
		Retrofit.Builder builder = new Retrofit.Builder()
			.baseUrl("https://www.boardgamegeek.com/")
			.addConverterFactory(SimpleXmlConverterFactory.createNonStrict());
		if (DEBUG) {
			final okhttp3.OkHttpClient.Builder clientBuilder = okHttpClient.newBuilder();
			HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
			httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			clientBuilder.addInterceptor(httpLoggingInterceptor);
			okHttpClient = clientBuilder.build();
		}
		builder.client(okHttpClient);
		return builder.build().create(BoardGameGeekService.class);
	}

	public static BggService createWithJson() {
		return createBuilderWithoutConverter().build().create(BggService.class);
	}

	public static BggService createWithAuth(Context context) {
		return addAuth(context, createBuilder()).build().create(BggService.class);
	}

	public static BggService createForPost(Context context, Converter converter) {
		return addAuth(context, createBuilder()).setConverter(converter).build().create(BggService.class);
	}

	private static Builder createBuilder() {
		return createBuilderWithoutConverter().setConverter(new SimpleXMLConverter(false));
	}

	private static Builder createBuilderWithoutConverter() {
		OkHttpClient client = new OkHttpClient();
		client.interceptors().add(new RetryInterceptor());

		Builder builder = new RestAdapter.Builder()
			.setEndpoint("https://www.boardgamegeek.com/")
			.setClient(new OkClient(client));
		if (DEBUG) {
			builder.setLog(new AndroidLog("BGG-retrofit")).setLogLevel(LogLevel.FULL);
		}

		return builder;
	}

	private static Builder addAuth(Context context, Builder builder) {
		RequestInterceptor requestInterceptor = null;

		AccountManager accountManager = AccountManager.get(context);
		final Account account = Authenticator.getAccount(accountManager);
		if (account != null) {
			try {
				final String authToken = accountManager.blockingGetAuthToken(account, Authenticator.AUTH_TOKEN_TYPE, true);
				requestInterceptor = new RequestInterceptor() {
					@Override
					public void intercept(RequestFacade request) {
						if (!TextUtils.isEmpty(account.name) && !TextUtils.isEmpty(authToken)) {
							request.addHeader("Cookie", "bggusername=" + account.name + "; bggpassword=" + authToken);
						}
					}
				};
			} catch (OperationCanceledException | AuthenticatorException | IOException e) {
				// TODO handle this somehow; maybe just return create()
			}
		}

		if (requestInterceptor != null) {
			builder.setRequestInterceptor(requestInterceptor);
		}
		return builder;
	}
}
