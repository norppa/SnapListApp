package com.ducksoup.snaplist.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.ducksoup.snaplist.R
import com.ducksoup.snaplist.Store
import com.google.android.material.textfield.TextInputEditText


class RegisterFragment : Fragment() {
    private lateinit var error: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "SnapList - Register"
        error = view.findViewById(R.id.register_error)
        view.findViewById<Button>(R.id.register_button).setOnClickListener { register(view) }
        view.findViewById<TextView>(R.id.to_login).setOnClickListener {
            view.findNavController().navigate(R.id.loginFragment)
        }
        view.findViewById<TextInputEditText>(R.id.register_input_password1)
            .addTextChangedListener { hideError() }
        view.findViewById<TextInputEditText>(R.id.register_input_password2)
            .addTextChangedListener { hideError() }
    }

    private fun register(view: View) {
        val username =
            view.findViewById<TextInputEditText>(R.id.register_input_username).text.toString()
        val password1 =
            view.findViewById<TextInputEditText>(R.id.register_input_password1).text.toString()
        val password2 =
            view.findViewById<TextInputEditText>(R.id.register_input_password2).text.toString()
        if (password1 != password2) return showError("Passwords don't match")
        Store.register(username, password1) {
            view.findNavController().navigate(R.id.snapListFragment)
        }
    }

    private fun hideError() {
        error.visibility = View.GONE
    }

    private fun showError(text: String) {
        error.text = text
        error.visibility = View.VISIBLE
    }
}