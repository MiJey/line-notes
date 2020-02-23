package dev.mijey.linenotes.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.mijey.linenotes.DateHelper
import dev.mijey.linenotes.Note
import dev.mijey.linenotes.R
import dev.mijey.linenotes.detail.NoteDetailActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.note_list_item.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class NoteListAdapter(private val mainActivity: MainActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mNotes: List<Note>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(mainActivity).inflate(R.layout.note_list_item, parent, false)
        return Item(mainActivity, v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as Item).bindData(mNotes?.get(position) ?: return, position)
    }

    inner class Item(var mainActivity: MainActivity, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindData(note: Note, pos: Int) {
            itemView.note_list_item_title.text = note.title
            itemView.note_list_item_text_preview.text = note.text

            if (note.imageList.isEmpty()) {
                itemView.note_list_item_thumbnail.visibility = View.GONE
            } else {
                itemView.note_list_item_thumbnail.setImageBitmap(note.imageList[0].getThumbnail(mainActivity))
                itemView.note_list_item_thumbnail.visibility = View.VISIBLE
            }

            val createdDate = DateHelper.dateString(note.createdTimestamp)
            val modifiedDate = DateHelper.dateString(note.modifiedTimestamp)

            itemView.note_list_item_date.text =
                "${mainActivity.resources.getString(R.string.modified_date)}: $modifiedDate / ${mainActivity.resources.getString(
                    R.string.created_date
                )}: $createdDate"

            if (mainActivity.isEditMode) {
                itemView.note_list_item_check.visibility = View.VISIBLE

                if (note.isSelected) {
                    itemView.note_list_item_check.background =
                        mainActivity.resources.getDrawable(R.drawable.bg_oval_color_accent, null)
                    itemView.note_list_item_check.setImageResource(R.drawable.ic_check_gray_lv1_24dp)
                } else {
                    itemView.note_list_item_check.background =
                        mainActivity.resources.getDrawable(R.drawable.bg_oval_stroke, null)
                    itemView.note_list_item_check.setImageResource(0)
                }

                itemView.note_list_item.setOnClickListener {
                    note.isSelected = !note.isSelected
                    notifyItemChanged(pos)
                }
            } else {
                itemView.note_list_item_check.visibility = View.GONE
                note.isSelected = false

                // 노트 편집
                itemView.setOnClickListener {
                    val detailIntent = Intent(mainActivity, NoteDetailActivity::class.java)
                    detailIntent.putExtra("note", note)
                    mainActivity.startActivityForResult(
                        detailIntent,
                        NoteDetailActivity.EDIT_NOTE_REQUEST_CODE
                    )
                }
            }
        }
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
}