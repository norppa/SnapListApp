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
        println("LOGIN REQUEST")
        val body = JSONObject("""{"username":"$username", "password": "$password" }""")
        println(body)
        val loginRequest = JsonObjectRequest(
            Request.Method.POST, "$url/users/login", body,
            { callback(it.getString("token"))},
            { printError(it) }
        )
        println(loginRequest)
        queue.add(loginRequest)
    }

    private fun printError(error: VolleyError) {
        println("ERROR")
        println(error)
    }
}