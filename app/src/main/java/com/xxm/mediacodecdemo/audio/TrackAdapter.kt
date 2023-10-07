package com.xxm.mediacodecdemo.audio

import android.content.Context
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseSingleItemAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.xxm.mediacodecdemo.R

/**
 *Time:2023/10/7
 *Author:zhangwenshuan
 *Description:
 *since version
 *
 */
class TrackAdapter : BaseQuickAdapter<TrackData, QuickViewHolder>() {


    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: TrackData?) {
        holder.setText(R.id.tvName, item!!.path)
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): QuickViewHolder {
        return QuickViewHolder(R.layout.layout_track, parent)
    }
}