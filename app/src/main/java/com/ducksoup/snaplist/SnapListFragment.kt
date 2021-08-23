package com.ducksoup.snaplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController

class SnapListFragment : Fragment() {

    private lateinit var token: Token

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_snap_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        token = Token(requireActivity())
        if (token.get().isNullOrEmpty())
            return view.findNavController().navigate(R.id.loginFragment)

        view.findViewById<Button>(R.id.button).setOnClickListener { logout(it) }
    }

    private fun logout(view: View) {
        token.set(null)
        view.findNavController().navigate(R.id.loginFragment)
    }
}