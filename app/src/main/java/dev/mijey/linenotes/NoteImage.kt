package dev.mijey.linenotes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.util.Log
import android.util.TypedValue
import java.lang.Exception

class NoteImage(val note: Note, val name: String) {
    fun getImagePath(context: Context): String = "${context.filesDir.path}/${note.createdTimestamp}/$name"

    fun getThumbnail(context: Context, thumbnailSizeDP: Float): Bitmap? {
        val thumbnailSizePX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, thumbnailSizeDP, context.resources.displayMetrics).toInt()

        return try {
            ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(getImagePath(context)),
                thumbnailSizePX, thumbnailSizePX
            )
        } catch (e: Exception) {
            Log.d("yejithumbnail", "썸네일 만들기 실패: $e")
            null
        }
    }
}