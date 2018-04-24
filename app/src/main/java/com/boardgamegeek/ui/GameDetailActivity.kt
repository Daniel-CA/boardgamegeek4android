package com.boardgamegeek.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import com.boardgamegeek.provider.BggContract
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ContentViewEvent
import org.jetbrains.anko.ctx
import org.jetbrains.anko.startActivity

class GameDetailActivity : SimpleSinglePaneActivity() {

    private var title: String? = null
    private var gameId: Int = 0
    private var gameName: String? = null
    private var queryToken: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = gameName
        supportActionBar?.subtitle = title

        if (savedInstanceState == null) {
            Answers.getInstance().logContentView(ContentViewEvent()
                    .putContentType("GameDetail")
                    .putContentName(title))
        }
    }

    override fun readIntent(intent: Intent) {
        title = intent.getStringExtra(KEY_TITLE)
        gameId = intent.getIntExtra(KEY_GAME_ID, BggContract.INVALID_ID)
        gameName = intent.getStringExtra(KEY_GAME_NAME)
        queryToken = intent.getIntExtra(KEY_QUERY_TOKEN, 0)
    }

    override fun onCreatePane(intent: Intent): Fragment = GameDetailFragment.newInstance(gameId, queryToken)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                when (gameId) {
                    BggContract.INVALID_ID -> onBackPressed()
                    else -> GameActivity.startUp(ctx, gameId, gameName)
                }
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val KEY_TITLE = "TITLE"
        private const val KEY_GAME_ID = "GAME_ID"
        private const val KEY_GAME_NAME = "GAME_NAME"
        private const val KEY_QUERY_TOKEN = "QUERY_TOKEN"

        @JvmStatic
        fun start(context: Context, title: String, gameId: Int, gameName: String, queryToken: Int) {
            context.startActivity<GameDetailActivity>(
                    KEY_TITLE to title,
                    KEY_GAME_ID to gameId,
                    KEY_GAME_NAME to gameName,
                    KEY_QUERY_TOKEN to queryToken)
        }
    }
}
