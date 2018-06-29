package com.boardgamegeek.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boardgamegeek.R
import com.boardgamegeek.fadeIn
import com.boardgamegeek.fadeOut
import com.boardgamegeek.ui.adapter.GameDetailAdapter
import com.boardgamegeek.ui.viewmodel.GameViewModel
import com.boardgamegeek.ui.viewmodel.GameViewModel.ProducerType
import kotlinx.android.synthetic.main.fragment_game_details.*
import org.jetbrains.anko.support.v4.act

class GameDetailFragment : Fragment() {
    private val adapter: GameDetailAdapter by lazy {
        GameDetailAdapter()
    }

    private val viewModel: GameViewModel by lazy {
        ViewModelProviders.of(act).get(GameViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_game_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView?.layoutManager = LinearLayoutManager(act)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.producerType.observe(this, Observer {
            adapter.type = it ?: ProducerType.UNKNOWN
        })

        viewModel.producers.observe(this, Observer {
            if (it?.isNotEmpty() == true) {
                adapter.items = it
                emptyMessage?.fadeOut()
                recyclerView?.fadeIn()
            } else {
                adapter.items = emptyList()
                emptyMessage?.fadeIn()
                recyclerView?.fadeOut()
            }
            progressView?.hide()
        })
    }
}
