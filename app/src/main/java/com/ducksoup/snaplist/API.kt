package com.ducksoup.snaplist

import android.view.View
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

object API {
    private lateinit var queue: RequestQueue
    private const val url = "https://jtthaavi.kapsi.fi/subrosa/snaplist"

    fun init(view: View) {
        queue = Volley.newRequestQueue(view.context)
    }

    fun login(username: String, password: String, callback: (token: String) -> Unit) {
        val body = JSONObject("""{"username":"$username", "password": "$password" }""")
        val loginRequest = JsonObjectRequest(
            Request.Method.POST, "$url/users/login", body,
            { callback(it.getString("token")) },
            { printError(it) }
        )
        queue.add(loginRequest)
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
        queue.add(request(values, {it.getInt("id")}, callback))
    }

    fun deleteList(listId: Int, callback: (Unit) -> Unit) {
        val values = mapOf("action" to "deleteList", "listId" to listId)
        queue.add(request(values, {}, callback))
    }

    private fun <T> request(
        values: Map<String, Any>,
        responseParser: (JSONObject) -> T,
        callback: (T) -> Unit
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
                headers["Authorization"] = "Bearer ${Token.getToken()}"
                return headers
            }
        }
    }

    private fun printError(error: VolleyError) {
        println("ERROR")
        println(error)
    }
}

