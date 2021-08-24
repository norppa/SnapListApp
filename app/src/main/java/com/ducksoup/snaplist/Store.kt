package com.ducksoup.snaplist

class Store {


    private val lists = mutableMapOf<Int, List>()
    private val items = mutableMapOf<Int, kotlin.collections.List<Item>>()

    fun setLists(lists: kotlin.collections.List<List>) {
        this.lists.clear()
        this.lists.putAll(lists.map { it.id to it})
    }

    fun setItems(listId: Int, items: kotlin.collections.List<Item>) {
        this.items[listId] = items
    }

    override fun toString(): String {
        return lists.toString()
    }

    data class List(val id: Int, val name: String)
    data class Item(val id: Int, val label: String, val checked: Boolean)
}

