package dev.mijey.linenotes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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

        val bitmap = getBitmapRotated(context)
        val dp90 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90f, context.resources.displayMetrics).toInt()

        thumbnail = try {
            ThumbnailUtils.extractThumbnail(
                bitmap,
                dp90,
                dp90
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
        val bitmap = getBitmapRotated(context)
        val imageWidth = bitmap.width
        val imageHeight = bitmap.height

        // 높이가 200dp를 넘어가는 이미지는 리사이징
        // resizedWidth : resizedHeight(200dp) = imageWidth : imageHeight
        val dp200 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            200f,
            context.resources.displayMetrics
        ).toInt()
        val resizedHeight = if (imageHeight > dp200) dp200 else imageHeight
        val resizedWidth = (resizedHeight * imageWidth) / imageHeight
        Log.d(
            "yejiresize",
            "imageWidth: $imageWidth, imageHeight: $imageHeight, resizedHeight: $resizedHeight, resizedWidth: $resizedWidth"
        )

        bitmapImage = try {
            ThumbnailUtils.extractThumbnail(
                bitmap,
                resizedWidth,
                resizedHeight
            )
        } catch (e: Exception) {
            Log.d("yejithumbnail", "비트맵 이미지 가져오기 실패: $e")
            null
        }

        return bitmapImage
    }

    private fun getImagePath(context: Context): String =
        "${context.filesDir.path}/${note.createdTimestamp}/$name"

    private fun getBitmapRotated(context: Context): Bitmap {
        val imagePath = getImagePath(context)
        val image = BitmapFactory.decodeFile(imagePath)

        val exif = ExifInterface(imagePath)
        val exifOrientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val exifDegree = exifOrientationToDegrees(exifOrientation)

        return rotate(image, exifDegree)
    }

    private fun exifOrientationToDegrees(exifOrientation: Int): Float {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        var bitmap = bitmap

        if (degrees != 0f) {
            val m = Matrix()
            m.setRotate(degrees, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
            try {
                val converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                if (bitmap != converted) {
                    bitmap.recycle()
                    bitmap = converted
                }
            } catch (ex: OutOfMemoryError) {
                // 회전에 실패하는 경우 원본 반환
            }
        }
        
        return bitmap
    }
}