package dev.mijey.linenotes.detail

import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.mijey.linenotes.Note
import dev.mijey.linenotes.NoteImage
import dev.mijey.linenotes.R
import kotlinx.android.synthetic.main.image_list_item.view.*


class ImageListAdapter(
    private val noteDetailActivity: NoteDetailActivity,
    private val note: Note
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(noteDetailActivity)
            .inflate(R.layout.image_list_item, parent, false)
        return Item(noteDetailActivity, v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as Item).bindData(note.imageList[position], position)
    }

    inner class Item(var noteDetailActivity: NoteDetailActivity, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindData(image: NoteImage, pos: Int) {
            itemView.image_list_item.width.toFloat()

            Thread {
                val bitmap = image.getBitmapImage(noteDetailActivity)
                android.os.Handler(Looper.getMainLooper()).post {
                    itemView.image_list_item_image.setImageBitmap(bitmap)
                }
            }.start()

            if (noteDetailActivity.isEditMode) {
                itemView.image_list_item_delete.visibility = View.VISIBLE

                itemView.image_list_item_delete.setOnClickListener {
                    noteDetailActivity.imageRemoveAt(pos)
                }
            } else {
                itemView.image_list_item_delete.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun getItemCount(): Int = note.imageList.size
}