package com.ducksoup.snaplist.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.navigation.findNavController
import com.ducksoup.snaplist.R
import com.ducksoup.snaplist.Store
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment() {
    private lateinit var error: TextView
    private lateinit var username: TextInputEditText
    private lateinit var password: TextInputEditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "SnapList - Login"
        view.findViewById<Button>(R.id.login).setOnClickListener { login() }
        view.findViewById<TextView>(R.id.to_register)
            .setOnClickListener { redirectTo(R.id.registerFragment) }
        error = view.findViewById(R.id.error_text)
        username = view.findViewById(R.id.input_username)
        password = view.findViewById(R.id.input_password)

        username.addTextChangedListener { hideError() }
        password.addTextChangedListener { hideError() }
    }

    private fun login() {
        val username = username.text.toString()
        val password = password.text.toString()
        if (username.isEmpty()) return showError("Enter username")
        if (password.isEmpty()) return showError("Enter password")
        Store.login(username, password, { redirectTo(R.id.snapListFragment) }, { showError(it) })
    }

    private fun redirectTo(target: Int) {
        view?.findNavController()?.navigate(target)
    }

    private fun showError(message: String) {
        error.text = message
        error.visibility = View.VISIBLE
    }

    private fun hideError() {
        error.text = ""
        error.visibility = View.GONE
    }
}