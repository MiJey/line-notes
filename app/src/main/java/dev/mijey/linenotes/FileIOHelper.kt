package dev.mijey.linenotes

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object FileIOHelper {
    // 갤러리에서 불러온 이미지 파일 이름 추출하기
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> {
                fileName = File(uri.path ?: return null).name
            }
            ContentResolver.SCHEME_CONTENT -> {
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.apply {
                        if (moveToFirst()) {
                            fileName = getString(getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        }
                        close()
                    }
                } catch (e: Exception) {
                    return null
                }
            }
        }
        return fileName
    }

    // 갤러리에서 선택한 이미지 내부 저장소로 복사하기
    fun copyFile(context: Context?, sourceUri: Uri?, dstPath: String?, dstName: String?): Boolean {
        context ?: return false
        sourceUri ?: return false
        dstPath ?: return false
        dstName ?: return false

        try {
            // 폴더 생성
            val myFile = File(dstPath, dstName)
            if (!myFile.exists()) {
                File(dstPath).mkdirs()
                if (!myFile.createNewFile()) return false
            }

            // 파일 복사
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return false
            val out = FileOutputStream(File(dstPath, dstName))
            val buffer = ByteArray(1024)

            while (true) {
                val size = inputStream.read(buffer)
                if (size < 0) break
                out.write(buffer, 0, size)
            }
            out.close()

            return true // 파일 저장 성공
        } catch (e: Exception) {
        }

        return false    // 파일 저장 실패
    }
}