package com.yooking.socket

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * create by yooking on 2019/8/9
 */
class ChatData(private val type: Int) : MultiItemEntity {

    override fun getItemType(): Int {
        return type
    }

    //    var ID: Long ?= 0L
    var name: String? = ""
    var content: String? = ""
    var time: String? = ""
}