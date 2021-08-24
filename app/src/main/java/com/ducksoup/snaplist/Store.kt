package com.ducksoup.snaplist

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

class Store {
    private val lists = mutableMapOf<Int, List>()
    private val items = mutableMapOf<Int, kotlin.collections.List<Item>>()

    fun setLists(lists: kotlin.collections.List<List>) {
        this.lists.clear()
        this.lists.putAll(lists.map { it.id to it })
    }

    fun setItems(listId: Int, items: kotlin.collections.List<Item>) {
        this.items[listId] = items
    }

    fun getToken(activity: FragmentActivity): String {
        return activity.getPreferences(AppCompatActivity.MODE_PRIVATE).getString(Keys.token, null)
            ?: ""
    }

    fun setToken(token: String?, activity: FragmentActivity) {
        val editor = activity.getPreferences(AppCompatActivity.MODE_PRIVATE).edit()
        if (token.isNullOrEmpty()) {
            editor.remove(Keys.token)
        } else {
            editor.putString(Keys.token, token)
        }
        editor.apply()
    }

    override fun toString(): String {
        return lists.toString()
    }

    data class List(val id: Int, val name: String)
    data class Item(val id: Int, val label: String, val checked: Boolean)

    object Keys {
        const val token = "@SnapListToken"
    }
}

