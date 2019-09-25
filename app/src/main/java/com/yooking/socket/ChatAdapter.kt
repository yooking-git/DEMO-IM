package com.yooking.socket

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * create by yooking on 2019/8/9
 */
class ChatAdapter(data: List<ChatData>?) : BaseMultiItemQuickAdapter<ChatData, BaseViewHolder>(data) {

    companion object {
        const val TYPE_LEFT = 0
        const val TYPE_RIGHT = 1
    }

    init {
        addItemType(TYPE_LEFT, R.layout.item_chat_left)
        addItemType(TYPE_RIGHT, R.layout.item_chat_right)
    }

    override fun convert(helper: BaseViewHolder?, item: ChatData?) {
        if (item != null)
            helper!!.setText(R.id.actv_chat_item_name, item.name)
                .setText(R.id.actv_chat_item_time, item.time)
                .setText(R.id.actv_chat_item_content, item.content)
    }

}