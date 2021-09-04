package com.ducksoup.snaplist

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import javax.xml.transform.ErrorListener

object API {
    private lateinit var queue: RequestQueue
    private const val url = "https://jtthaavi.kapsi.fi/subrosa/snaplist"

    fun init(context: Context) {
        queue = Volley.newRequestQueue(context)
    }

    fun login(
        username: String,
        password: String,
        onSuccess: (token: String) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        val errorListener = { volleyError: VolleyError ->
            val errorMessage = when (val statusCode = volleyError.networkResponse.statusCode) {
                401 -> "Incorrect username or password"
                else -> "Network error ($statusCode)"
            }
            onFailure(errorMessage)
        }
        queue.add(
            JsonObjectRequest(
                Request.Method.POST,
                "$url/users/login",
                JSONObject(mapOf("username" to username, "password" to password)),
                { onSuccess(it.getString("token")) },
                errorListener
            )
        )
    }


    fun register(
        username: String,
        password: String,
        onSuccess: (token: String) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        queue.add(JsonObjectRequest(
            Request.Method.POST,
            "$url/users/register",
            JSONObject(mapOf("username" to username, "password" to password)),
            { onSuccess(it.getString("token")) },
            {
                val errorMessage = when (it.networkResponse.statusCode) {
                    400 -> {
                        val data = String(it.networkResponse.data, Charsets.UTF_8)
                        if (JSONObject(String(it.networkResponse.data)).getString("error") == "Username taken") {
                            "Username $username is taken, please choose another username."
                        } else {
                            "Bad request (400)"
                        }
                    }
                    500 -> "Database error! Please try again later."
                    else -> "An error that should not happen, happened :("
                }
                onFailure(errorMessage)
            }
        ))
    }

    fun changeUsername(newUsername: String, callback: (success: Boolean, value: String) -> Unit) {
        queue.add(
            Req(
                "$url/users/username",
                mapOf("username" to newUsername),
                {
                    println("success ${it}")
                    callback(true, it.getString("token"))
                },
                { callback(false, JSONObject(String(it.networkResponse.data)).getString("error")) }
            )
        )
    }

    fun changePassword(newPassword: String, callback: (token: String) -> Unit) {
        queue.add(
            request(
                mapOf("password" to newPassword),
                { it.getString("token") },
                callback,
                "$url/users/password"
            )
        )
    }

    fun deleteUser(callback: () -> Unit) {
        queue.add(JsonObjectRequest(
            Request.Method.POST,
            "$url/users/delete",
            JSONObject(),
            { callback() },
            { printError(it) }
        ))
    }

    fun getLists(callback: (lists: List<StoreList>) -> Unit) {
        val body = mapOf("action" to "getLists")
        val resultConverter = { jsonObject: JSONObject ->
            val jsonArray = jsonObject.getJSONArray("lists")
            val lists = mutableListOf<StoreList>()
            for (i in 0 until jsonArray.length()) {
                val o = jsonArray.getJSONObject(i)
                val list = StoreList(o.getInt("id"), o.getString("name"))
                lists.add(list)
            }
            lists
        }
        queue.add(request(body, resultConverter, callback))
    }

    fun getItems(listId: Int, callback: (lists: List<StoreListItem>) -> Unit) {
        val body = mapOf("action" to "getItems", "listId" to listId)
        val resultConverter = { jsonObject: JSONObject ->
            val jsonArray = jsonObject.getJSONArray("items")
            val items = mutableListOf<StoreListItem>()
            for (i in 0 until jsonArray.length()) {
                val o = jsonArray.getJSONObject(i)
                items.add(
                    StoreListItem(
                        o.getInt("id"),
                        o.getString("item"),
                        o.getInt("checked") != 0
                    )
                )
            }
            items
        }

        queue.add(request(body, resultConverter, callback))
    }

    fun addItem(label: String, listId: Int, callback: (id: Int) -> Unit) {
        val body = mapOf("action" to "addItem", "listId" to listId, "itemName" to label)
        queue.add(request(body, { it.getInt("id") }, callback))
    }

    fun setChecked(value: Boolean, itemId: Int, callback: (jsonObject: JSONObject) -> Unit) {
        val body = mapOf("action" to "setItemCheck", "itemId" to itemId, "value" to value)
        queue.add(request(body, { it }, callback))
    }

    fun deleteChecked(listId: Int, callback: (Unit) -> Unit) {
        val values = mapOf("action" to "deleteCheckedItems", "listId" to listId)
        queue.add(request(values, {}, callback))
    }

    fun deleteAll(listId: Int, callback: (Unit) -> Unit) {
        val values = mapOf("action" to "deleteAllItems", "listId" to listId)
        queue.add(request(values, {}, callback))
    }

    fun createList(name: String, callback: (listId: Int) -> Unit) {
        val values = mapOf("action" to "createList", "listName" to name)
        queue.add(request(values, { it.getInt("id") }, callback))
    }

    fun deleteList(listId: Int, callback: (Unit) -> Unit) {
        val values = mapOf("action" to "deleteList", "listId" to listId)
        queue.add(request(values, {}, callback))
    }

    private fun <T> request(
        values: Map<String, Any>,
        responseParser: (JSONObject) -> T,
        callback: (T) -> Unit,
        url: String = this.url
    ): JsonObjectRequest {
        val body = JSONObject(values)
        return object : JsonObjectRequest(
            Method.POST, url, body,
            { callback(responseParser(it)) },
            { printError(it) }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "Bearer ${Store.token}"
                return headers
            }
        }
    }

    private fun printError(error: VolleyError) {
        println("ERROR")
        println(error)
    }

    private class Req : JsonObjectRequest {
        constructor(
            values: Map<String, Any>,
            responseListener: Response.Listener<JSONObject>,
            errorListener: Response.ErrorListener
        ) : super(Method.POST, API.url, JSONObject(values), responseListener, errorListener)

        constructor(
            url: String,
            values: Map<String, Any>,
            responseListener: Response.Listener<JSONObject>,
            errorListener: Response.ErrorListener
        ) : super(Method.POST, url, JSONObject(values), responseListener, errorListener)

        override fun getHeaders(): Map<String, String> {
            val headers = HashMap<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] = "Bearer ${Store.token}"
            return headers
        }

    }

}

