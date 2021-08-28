package com.ducksoup.snaplist

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete_checked -> {
                Store.deleteChecked() { refresh() }
                true
            }

            R.id.menu_delete_all -> {
                Store.deleteAll { refresh() }
                true
            }
            R.id.menu_logout -> {
                logout(requireView())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val token = Token.getToken(requireActivity())
        if (token.isEmpty())
            return view.findNavController().navigate(R.id.loginFragment)

        tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        recyclerView = view.findViewById<RecyclerView>(R.id.list)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val activeListPosition = tab?.position ?: 0
                Store.setActiveList(activeListPosition) { refresh() }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ListAdapter()

        Store.fetchLists {
            Store.lists.forEach { list ->
                tabLayout.addTab(tabLayout.newTab().setText(list.name).setId(list.id))
            }
            Store.fetchItems { refresh() }
        }

    }

    private fun refresh() {
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun logout(view: View) {
        Token.setToken(null, requireActivity())
        view.findNavController().navigate(R.id.loginFragment)
    }
}