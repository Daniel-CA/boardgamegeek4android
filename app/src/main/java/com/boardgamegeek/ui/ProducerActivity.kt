package com.boardgamegeek.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import com.boardgamegeek.R
import com.boardgamegeek.provider.BggContract
import com.boardgamegeek.provider.BggContract.*
import com.boardgamegeek.tasks.sync.SyncPublisherTask.CompletedEvent
import com.boardgamegeek.util.PreferencesUtils
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ContentViewEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.ctx
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import timber.log.Timber

enum class ProducerType {
    ARTIST,
    DESIGNER,
    PUBLISHER
}

class ProducerActivity : SimpleSinglePaneActivity() {
    private var uri: Uri? = null
    private var type: ProducerType? = null
    private var id = BggContract.INVALID_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contentType = getContentType(uri, type)
        if (TextUtils.isEmpty(contentType)) {
            Timber.w("Unexpected URI: %s", uri)
            finish()
        }

        supportActionBar?.title = contentType

        if (savedInstanceState == null) {
            val event = ContentViewEvent().putContentType(contentType)
            event.putContentId(id.toString())
            Answers.getInstance().logContentView(event)
        }
    }

    override fun readIntent(intent: Intent) {
        uri = intent.data
        type = intent.getSerializableExtra(KEY_TYPE) as ProducerType?
        id = intent.getIntExtra(KEY_ID, BggContract.INVALID_ID)
    }

    private fun getContentType(uri: Uri?, type: ProducerType?): String? {
        return when {
            type == ProducerType.DESIGNER -> getString(R.string.title_designer)
            type == ProducerType.ARTIST -> getString(R.string.title_artist)
            type == ProducerType.PUBLISHER -> getString(R.string.title_publisher)
            Designers.isDesignerUri(uri) -> getString(R.string.title_designer)
            Artists.isArtistUri(uri) -> getString(R.string.title_artist)
            Publishers.isPublisherUri(uri) -> getString(R.string.title_publisher)
            else -> null
        }
    }

    override fun onCreatePane(intent: Intent): Fragment {
        return ProducerFragment.newInstance(uri, type, id)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: CompletedEvent) {
        if (event.errorMessage.isNotBlank() && PreferencesUtils.getSyncShowErrors(ctx)) {
            // TODO: 3/29/17 makes this a Snackbar
            toast(event.errorMessage)
        }
    }

    companion object {
        private const val KEY_ID = "ID"
        private const val KEY_TYPE = "TYPE"

        @JvmStatic
        fun start(context: Context, id: Int, type: ProducerType) {
            context.startActivity<ProducerActivity>(
                    KEY_ID to id,
                    KEY_TYPE to type)
        }
    }
}
