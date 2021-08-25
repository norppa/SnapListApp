package com.ducksoup.snaplist

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SnapListFragment : Fragment() {

    private lateinit var api: API

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
        if (token.isNullOrEmpty())
            return view.findNavController().navigate(R.id.loginFragment)


        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)

        api = API(view)
        api.getLists(token) { lists: List<Store.List> ->
            run {
                Store.setLists(lists)
                viewPager.adapter = PageAdapter(lists,this)
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    tab.text = lists[position].name
                }.attach()
            }
        }

    }

    private fun logout(view: View) {
        Token.setToken(null, requireActivity())
        view.findNavController().navigate(R.id.loginFragment)
    }
}