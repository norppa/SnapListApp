package com.ducksoup.snaplist

import java.lang.IndexOutOfBoundsException

object Store {
    private val lists = mutableMapOf<Int, List>()
    private val items = mutableMapOf<Int, kotlin.collections.List<Item>>()

    fun setLists(lists: kotlin.collections.List<List>) {
        this.lists.clear()
        this.lists.putAll(lists.map { it.id to it })
    }

    fun setItems(listId: Int, items: kotlin.collections.List<Item>) {
        this.items[listId] = items
    }

    fun getItems(listId: Int):kotlin.collections.List<Item> {
        return items[listId] ?: throw IndexOutOfBoundsException()
    }

    fun setChecked(value: Boolean, listId: Int, itemId: Int, callback: () -> Unit) {
        API.setChecked(value, itemId) {
            println("SUCCESS CALLBACK $it")
            items[listId]?.find { it.id == itemId }?.checked = value
            callback()
        }

    }

    override fun toString(): String {
        return lists.toString()
    }

    data class List(val id: Int, val name: String)
    data class Item(val id: Int, val label: String, var checked: Boolean)
}

