package com.ducksoup.snaplist.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ducksoup.snaplist.R
import com.ducksoup.snaplist.Store
import com.google.android.material.textfield.TextInputEditText


class RegisterFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var error: TextView
    private lateinit var username: TextInputEditText
    private lateinit var password1: TextInputEditText
    private lateinit var password2: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "SnapList - Register"
        navController = view.findNavController()
        error = view.findViewById(R.id.register_error)
        username = view.findViewById(R.id.register_input_username)
        password1 = view.findViewById(R.id.register_input_password1)
        password2 = view.findViewById(R.id.register_input_password2)
        view.findViewById<Button>(R.id.register_button).setOnClickListener { register() }
        view.findViewById<TextView>(R.id.to_login).setOnClickListener {
            navController.navigate(R.id.loginFragment)
        }
        username.addTextChangedListener { hideError() }
        password1.addTextChangedListener { hideError() }
        password2.addTextChangedListener { hideError() }
    }

    private fun register() {
        val username = username.text.toString()
        val password1 = password1.text.toString()
        val password2 = password2.text.toString()
        if (username.isEmpty()) return showError("Enter username")
        if (password1 != password2) return showError("Passwords don't match")
        Store.register(
            username,
            password1,
            { navController.navigate(R.id.snapListFragment) },
            { showError(it) })
    }

    private fun hideError() {
        error.visibility = View.GONE
    }

    private fun showError(text: String) {
        error.text = text
        error.visibility = View.VISIBLE
    }
}