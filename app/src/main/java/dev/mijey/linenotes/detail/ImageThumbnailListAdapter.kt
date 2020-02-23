package dev.mijey.linenotes.detail

import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.mijey.linenotes.Note
import dev.mijey.linenotes.NoteImage
import dev.mijey.linenotes.R
import kotlinx.android.synthetic.main.activity_note_detail.*
import kotlinx.android.synthetic.main.image_thumbnail_list_item.view.*


class ImageThumbnailListAdapter(
    private val noteDetailActivity: NoteDetailActivity,
    private val note: Note
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(noteDetailActivity)
            .inflate(R.layout.image_thumbnail_list_item, parent, false)
        return Item(noteDetailActivity, v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as Item).bindData(note.imageList[position], position)
    }

    inner class Item(var noteDetailActivity: NoteDetailActivity, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindData(image: NoteImage, pos: Int) {
            itemView.image_thumbnail_list_item_selected.visibility = if (image.isSelected) View.VISIBLE else View.GONE

            Thread {
                val bitmap = image.getBitmapImage(noteDetailActivity)
                android.os.Handler(Looper.getMainLooper()).post {
                    itemView.image_thumbnail_list_item_image.setImageBitmap(bitmap)
                }
            }.start()

            if (noteDetailActivity.isEditMode) {

            }

            itemView.image_thumbnail_list_item.setOnClickListener {
                Log.d("yejithumbnail", "썸네일 클릭 pos: $pos")
                noteDetailActivity.imageScrollTo(pos)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun getItemCount(): Int = note.imageList.size
}