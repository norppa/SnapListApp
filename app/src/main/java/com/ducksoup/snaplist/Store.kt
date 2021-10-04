package com.ducksoup.snaplist

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ducksoup.snaplist.API.Reply

object Store {
    private lateinit var prefs: SharedPreferences
    val lists = mutableListOf<StoreList>()
    private var activeListPosition: Int = -1
    var username: String? = null
    var token: String? = null

    private const val sharedPrefsKey = "@SnapListSharedPreferences"
    private const val tokenKey = "@SnapListToken"
    private const val usernameKey = "@SnapListUsername"
    private const val positionKey = "@SnapListPosition"

    enum class Actions { ToggleChecked, AddItem }

    fun init(context: Context) {
        API.init(context)
        prefs = context.getSharedPreferences(sharedPrefsKey, AppCompatActivity.MODE_PRIVATE)
        token = prefs.getString(tokenKey, null)
        username = prefs.getString(usernameKey, null)
        activeListPosition = prefs.getInt(positionKey, 0)
        Log.i("DEBUG", "active list position $activeListPosition")
    }

    fun open(callback: (Reply) -> Unit) {
        API.getLists { response ->
            when (response) {
                is Reply.Success -> {
                    @Suppress("UNCHECKED_CAST")
                    val lists: List<StoreList> = response.value as List<StoreList>
                    this.lists.clear()
                    if (lists.isNotEmpty()) {
                        this.lists.addAll(lists)
                        if (activeListPosition >= this.lists.size) {
                            activeListPosition = 0
                        }

                        setActiveList(activeListPosition) { callback(it) }
                    } else {
                        callback(response)
                    }
                }
                is Reply.Failure -> {
                    callback(Reply.Failure("Failed getting lists"))
                }

            }
        }
    }

    fun getActiveListPosition() = activeListPosition

    fun setActiveList(position: Int, callback: (Reply) -> Unit) {
        activeListPosition = position
        storeActiveList(position)
        val listId = lists[activeListPosition].id
        API.getItems(listId) { response ->
            when (response) {
                is Reply.Success -> {
                    @Suppress("UNCHECKED_CAST")
                    val items: List<StoreListItem> = response.value as List<StoreListItem>
                    lists.find { it.id == listId }?.items = items.sortedBy { it.checked }.toMutableList()
                    callback(response)
                }
                is Reply.Failure -> {
                    callback(Reply.Failure("Failed getting items"))
                }
            }
        }
    }


    fun addItem(label: String, callback: (Reply) -> Unit) {
        val listId = lists[activeListPosition].id
        API.addItem(label, listId) { reply ->
            when (reply) {
                is Reply.Success -> {
                    val id: Int = reply.value as Int
                    lists[activeListPosition].items?.add(0, StoreListItem(id, label, false))
                        ?: throw Exception("Store.addItem empty items list")
                    callback(reply)
                }
                is Reply.Failure -> {
                    callback(Reply.Failure("Failed to add item"))
                }
            }
        }
    }

    fun getItems(): List<StoreListItem> {
        return lists.getOrNull(activeListPosition)?.items ?: listOf()
    }

    fun toggleChecked(itemId: Int, callback: (Reply) -> Unit) {
        val item = lists[activeListPosition].items?.find { it.id == itemId }
            ?: throw IndexOutOfBoundsException()
        API.setChecked(!item.checked, itemId) {reply ->
            when (reply) {
                is Reply.Success -> {
                    item.checked = !item.checked
                    lists[activeListPosition].items?.sortBy { it.checked }
                    callback(reply)
                }
                is Reply.Failure -> {
                    callback(Reply.Failure("Failed to set item check status"))
                }
            }

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

    fun changeUsername(newUsername: String, callback: (errorMessage: String?) -> Unit) {
        API.changeUsername(newUsername) { success: Boolean, value: String ->
            if (success) {
                storeUserInfo(value, newUsername)
                callback(null)
            } else {
                val errorMessage = when (value) {
                    "USERNAME_TAKEN" -> "Username $newUsername has been taken, please choose another username."
                    else -> value
                }
                callback(errorMessage)
            }
        }
    }

    fun changePassword(newPassword: String, callback: (errorMessage: String?) -> Unit) {
        API.changePassword(newPassword) { success: Boolean, value: String ->
            if (success) {
                storeUserInfo(value, username)
                callback(null)
            } else {
                val errorMessage = when (value) {
                    else -> value
                }
                callback(errorMessage)
            }
        }
    }

    fun deleteAccount(callback: (errorMessage: String?) -> Unit) {
        API.deleteAccount { success, value ->
            if (success) {
                logout { callback(null) }
            } else {
                callback(value)
            }
        }
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

    private fun storeActiveList(position: Int) {
        val editor = prefs.edit()
        editor.putInt(positionKey, position)
        editor.apply()
    }

//    sealed class Reply {
//        object Success : Reply()
//        data class Failure(val errorMessage: String) : Reply()
//
//    }

}

data class StoreList(val id: Int, val name: String, var items: MutableList<StoreListItem>? = null)
data class StoreListItem(val id: Int, val label: String, var checked: Boolean)

