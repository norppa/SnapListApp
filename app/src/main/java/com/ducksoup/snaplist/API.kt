package com.ducksoup.snaplist

import android.view.View
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class API(view: View) {
    private val queue = Volley.newRequestQueue(view.context)
    private val url = "https://jtthaavi.kapsi.fi/subrosa/snaplist"

    fun login(username: String, password: String, callback: (token: String) -> Unit) {
        val body = JSONObject("""{"username":"$username", "password": "$password" }""")
        val loginRequest = JsonObjectRequest(
            Request.Method.POST, "$url/users/login", body,
            { callback(it.getString("token")) },
            { printError(it) }
        )
        queue.add(loginRequest)
    }

    fun getLists(token: String, callback: (lists: List<Store.List>) -> Unit) {
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

        val request = object : JsonObjectRequest(
            Method.POST, url, body,
            { callback(resultToList(it)) },
            { println("Error: $it") }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }
        queue.add(request)
    }

    fun getItems(listId: Int, token: String, callback: (lists: List<Store.Item>) -> Unit) {
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


        val request = object : JsonObjectRequest(
            Method.POST, url, body,
            { callback(resultToList(it)) },
            { println("Error: $it") }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }
        queue.add(request)
    }

    private fun printError(error: VolleyError) {
        println("ERROR")
        println(error)
    }
}