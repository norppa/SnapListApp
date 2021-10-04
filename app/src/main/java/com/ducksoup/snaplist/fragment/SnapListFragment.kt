package com.ducksoup.snaplist.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ducksoup.snaplist.API.Reply
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
        return inflater.inflate(R.layout.fragment_snap_list, container, false)
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

        tabLayout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            tabLayout.scrollX = (0 until Store.getActiveListPosition())
                .fold(0, { acc, i -> acc + (tabLayout.getTabAt(i)?.view?.width ?: 0) }) - 100
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val activeListPosition = tab?.position ?: 0
                Store.setActiveList(activeListPosition) { refreshList() }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = Adapter()

        Store.open openingProcedure@{ reply ->
            when (reply) {
                is Reply.Success -> {

                    if (Store.lists.isEmpty()) {
                        view.findNavController().navigate(R.id.startFragment)
                        return@openingProcedure
                    }
                    Store.lists.forEach { list ->
                        tabLayout.addTab(
                            tabLayout.newTab().setText(list.name).setId(list.id),
                            false
                        )
                    }
                    tabLayout.getTabAt(Store.getActiveListPosition())?.select()
                    refreshList()

                    addButton.setOnClickListener { addItem() }
                    inputText.isEnabled = true
                    inputText.requestFocus()
                }
                is Reply.Failure -> toast(reply.errorMessage, Toast.LENGTH_SHORT)
            }

        }
    }

    private fun addItem() {
        val itemText = inputText.text.toString()
        if (itemText.isEmpty()) return toast("Enter an item to add")

        callStore(Store.Actions.AddItem, itemText) {
            refreshList()
            inputText.setText("")
        }
    }

    private fun refreshList() {
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun toast(message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, length).show()
    }

    private fun callStore(action: Store.Actions, parameter: Any, onSuccess: () -> Unit) {
        val incorrectParameters by lazy { Reply.Failure("Parameter type exception") }

        loadingPanel.visibility = View.VISIBLE
        inputText.visibility = View.INVISIBLE
        addButton.isEnabled = false

        fun callback(reply: Reply, onSuccess: () -> Unit = {}) {
            when (reply) {
                is Reply.Success -> onSuccess()
                is Reply.Failure -> toast(reply.errorMessage, Toast.LENGTH_LONG)
            }
            loadingPanel.visibility = View.INVISIBLE
            inputText.visibility = View.VISIBLE
            inputText.requestFocus()
            addButton.isEnabled = true
        }

        when (action) {
            Store.Actions.ToggleChecked -> {
                if (parameter !is Int) return callback(incorrectParameters)
                Store.toggleChecked(parameter) { callback(it, onSuccess) }
            }
            Store.Actions.AddItem -> {
                if (parameter !is String) return callback(incorrectParameters)
                Store.addItem(parameter) { callback(it, onSuccess) }
            }
        }
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val label: TextView = view.findViewById(R.id.list_item_label)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = Store.getItems()[position]
            holder.label.text = item.label
            setChecked(holder.label, item.checked)
            holder.label.setOnClickListener {
                callStore(Store.Actions.ToggleChecked, item.id) { this.notifyDataSetChanged() }
            }
        }

        private fun setChecked(textView: TextView, isChecked: Boolean) {
            if (isChecked) {
                textView.setTextColor(getColor(resources, R.color.lightgray, null))
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textView.setTextColor(getColor(resources, R.color.black, null))
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            }
        }

        override fun getItemCount(): Int {
            return Store.getItems().size
        }
    }
}