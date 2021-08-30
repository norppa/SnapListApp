package com.ducksoup.snaplist

import java.lang.IndexOutOfBoundsException

object Store {
    val lists = mutableListOf<StoreList>()
    private var activeListPosition: Int = 0

    fun getActiveListPosition() = activeListPosition

    fun setActiveList(position: Int, callback: () -> Unit = {}) {
        if (position >= lists.size) throw IndexOutOfBoundsException("Tried to set active list to $position, but only ${lists.size} lists available")
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

    private fun setItems(listId: Int, items: List<StoreListItem>) {
        getListById(listId).items = items.toMutableList()
    }

    fun addItem(label: String, callback: () -> Unit) {
        val listId = lists[activeListPosition].id
        API.addItem(label, listId) {
            lists[activeListPosition].items?.add(StoreListItem(it, label, false))
            callback()
        }
    }

    fun getItems(): List<StoreListItem> {
        return lists.getOrNull(activeListPosition)?.items ?: listOf()
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
        API.deleteChecked(lists[activeListPosition].id) {
            lists[activeListPosition].items?.removeAll { it.checked }
            callback()
        }
    }

    fun createList(name: String, callback: (id: Int) -> Unit) {
        API.createList(name) {
            val list = StoreList(it, name, mutableListOf())
            lists.add(list)
            activeListPosition = lists.size - 1
            callback(it)
        }
    }

    fun deleteList(callback: () -> Unit) {
        API.deleteList(lists[activeListPosition].id) {
            lists.removeAt(activeListPosition)
            activeListPosition = if (activeListPosition == 0) 0 else activeListPosition - 1
            callback()
        }
    }

    fun clear() {
        lists.clear()
        activeListPosition = 0

    }
}

data class StoreList(val id: Int, val name: String, var items: MutableList<StoreListItem>? = null)
data class StoreListItem(val id: Int, val label: String, var checked: Boolean)