package com.ducksoup.snaplist

import android.view.View
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
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
        val body = JSONObject(mapOf("action" to "getLists"))

        fun resultToList(jsonObject: JSONObject): List<StoreList> {
            val jsonArray = jsonObject.getJSONArray("lists")
            val lists = mutableListOf<StoreList>()
            for (i in 0 until jsonArray.length()) {
                val o = jsonArray.getJSONObject(i)
                val list = StoreList(o.getInt("id"), o.getString("name"))
                lists.add(list)
            }
            return lists
        }
        queue.add(request(body, ::resultToList) { callback(it) })
    }

    fun getItems(listId: Int, callback: (lists: List<StoreListItem>) -> Unit) {
        val body = JSONObject(mapOf("action" to "getItems", "listId" to listId))

        fun resultToList(jsonObject: JSONObject): List<StoreListItem> {
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
            return items
        }

        queue.add(request(body, ::resultToList) { callback(it) })
    }

    fun addItem(label: String, listId: Int, callback: (id: Int) -> Unit) {
        val body = JSONObject(mapOf("action" to "addItem", "listId" to listId, "itemName" to label))
        queue.add(request(body, { it.getInt("id") }, { callback(it) }))
    }

    fun setChecked(value: Boolean, itemId: Int, callback: (jsonObject: JSONObject) -> Unit) {
        val body =
            JSONObject(mapOf("action" to "setItemCheck", "itemId" to itemId, "value" to value))

        queue.add(request(body, { it }) { callback(it) })
    }

    fun deleteChecked(listId: Int, callback: () -> Unit) {
        val body = JSONObject(
            mapOf(
                "action" to "deleteCheckedItems",
                "listId" to listId
            )
        )
        queue.add(request(body, { it }, { callback() }))
    }

    fun deleteAll(listId: Int, callback: () -> Unit) {
        val body = JSONObject(
            mapOf(
                "action" to "deleteAllItems",
                "listId" to listId
            )
        )
        queue.add(request(body, { it }, { callback() }))
    }

    private fun <T> request(
        body: JSONObject,
        responseParser: (JSONObject) -> T,
        callback: (T) -> Unit
    ): JsonObjectRequest {
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