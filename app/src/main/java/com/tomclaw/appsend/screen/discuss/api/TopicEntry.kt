package com.tomclaw.appsend.screen.discuss.api

import com.google.gson.annotations.SerializedName

data class TopicEntry(
    @SerializedName("topic_id")
    val topicId: Int,
    @SerializedName("type")
    val type: Int,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("package")
    val packageName: String?,
    @SerializedName("read_msg_id")
    val readMsgId: Int?,
    @SerializedName("last_msg")
    val lastMsg: MessageEntry,
)