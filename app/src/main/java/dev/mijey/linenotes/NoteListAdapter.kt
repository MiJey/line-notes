package dev.mijey.linenotes

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.note_list_item.view.*

class NoteListAdapter(private val mainActivity: MainActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mNotes: List<Note>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(mainActivity).inflate(R.layout.note_list_item, parent, false)
        return Item(mainActivity, v)
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun getItemCount(): Int {
        val cnt = mNotes?.size ?: 0
        if (cnt == 0) mainActivity.empty_view.visibility = View.VISIBLE
        else mainActivity.empty_view.visibility = View.GONE

        return cnt
    }

    fun setNotes(notes: List<Note>) {
        mNotes = notes
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as Item).bindData(mNotes?.get(position) ?: return, position)
    }

    inner class Item(var mainActivity: MainActivity, itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(note: Note, pos: Int) {
            itemView.note_list_item_title.text = "$pos ${note.title}"
            itemView.note_list_item_text_preview.text = note.text
            itemView.note_list_item_thumbnail

            if (mainActivity.isEditMode) {
                itemView.note_list_item_check.visibility = View.VISIBLE

                if (note.isSelected) {
                    itemView.note_list_item_check.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorAccent))
                } else {
                    itemView.note_list_item_check.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.colorPrimaryDark))
                }

                itemView.note_list_item.setOnClickListener {
                    note.isSelected = !note.isSelected
                    notifyItemChanged(pos)
                }
            } else {
                itemView.note_list_item_check.visibility = View.GONE
                note.isSelected = false

                itemView.setOnClickListener {
                    val detailIntent = Intent(mainActivity, NoteDetailActivity::class.java)
                    detailIntent.putExtra("timestamp", note.modifiedTimestamp)
                    mainActivity.startActivity(detailIntent)
                }
            }
        }
    }
}