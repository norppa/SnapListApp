package com.ducksoup.snaplist.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ducksoup.snaplist.R
import com.ducksoup.snaplist.Store
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PreferencesFragment : Fragment() {
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.title = "SnapList - Preferences (${Store.username})"
        navController = view.findNavController()
        view.findViewById<ConstraintLayout>(R.id.settings_logout).setOnClickListener {
            Store.logout { navController.navigate(R.id.loginFragment) }
        }

        view.findViewById<ConstraintLayout>(R.id.settings_password).setOnClickListener {
            changePasswordDialog(it.context)
        }

        view.findViewById<ConstraintLayout>(R.id.settings_username).setOnClickListener {
            changeUsernameDialog(it.context)
        }

        view.findViewById<ConstraintLayout>(R.id.settings_destroy)
            .setOnClickListener { deleteAccountDialog(it.context) }
    }

    private fun deleteAccountDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Delete account?")
            .setMessage("Are you sure you want to delete your account and all the data associated with it? This action can not be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete account") { _,_ ->
                Store.deleteAccount { errorMessage ->
                    if (errorMessage.isNullOrEmpty()) {
                        navController.navigate(R.id.loginFragment)
                    } else {
                        toast(errorMessage)
                    }
                }
            }
            .show()
    }

    private fun changePasswordDialog(context: Context) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.change_password_dialog, null, false)
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setNegativeButton("cancel", null)
            .setPositiveButton("Change password", null)
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val password1 = view.findViewById<EditText>(R.id.password_1).text.toString()
            val password2 = view.findViewById<EditText>(R.id.password_2).text.toString()
            if (password1 == password2) {
                Store.changePassword(password1) { errorMessage ->
                    if (errorMessage.isNullOrEmpty()) {
                        dialog.dismiss()
                        toast("Password changed successfully!")
                    } else {
                        toast(errorMessage)
                    }
                }
            }
        }
    }

    private fun changeUsernameDialog(context: Context) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.change_username_dialog, null, false)
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setNegativeButton("cancel", null)
            .setPositiveButton("Change username", null)
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val username = view.findViewById<EditText>(R.id.username).text.toString()
            Store.changeUsername(username) { errorMessage ->
                if (errorMessage.isNullOrEmpty()) {
                    dialog.dismiss()
                    activity?.title = "SnapList - Preferences (${Store.username})"
                    toast("Username changed successfully!")
                } else {
                    toast(errorMessage)
                }

            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}