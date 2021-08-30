package com.ducksoup.snaplist.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import com.ducksoup.snaplist.API
import com.ducksoup.snaplist.R
import com.ducksoup.snaplist.Token
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "SnapList - Login"
        view.findViewById<Button>(R.id.login).setOnClickListener { login(view) }
        view.findViewById<TextView>(R.id.to_register).setOnClickListener {
            view.findNavController().navigate(R.id.registerFragment)
        }
    }

    private fun login(view: View) {
        val username = view.findViewById<TextInputEditText>(R.id.input_username).text.toString()
        val password = view.findViewById<TextInputEditText>(R.id.input_password).text.toString()
        fun callback(token: String) {
            Token.setToken(token, requireActivity())
            view.findNavController().navigate(R.id.snapListFragment)
        }
        API.login(username, password, ::callback)

        // Hide the keyboard.
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view.windowToken, 0)
    }
}