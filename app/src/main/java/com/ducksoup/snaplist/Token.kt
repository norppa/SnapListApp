package com.ducksoup.snaplist

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

class Token(private val activity: FragmentActivity) {

    private val tokenKey = "@SnapListToken"
    private val preferences = activity.getPreferences(AppCompatActivity.MODE_PRIVATE)

    fun get() = preferences.getString(tokenKey, null)
    fun set(token: String?) {
        val editor = preferences.edit()
        editor.putString(tokenKey, token)
        editor.apply()
    }


}