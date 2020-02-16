package dev.mijey.linenotes.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.mijey.linenotes.Note
import dev.mijey.linenotes.R
import dev.mijey.linenotes.detail.NoteDetailActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.note_list_item.view.*
import java.text.SimpleDateFormat

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
            itemView.note_list_item_title.text = "$pos ${note.title}"
            itemView.note_list_item_text_preview.text = note.text
            itemView.note_list_item_thumbnail

            // TODO 오늘 변경사항은 날짜 말고 시간으로 보여주기
            val createdDate =
                SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss").format(note.createdTimestamp) // DateFormat.getDateInstance(DateFormat.LONG).format(Date(note.createdTimestamp))
            val modifiedDate =
                SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss").format(note.modifiedTimestamp) // DateFormat.getDateInstance(DateFormat.LONG).format(Date(note.modifiedTimestamp))
            itemView.note_list_item_date.text =
                "${mainActivity.resources.getString(R.string.modified_date)}: $modifiedDate / ${mainActivity.resources.getString(
                    R.string.created_date
                )}: $createdDate"

            if (mainActivity.isEditMode) {
                itemView.note_list_item_check.visibility = View.VISIBLE

                if (note.isSelected) {
                    itemView.note_list_item_check.setBackgroundColor(
                        ContextCompat.getColor(
                            mainActivity,
                            R.color.colorAccent
                        )
                    )
                } else {
                    itemView.note_list_item_check.setBackgroundColor(
                        ContextCompat.getColor(
                            mainActivity,
                            R.color.colorPrimaryDark
                        )
                    )
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
                    mainActivity.startActivityForResult(detailIntent, NoteDetailActivity.EDIT_NOTE_REQUEST_CODE)
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