package com.ducksoup.snaplist

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

object Store {
    private lateinit var prefs: SharedPreferences
    val lists = mutableListOf<StoreList>()
    private var activeListPosition: Int = 0
    var username: String? = null
    var token: String? = null

    private const val sharedPrefsKey = "@SnapListSharedPreferences"
    private const val tokenKey = "@SnapListToken"
    private const val usernameKey = "@SnapListUsername"

    fun init(context: Context) {
        API.init(context)
        prefs = context.getSharedPreferences(sharedPrefsKey, AppCompatActivity.MODE_PRIVATE)
        token = prefs.getString(tokenKey, null)
        username = prefs.getString(usernameKey, null)
    }

    fun getActiveListPosition() = activeListPosition

    fun setActiveList(position: Int, callback: () -> Unit = {}) {
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

    private fun setItems(listId: Int, items: List<StoreListItem>) {
        lists.find { it.id == listId }?.items = items.toMutableList()
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

    fun login(
        username: String,
        password: String,
        successCallback: () -> Unit,
        failureCallback: (String) -> Unit
    ) {
        val onSuccess = { token: String ->
            storeUserInfo(token, username)
            successCallback()
        }
        API.login(username, password, onSuccess, failureCallback)
    }

    fun register(
        username: String,
        password: String,
        successCallback: () -> Unit,
        failureCallback: (String) -> Unit
    ) {
        val onSuccess = { token: String ->
            storeUserInfo(token, username)
            successCallback()
        }
        API.register(username, password, onSuccess, failureCallback)
    }

    fun logout(callback: () -> Unit) {
        lists.clear()
        activeListPosition = 0
        storeUserInfo(null, null)
        callback()
    }

    fun changeUsername(
        newUsername: String,
        callback: (errorMessage: String?) -> Unit
    ) {
        API.changeUsername(newUsername) { success: Boolean, value: String ->
            if (success) {
                storeUserInfo(value, newUsername)
                callback( null)
            } else {
                val errorMessage = when (value) {
                    "USERNAME_TAKEN" -> "Username $newUsername has been taken, please choose another username."
                    else -> value
                }
                callback( errorMessage)
            }
        }
    }

    fun changePassword(newPassword: String, callback: () -> Unit) {
        API.changePassword(newPassword) {
            storeUserInfo(it, username)
            callback()
        }
    }

    fun deleteUser(callback: () -> Unit) {
        API.deleteUser { logout { callback() } }
    }

    private fun storeUserInfo(token: String?, username: String?) {
        this.token = token
        this.username = username
        val editor = prefs.edit()
        if (token.isNullOrEmpty()) editor.remove(tokenKey) else editor.putString(tokenKey, token)
        if (username.isNullOrEmpty()) editor.remove(usernameKey) else editor.putString(
            usernameKey,
            username
        )
        editor.apply()
    }
}

data class StoreList(val id: Int, val name: String, var items: MutableList<StoreListItem>? = null)
data class StoreListItem(val id: Int, val label: String, var checked: Boolean)