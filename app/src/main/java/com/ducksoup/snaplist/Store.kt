package com.ducksoup.snaplist

import java.lang.IndexOutOfBoundsException

object Store {
    val lists = mutableListOf(StoreList(0, "", listOf()))
    var activeListPosition: Int = 0

    fun setActiveList(position: Int, callback: () -> Unit = {}) {
        if (position >= lists.size) throw IndexOutOfBoundsException()
        activeListPosition = position
        if (lists[activeListPosition].items == null) {
            fetchItems { callback() }
        } else {
            callback()
        }
    }

    fun fetchLists(callback: () -> Unit) {
        API.getLists { lists ->
            if (lists.isNotEmpty()) {
                this.lists.clear()
                this.lists.addAll(lists)
                activeListPosition = 0
            }
            callback()
        }
    }

    fun fetchItems(callback: (List<StoreListItem>) -> Unit) {
        val listId = lists[activeListPosition].id
        API.getItems(listId) { items ->
            setItems(listId, items)
            callback(items)
        }
    }

    private fun getListById(listId: Int): StoreList {
        return this.lists.find { it.id == listId } ?: throw IndexOutOfBoundsException()
    }

    fun getList(position: Int): StoreList {
        return lists[position]
    }

    fun setItems(listId: Int, items: List<StoreListItem>) {
        getListById(listId).items = items
    }

    fun getItems(): List<StoreListItem> {
        return lists[activeListPosition].items!!
    }

    fun toggleChecked(itemId: Int, callback: () -> Unit) {
        val item = lists[activeListPosition].items?.find { it.id == itemId }
            ?: throw IndexOutOfBoundsException()
        API.setChecked(!item.checked, itemId) {
            item.checked = !item.checked
            callback()
        }


    }

    fun deleteAll(callback: () -> Unit) {
        val list = lists[activeListPosition]
        API.deleteAll(list.id) {
            list.items = mutableListOf()
            callback()
        }
    }

    fun deleteChecked(callback: () -> Unit) {
        val list = lists[activeListPosition]
        API.deleteChecked(list.id) {
            val items = list.items?.filter { !it.checked } ?: throw IndexOutOfBoundsException()
            list.items = items
            callback()
        }
    }
}

data class StoreList(val id: Int, val name: String, var items: List<StoreListItem>? = null)
data class StoreListItem(val id: Int, val label: String, var checked: Boolean)

