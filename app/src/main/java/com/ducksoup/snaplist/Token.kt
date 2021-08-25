package com.ducksoup.snaplist

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

object Token {
    private const val tokenKey = "@SnapListToken"
    private var token: String? = null

    fun getToken(activity: FragmentActivity): String {
        if (token.isNullOrEmpty()) {
            val tokenFromPreferences = activity.getPreferences(AppCompatActivity.MODE_PRIVATE)
                .getString(tokenKey, null) ?: ""
            token = tokenFromPreferences
            return tokenFromPreferences

        } else {
            return token as String
        }
    }

    fun setToken(token: String?, activity: FragmentActivity) {
        val editor = activity.getPreferences(AppCompatActivity.MODE_PRIVATE).edit()
        if (token.isNullOrEmpty()) {
            editor.remove(tokenKey)
            this.token = null
        } else {
            editor.putString(tokenKey, token)
            this.token == token
        }
        editor.apply()
    }


}