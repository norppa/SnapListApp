package com.ducksoup.snaplist.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ducksoup.snaplist.ListAdapter
import com.ducksoup.snaplist.R
import com.ducksoup.snaplist.Store
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText

class SnapListFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView

    private lateinit var inputText: EditText
    private lateinit var loadingPanel: RelativeLayout
    private lateinit var addButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_snap_list, container, false)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete_checked -> {
                Store.deleteChecked { refreshList() }
                true
            }
            R.id.menu_delete_all -> {
                Store.deleteAll { refreshList() }
                true
            }
            R.id.menu_add_list -> {
                val ctx = requireContext()
                val view = LayoutInflater.from(ctx).inflate(R.layout.add_list_dialog, null, false)
                MaterialAlertDialogBuilder(ctx)
                    .setView(view)
                    .setNegativeButton("cancel") { _, _ ->
                        view.findViewById<TextInputEditText>(R.id.input_list_name).setText("")
                    }
                    .setPositiveButton("add") { _, _ ->
                        val input = view.findViewById<TextInputEditText>(R.id.input_list_name)
                        val listName = input.text.toString()
                        Store.createList(listName) {
                            tabLayout.addTab(tabLayout.newTab().setText(listName).setId(it), true)
                            Store.setActiveList(tabLayout.selectedTabPosition) { refreshList() }
                        }
                        input.setText("")
                    }
                    .show()
                true
            }
            R.id.menu_delete_list -> {
                val activeListPosition = Store.getActiveListPosition()
                Store.deleteList {
                    if (Store.lists.isEmpty()) {
                        view?.findNavController()?.navigate(R.id.startFragment)
                    }
                    tabLayout.removeTabAt(activeListPosition)
                    refreshList()
                }
                true
            }
            R.id.menu_preferences -> {
                this.findNavController().navigate(R.id.preferencesFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Store.token.isNullOrEmpty()) return view.findNavController()
            .navigate(R.id.loginFragment)

        activity?.title = "SnapList"
        tabLayout = view.findViewById(R.id.tab_layout)
        recyclerView = view.findViewById(R.id.list)
        inputText = view.findViewById(R.id.new_item_text)
        loadingPanel = view.findViewById(R.id.loadingPanel)
        addButton = view.findViewById(R.id.add_button)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val activeListPosition = tab?.position ?: 0
                Store.setActiveList(activeListPosition) { refreshList() }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ListAdapter()

        Store.fetchLists {
            if (Store.lists.isEmpty()) {
                view.findNavController().navigate(R.id.startFragment)
            } else {
                val position = Store.getActiveListPosition()
                Store.lists.forEach { list ->
                    tabLayout.addTab(tabLayout.newTab().setText(list.name).setId(list.id))
                }
                Store.setActiveList(position)

                tabLayout.getTabAt(Store.getActiveListPosition())?.select()
                Store.fetchItems { refreshList() }
            }
        }

        addButton.setOnClickListener {
            val itemText = inputText.text.toString()
            if (itemText.isEmpty()) {
                Toast.makeText(context, "Enter an item to add", Toast.LENGTH_SHORT).show()
            } else {
                setBusy(true)
                Store.addItem(
                    itemText,
                    {
                        refreshList()
                        inputText.setText("")
                        setBusy(false)
                    },
                    {
                        Toast.makeText(context, "Error contacting server", Toast.LENGTH_LONG).show()
                        setBusy(false)
                    }
                )
            }
        }

        inputText.requestFocus()

    }

    private fun setBusy(isBusy: Boolean) {
        if (isBusy) {
            loadingPanel.visibility = View.VISIBLE
            inputText.visibility = View.INVISIBLE
            addButton.isEnabled = false
        } else {
            loadingPanel.visibility = View.INVISIBLE
            inputText.visibility = View.VISIBLE
            inputText.requestFocus()
            addButton.isEnabled = true
        }
    }

    private fun refreshList() {
        recyclerView.adapter?.notifyDataSetChanged()
    }
}