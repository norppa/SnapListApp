package com.ducksoup.snaplist.fragment

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ducksoup.snaplist.R
import com.ducksoup.snaplist.Store
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class StartFragment : Fragment() {
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_short, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_list -> {
                openAddListDialog()
                true
            }
            R.id.menu_preferences -> {
                navController.navigate(R.id.preferencesFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = view.findNavController()
        activity?.title = "SnapList"
        view.findViewById<Button>(R.id.start_button).setOnClickListener { openAddListDialog() }

    }

    private fun openAddListDialog() {
        val ctx = requireContext()
        val dialogView = LayoutInflater.from(ctx).inflate(R.layout.add_list_dialog, null, false)
        MaterialAlertDialogBuilder(ctx)
            .setView(dialogView)
            .setNegativeButton("cancel") { _, _ ->
                dialogView.findViewById<TextInputEditText>(R.id.input_list_name).setText("")
            }
            .setPositiveButton("add") { _, _ ->
                val input = dialogView.findViewById<TextInputEditText>(R.id.input_list_name)
                val listName = input.text.toString()
                Store.createList(listName) {
                    view?.findNavController()?.navigate(R.id.snapListFragment)
                }
                input.setText("")
            }
            .show()
    }
}