package com.ducksoup.snaplist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter

class PageAdapter(private val lists: List<Store.List>, fragment: Fragment) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount() = lists.size

    override fun createFragment(position: Int): Fragment {
        return PageFragment(lists[position])
    }

}

class PageFragment(private val list: Store.List) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.sList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val token = Store().getToken(requireActivity())
        API(view).getItems(list.id, token) { recyclerView.adapter = ListAdapter(it)}

    }

}