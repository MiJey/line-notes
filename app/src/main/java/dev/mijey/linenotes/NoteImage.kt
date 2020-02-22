package dev.mijey.linenotes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.util.Log
import android.util.TypedValue


class NoteImage(val note: Note, val name: String) {
    var isSelected = false

    private var thumbnail: Bitmap? = null
    private var bitmapImage: Bitmap? = null

    fun getThumbnail(context: Context): Bitmap? {
        if (thumbnail != null)
            return thumbnail

        val thumbnailSizePX = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            90f,
            context.resources.displayMetrics
        ).toInt()

        thumbnail = try {
            ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(getImagePath(context)),
                thumbnailSizePX,
                thumbnailSizePX
            )
        } catch (e: Exception) {
            Log.d("yejithumbnail", "썸네일 만들기 실패: $e")
            null
        }

        return thumbnail
    }

    fun getBitmapImage(context: Context): Bitmap? {
        if (bitmapImage != null)
            return bitmapImage

        // 원본 이미지 크기
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(getImagePath(context), options)
        val imageWidth = options.outWidth
        val imageHeight = options.outHeight

        // 높이가 200dp를 넘어가는 이미지는 리사이징
        // resizedWidth : resizedHeight(200dp) = imageWidth : imageHeight
        val dp200 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, context.resources.displayMetrics).toInt()
        val resizedHeight = if (imageHeight > dp200) dp200 else imageHeight
        val resizedWidth = (resizedHeight * imageWidth) / imageHeight
        Log.d("yejiresize", "imageWidth: $imageWidth, imageHeight: $imageHeight, resizedHeight: $resizedHeight, resizedWidth: $resizedWidth")

        bitmapImage = try {
            ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(getImagePath(context)),
                resizedWidth,
                resizedHeight
            )
        } catch (e: Exception) {
            Log.d("yejithumbnail", "비트맵 이미지 가져오기 실패: $e")
            null
        }

        return bitmapImage
    }

    private fun getImagePath(context: Context): String = "${context.filesDir.path}/${note.createdTimestamp}/$name"
}