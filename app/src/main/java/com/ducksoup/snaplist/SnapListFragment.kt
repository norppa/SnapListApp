package com.ducksoup.snaplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SnapListFragment : Fragment() {

    private lateinit var api: API
    private lateinit var store: Store

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_snap_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        store = Store()
        val token = store.getToken(requireActivity())
        if (token.isNullOrEmpty())
            return view.findNavController().navigate(R.id.loginFragment)


        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)

        api = API(view)
        api.getLists(token) { lists: List<Store.List> ->
            run {
                store.setLists(lists)
                viewPager.adapter = PageAdapter(lists,this)
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    tab.text = lists[position].name
                }.attach()
            }
        }

        view.findViewById<Button>(R.id.button).setOnClickListener { logout(it) }
    }

    private fun logout(view: View) {
        store.setToken(null, requireActivity())
        view.findNavController().navigate(R.id.loginFragment)
    }
}