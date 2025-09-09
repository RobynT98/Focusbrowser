package com.conri.focusbrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.conri.focusbrowser.databinding.ActivityTabsBinding
import com.conri.focusbrowser.databinding.ItemTabBinding

class TabsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTabsBinding
    private lateinit var adapter: TabsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TabsAdapter { index ->
            // switch to tab
            TabManager.switchTo((MainActivity@this).findViewById(R.id.tab_container), index)
            finish()
        }
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = adapter

        // Swipe to remove, drag to reorder
        val helper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = vh.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                TabManager.move(from, to)
                adapter.notifyItemMoved(from, to)
                return true
            }
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                val idx = vh.bindingAdapterPosition
                val container = findViewById<ViewGroup>(R.id.tab_container)
                TabManager.close(container as ViewGroup as android.widget.FrameLayout, idx)
                adapter.notifyItemRemoved(idx)
            }
        })
        helper.attachToRecyclerView(binding.list)
    }

    inner class TabsAdapter(val onClick: (Int) -> Unit): RecyclerView.Adapter<TabsAdapter.VH>() {
        inner class VH(val b: ItemTabBinding): RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
            val b = ItemTabBinding.inflate(LayoutInflater.from(p.context), p, false)
            return VH(b)
        }
        override fun onBindViewHolder(h: VH, pos: Int) {
            val tab = TabManager.list()[pos]
            h.b.title.text = tab.title
            h.b.url.text = tab.url
            h.b.root.setOnClickListener { onClick(pos) }
        }
        override fun getItemCount() = TabManager.list().size
    }
}
