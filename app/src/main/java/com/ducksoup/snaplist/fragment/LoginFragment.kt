package com.ducksoup.snaplist.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import com.ducksoup.snaplist.R
import com.ducksoup.snaplist.Store
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
        view.findViewById<Button>(R.id.login).setOnClickListener { login() }
        view.findViewById<TextView>(R.id.to_register)
            .setOnClickListener { redirectTo(R.id.registerFragment) }
    }

    private fun login() {
        val username = view?.findViewById<TextInputEditText>(R.id.input_username)?.text.toString()
        val password = view?.findViewById<TextInputEditText>(R.id.input_password)?.text.toString()
        Store.login(username, password) { redirectTo(R.id.snapListFragment) }
    }

    private fun redirectTo(target: Int) {
        view?.findNavController()?.navigate(target)
    }
}