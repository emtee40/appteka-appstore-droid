package com.tomclaw.appsend.screen.topics.adapter

import com.avito.konveyor.blueprint.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onItemLongClick(item: Item)

    fun onLoadMore(item: Item)

}
