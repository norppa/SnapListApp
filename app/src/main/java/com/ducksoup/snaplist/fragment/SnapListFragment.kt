package com.ducksoup.snaplist.fragment

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_snap_list, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete_checked -> {
                Store.deleteChecked() { refreshList() }
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
            R.id.menu_logout -> {
                Store.logout { view?.findNavController()?.navigate(R.id.loginFragment) }
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
                Store.lists.forEach { list ->
                    tabLayout.addTab(tabLayout.newTab().setText(list.name).setId(list.id))
                }
                Store.fetchItems { refreshList() }
            }
        }

        val inputText = view.findViewById<EditText>(R.id.new_item_text)
        view.findViewById<FloatingActionButton>(R.id.add_button).setOnClickListener {
            Store.addItem(inputText.text.toString()) {
                refreshList()
                inputText.setText("")
            }
        }

    }

    private fun refreshList() {
        recyclerView.adapter?.notifyDataSetChanged()
    }
}