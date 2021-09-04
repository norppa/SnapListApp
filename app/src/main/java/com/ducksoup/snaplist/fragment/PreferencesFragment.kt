package com.ducksoup.snaplist.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.ducksoup.snaplist.R
import com.ducksoup.snaplist.Store
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PreferencesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.title = "SnapList - Preferences (${Store.username})"
        val navController = view.findNavController()
        view.findViewById<ConstraintLayout>(R.id.settings_logout).setOnClickListener {
            Store.logout { navController.navigate(R.id.loginFragment) }
        }

        view.findViewById<ConstraintLayout>(R.id.settings_password).setOnClickListener {
            changePasswordDialog(it.context)
        }

        view.findViewById<ConstraintLayout>(R.id.settings_username).setOnClickListener {
            changeUsernameDialog(it.context)
        }
    }

    private fun changePasswordDialog(context: Context) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.change_password_dialog, null, false)
        MaterialAlertDialogBuilder(context)
            .setView(view)
            .setNegativeButton("cancel") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setPositiveButton("Change password") { dialogInterface, _ ->
                val password1 = view.findViewById<EditText>(R.id.password_1).text.toString()
                val password2 = view.findViewById<EditText>(R.id.password_2).text.toString()
                if (password1 == password2) {
                    Store.changePassword(password1) {
                        println("password changed")
                        dialogInterface.dismiss()
                    }
                }
            }
            .show()
    }

    private fun changeUsernameDialog(context: Context) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.change_username_dialog, null, false)
        MaterialAlertDialogBuilder(context)
            .setView(view)
            .setNegativeButton("cancel") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setPositiveButton("Change username") { dialogInterface, _ ->
                val username = view.findViewById<EditText>(R.id.username).text.toString()
                Store.changeUsername(username) {
                    dialogInterface.dismiss()
                }

            }
            .show()
    }
}