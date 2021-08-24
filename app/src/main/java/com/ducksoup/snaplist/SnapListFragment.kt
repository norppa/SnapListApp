package com.ducksoup.snaplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SnapListFragment : Fragment() {

    private lateinit var token: Token
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
        token = Token(requireActivity())
        if (token.get().isNullOrEmpty())
            return view.findNavController().navigate(R.id.loginFragment)

        val list = view.findViewById<RecyclerView>(R.id.sList)
        list.layoutManager = LinearLayoutManager(context)

        api = API(view)
        store = Store()
        api.getLists(token) { lists: List<Store.List> ->
            run {
                store.setLists(lists)
                if (lists.isNotEmpty()) {
                    val firstListId = lists[0].id
                    api.getItems(firstListId, token) { items ->
                        run {
                            store.setItems(firstListId, items)
                            list.adapter = ListAdapter(items)
                        }
                    }
                }
            }
        }



        view.findViewById<Button>(R.id.button).setOnClickListener { logout(it) }
    }

    private fun logout(view: View) {
        token.set(null)
        view.findNavController().navigate(R.id.loginFragment)
    }
}