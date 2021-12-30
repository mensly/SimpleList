package cat.balrog.simplelist

import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView

private const val KEY_LIST = "list"

class ListAdapter : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView by lazy { itemView.findViewById<TextView>(android.R.id.text1) }

        init {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }

        fun updateIndex(index: Int) {
            if (index < 0) {
                textView.text = textView.resources.getString(R.string.heading)
                textView.setTextColor(Color.WHITE)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            } else {
                textView.text = items[index]
                textView.setTextColor(if (index == selected) Color.WHITE else Color.GRAY)
            }
        }
    }

    private val items = mutableListOf<String>()
    private var selected = 0

    fun loadItems(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        items.clear()
        prefs.getString(KEY_LIST, null)
            ?.split("\n")
            ?.filter(String::isNotBlank)
            ?.forEach(items::add)
    }

    fun saveItems(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit {
            putString(KEY_LIST, items.joinToString("\n"))
        }
    }

    fun addItem(item: String) {
        items.add(item)
        if (selected < 0) {
            selected = 0
        }
        notifyDataSetChanged() // TODO: Be more specific
    }

    fun removeSelected() {
        val selected = this.selected
        if (selected < 0 || selected >= items.size) {
            return
        }
        items.removeAt(selected)
        if (selected >= items.size) {
            this.selected = items.size - 1
        }
        notifyDataSetChanged()
    }

    fun selectionUp(): Int {
        val selected = this.selected
        if (selected > 0) {
            this.selected = selected - 1
            notifyDataSetChanged()
        }
        return this.selected + 1
    }

    fun selectionDown(): Int {
        val selected = this.selected
        if (selected < items.size - 1) {
            this.selected = selected + 1
            notifyDataSetChanged()
        }
        return this.selected + 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.updateIndex(position - 1)
    }

    override fun getItemCount() = items.size + 1

    override fun getItemViewType(position: Int) = if (position == 0) 0 else 1
}