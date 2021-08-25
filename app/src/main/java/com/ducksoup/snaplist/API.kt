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

    fun getLists(callback: (lists: List<Store.List>) -> Unit) {
        val body = JSONObject(mapOf("action" to "getLists"))

        fun resultToList(jsonObject: JSONObject): List<Store.List> {
            val jsonArray = jsonObject.getJSONArray("lists")
            val lists = mutableListOf<Store.List>()
            for (i in 0 until jsonArray.length()) {
                val o = jsonArray.getJSONObject(i)
                val list = Store.List(o.getInt("id"), o.getString("name"))
                lists.add(list)
            }
            return lists
        }
        queue.add(request(body, ::resultToList) { callback(it)})
    }

    fun getItems(listId: Int, callback: (lists: List<Store.Item>) -> Unit) {
        val body = JSONObject(mapOf("action" to "getItems", "listId" to listId))

        fun resultToList(jsonObject: JSONObject): List<Store.Item> {
            val jsonArray = jsonObject.getJSONArray("items")
            val items = mutableListOf<Store.Item>()
            for (i in 0 until jsonArray.length()) {
                val o = jsonArray.getJSONObject(i)
                items.add(Store.Item(o.getInt("id"), o.getString("item"), o.getInt("checked") != 0))
            }
            return items
        }

        queue.add(request(body, ::resultToList) { callback(it) })
    }

    fun setChecked(value: Boolean, itemId: Int, callback: (jsonObject: JSONObject) -> Unit) {
        val body =
            JSONObject(mapOf("action" to "setItemCheck", "itemId" to itemId, "value" to value))

        queue.add(request(body, { it }) { callback(it) })
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
            override fun getHeaders(): Map<String, String> = generateHeaders()
        }
    }

    private fun generateHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()
        headers["Content-Type"] = "application/json"
        headers["Authorization"] = "Bearer ${Token.getToken()}"
        return headers
    }

    private fun printError(error: VolleyError) {
        println("ERROR")
        println(error)
    }
}