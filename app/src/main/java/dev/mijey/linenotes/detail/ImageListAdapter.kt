package dev.mijey.linenotes.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.mijey.linenotes.R
import kotlinx.android.synthetic.main.image_list_item.view.*

class ImageListAdapter(private val noteDetailActivity: NoteDetailActivity, private val images: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(noteDetailActivity).inflate(R.layout.image_list_item, parent, false)
        return Item(noteDetailActivity, v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as Item).bindData(images[position], position)
    }

    inner class Item(var noteDetailActivity: NoteDetailActivity, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindData(image: String, pos: Int) {
            itemView.image_list_item_path.text = image

            if (noteDetailActivity.isEditMode) {
                itemView.image_list_item_delete.visibility = View.VISIBLE

                itemView.image_list_item_delete.setOnClickListener {
                    // TODO 해당 이미지 삭제
                }
            } else {
                itemView.image_list_item_delete.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun getItemCount(): Int = images.size
}