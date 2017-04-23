package com.boardgamegeek.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boardgamegeek.R;
import com.boardgamegeek.ui.model.Article;
import com.boardgamegeek.ui.widget.TimestampView;
import com.boardgamegeek.util.ActivityUtils;
import com.boardgamegeek.util.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

public class ThreadRecyclerViewAdapter extends RecyclerView.Adapter<ThreadRecyclerViewAdapter.ArticleViewHolder> {
	private final List<Article> articles;
	private final LayoutInflater inflater;

	public ThreadRecyclerViewAdapter(Context context, List<Article> articles) {
		this.articles = articles;
		inflater = LayoutInflater.from(context);
		setHasStableIds(true);
	}

	@Override
	public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ArticleViewHolder(inflater.inflate(R.layout.row_thread_article, parent, false));
	}

	@Override
	public void onBindViewHolder(ArticleViewHolder holder, int position) {
		holder.bind(articles.get(position));
	}

	@Override
	public int getItemCount() {
		return articles == null ? 0 : articles.size();
	}

	@Override
	public long getItemId(int position) {
		Article article = articles.get(position);
		if (article == null) return RecyclerView.NO_ID;
		return (long) article.id();
	}

	public int getPosition(int articleId) {
		if (articles == null) return RecyclerView.NO_POSITION;
		for (int i = 0; i < articles.size(); i++) {
			if (articles.get(i).id() == articleId) return i;
		}
		return RecyclerView.NO_POSITION;
	}

	public class ArticleViewHolder extends RecyclerView.ViewHolder {
		@BindView(R.id.username) TextView usernameView;
		@BindView(R.id.post_date) TimestampView postDateView;
		@BindView(R.id.date_divider) View dateDivider;
		@BindView(R.id.edit_date) TimestampView editDateView;
		@BindView(R.id.body) TextView bodyView;
		@BindView(R.id.view_button) View viewButton;

		@DebugLog
		public ArticleViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}

		public void bind(Article article) {
			if (article == null) return;

			usernameView.setText(article.username());
			editDateView.setTimestamp(article.editTicks());
			postDateView.setTimestamp(article.postTicks());
			UIUtils.setTextMaybeHtml(bodyView, article.body());
			if (article.editTicks() != article.postTicks()) {
				editDateView.setTimestamp(article.editTicks());
				editDateView.setVisibility(View.VISIBLE);
				dateDivider.setVisibility(View.VISIBLE);
			} else {
				editDateView.setVisibility(View.GONE);
				dateDivider.setVisibility(View.GONE);
			}
			if (TextUtils.isEmpty(article.body())) {
				bodyView.setText("");
			} else {
				UIUtils.setTextMaybeHtml(bodyView, article.body().trim());
			}
			Bundle bundle = new Bundle();
			bundle.putString(ActivityUtils.KEY_USER, article.username());
			bundle.putLong(ActivityUtils.KEY_POST_DATE, article.postTicks());
			bundle.putLong(ActivityUtils.KEY_EDIT_DATE, article.editTicks());
			bundle.putInt(ActivityUtils.KEY_EDIT_COUNT, article.numberOfEdits());
			bundle.putString(ActivityUtils.KEY_BODY, article.body());
			bundle.putString(ActivityUtils.KEY_LINK, article.link());
			bundle.putInt(ActivityUtils.KEY_ARTICLE_ID, article.id());
			viewButton.setTag(bundle);
		}
	}
}
