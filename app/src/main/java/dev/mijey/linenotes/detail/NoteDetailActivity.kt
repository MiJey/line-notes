package dev.mijey.linenotes.detail

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import dev.mijey.linenotes.*
import kotlinx.android.synthetic.main.activity_note_detail.*
import kotlinx.android.synthetic.main.dialog_add_image.view.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection

/**
 * TODO 제목, 메모 둘 다 비어있으면 저장하지 않기
 * TODO 이미지 처음 가져올 때 썸네일 강조하기
 * TODO 이 메모 삭제
 * TODO 이미지 단독으로 확대해서 보기
 */

class NoteDetailActivity : AppCompatActivity() {

    companion object {
        const val EDIT_NOTE_REQUEST_CODE = 1234
        const val EDIT_NOTE_RESULT_CODE = 4321

        const val GALLERY_REQUEST_CODE = 2222
        const val CAMERA_REQUEST_CODE = 3333
    }

    private var originNote: Note? = null
    private var editingNote: Note? = null
    private var tempImageFileFromCamera: File? = null
    private var isLocked = false    // 연속 이미지 삭제 방지, 뒤로가기때 저장 꼬이는 것 방지용

    var currentImagePosition = 0
    var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        // 기존 임시 폴더 삭제
        File(filesDir.path, "0").delete()

        originNote = intent.getParcelableExtra("note")
        editingNote = originNote?.copy() ?: Note()
        val editingNote = editingNote ?: return

        if (editingNote.createdTimestamp == 0L) {
            // 새 노트
            isEditMode = true
        } else {
            // 기존 노트 불러오기
            detail_title.setText(editingNote.title)
            detail_text.setText(editingNote.text)

            if (editingNote.imageList.isNotEmpty())
                editingNote.imageList[0].isSelected = true
        }

        detail_image_list.hasFixedSize()
        detail_image_list.adapter = ImageListAdapter(this, editingNote)
        detail_image_thumbnail_list.adapter = ImageThumbnailListAdapter(this, editingNote)

        val imageSnapHelper = LinearSnapHelper()
        imageSnapHelper.attachToRecyclerView(detail_image_list)

        val thumbnailSnapHelper = LinearSnapHelper()
        thumbnailSnapHelper.attachToRecyclerView(detail_image_thumbnail_list)

        // 현재 이미지 썸네일 강조하기
        detail_image_list.addOnScrollListener(object  : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val view =  imageSnapHelper.findSnapView(detail_image_list.layoutManager) ?: return
                    val newPosition = recyclerView.getChildAdapterPosition(view)
                    imageScrollTo(newPosition)
                }
            }
        })

        // 이미지 추가하기
        detail_tool_bar_add_image.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_image, null)
            val dialog = android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            // 카메라 지원 여부 확인
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                dialogView.add_image_from_camera.setOnClickListener {
                    // 카메라 앱 실행
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(packageManager)?.also {
                            try {
                                tempImageFileFromCamera = File.createTempFile("JPEG_", ".jpg", editingNote.getDirectory(this))
                            } catch (e: Exception) {
                                Log.d("yejicamera", "사진 파일 생성 실패: $e")
                                Toast.makeText(this, resources.getString(R.string.fail_image), Toast.LENGTH_SHORT).show()
                            }

                            tempImageFileFromCamera?.also {
                                val photoURI: Uri = FileProvider.getUriForFile(
                                    this,
                                    "com.example.android.fileprovider",
                                    it
                                )
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                            }
                        }
                    }

                    dialog.dismiss()
                }
            } else {
                // 카메라를 지원하지 않는 경우
                dialogView.add_image_from_camera.visibility = View.GONE
            }


            // 갤러리
            dialogView.add_image_from_gallery.setOnClickListener {
                val galleryIntent = Intent()
                galleryIntent.type = "image/*"
                galleryIntent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(galleryIntent, ""),
                    GALLERY_REQUEST_CODE
                )

                dialog.dismiss()
            }

            // 외부 링크
            dialogView.add_image_from_link.setOnClickListener {
                val urlString = dialogView.add_image_url.text.toString()

                if (urlString.isEmpty()) {
                    Toast.makeText(this, resources.getString(R.string.link_hint), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (isWifiConnected(this) == null) {
                    // 네트워크 없음
                    Toast.makeText(this, resources.getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val imageName = System.currentTimeMillis().toString()
                writePNGFileFromURL(editingNote.getDirectoryPath(this), imageName, urlString)
                Toast.makeText(this, resources.getString(R.string.load_image), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            dialog.show()
        }

        // 편집, 저장
        detail_tool_bar_action_button.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true
                setLayout()
            } else {
                // 저장하기 위해 액티비티 닫기
                onBackPressed()
            }
        }

        detail_title.setOnClickListener {
            isEditMode = true
            setLayout()
        }

        detail_text.setOnClickListener {
            isEditMode = true
            setLayout()
        }

        detail_scroll_view.setOnClickListener {
            isEditMode = true
            setLayout()
        }

        // 첫 클릭은 포커스만 가져가서 onClick 리스너는 안불리고 키보드만 올라옴
        // -> 포커스 가져오면 onClick 리스너 호출해서 편집 모드로 전환
        detail_text.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.callOnClick()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setLayout()
    }

    override fun onBackPressed() {
        // 저장 꼬이는 것 방지
        if (isLocked) return
        isLocked = true

        editingNote?.title = detail_title.text.toString()
        editingNote?.text = detail_text.text.toString()

        if (isChanged()) {
            AlertDialog.Builder(this)
                .setMessage(resources.getString(R.string.save_confirm))
                .setPositiveButton(resources.getString(R.string.save)) { dialogInterface, i ->
                    editingNote?.modifiedTimestamp = System.currentTimeMillis()

                    if (editingNote?.createdTimestamp == 0L) {
                        // 새 노트 저장
                        editingNote?.createdTimestamp = editingNote?.modifiedTimestamp ?: 0L
                        editingNote?.createDirectoryFromTemp(this)
                    }

                    editingNote?.syncImageFileList(this)

                    val returnIntent = Intent()
                    returnIntent.putExtra("note", editingNote)
                    setResult(EDIT_NOTE_RESULT_CODE, returnIntent)

                    super.onBackPressed()
                }
                .setNegativeButton(resources.getString(R.string.not_save)) { dialogInterface, i ->
                    super.onBackPressed()
                }
                .setNeutralButton(resources.getString(R.string.cancel)) { dialogInterface, i ->
                    // 다이얼로그만 닫고 아무것도 안함
                }
                .setOnDismissListener {
                    isLocked = false
                }
                .create()
                .show()
        } else {
            isLocked = false

            if (isEditMode) {
                isEditMode = false
                setLayout()
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    val note = editingNote ?: return
                    val imageName = tempImageFileFromCamera?.name ?: return
                    note.imageList.add(NoteImage(note, imageName))
                    imageScrollTo(note.imageList.size - 1)
                } else {
                    Log.d("yejicamera", "카메라로 이미지 가져오기 실패 또는 취소")
                    Toast.makeText(this, resources.getString(R.string.cancel_image), Toast.LENGTH_SHORT).show()
                }
            }

            GALLERY_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    // 갤러리에서 이미지 가져오기 성공
                    val uri: Uri = data?.data ?: return
                    val imageFileName = FileIOHelper.getFileNameFromUri(this, uri) ?: return
                    val dstPath = editingNote?.getDirectoryPath(this)

                    Thread {
                        if (FileIOHelper.copyFile(this, uri, dstPath, imageFileName)) {
                            // 이미지 복사 완료
                            runOnUiThread {
                                val note = editingNote ?: return@runOnUiThread
                                note.imageList.add(NoteImage(note, imageFileName))
                                imageScrollTo(note.imageList.size - 1)
                                Log.d("yejigallery", "이미지 복사 완료: ${System.currentTimeMillis()}, note: $note")
                            }
                        } else {
                            Log.d("yejigallery", "갤러리에서 이미지 가져오고 내부저장소로 복사 실패")
                            Toast.makeText(this, resources.getString(R.string.fail_image), Toast.LENGTH_SHORT).show()
                        }
                    }.start()
                } else {
                    Log.d("yejigallery", "갤러리에서 이미지 가져오기 실패 또는 취소")
                    Toast.makeText(this, resources.getString(R.string.cancel_image), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun imageScrollTo(position: Int) {
        val imageList = editingNote?.imageList ?: return
        val newPosition = when {
            position < 0 -> 0
            position >= imageList.size -> imageList.size - 1
            else -> position
        }

        var beforePosition = currentImagePosition
        currentImagePosition = newPosition

        beforePosition = when {
            beforePosition < 0 -> 0
            beforePosition >= imageList.size -> imageList.size - 1
            else -> beforePosition
        }

        imageList[beforePosition].isSelected = false
        imageList[currentImagePosition].isSelected = true

        detail_image_thumbnail_list.adapter?.notifyItemChanged(beforePosition)
        detail_image_thumbnail_list.adapter?.notifyItemChanged(currentImagePosition)

        detail_image_list.scrollToPosition(currentImagePosition)
        detail_image_thumbnail_list.scrollToPosition(currentImagePosition)
    }

    fun imageRemoveAt(position: Int) {
        if (isLocked) return
        isLocked = true

        // 해당 이미지 삭제
        try {
            editingNote?.imageList?.removeAt(position)
            detail_image_list.adapter?.notifyItemRemoved(position)
            detail_image_thumbnail_list.adapter?.notifyItemRemoved(position)
            imageScrollTo(position)
        } catch (e: java.lang.Exception) {
            Log.d("yejiimage", "이미지 삭제 오류: $e")
            detail_image_list.adapter?.notifyDataSetChanged()
            detail_image_thumbnail_list.adapter?.notifyDataSetChanged()
        }

        isLocked = false
    }

    /*********************************************************************************************/

    private fun setLayout() {
        if (isEditMode) {
            detail_tool_bar_action_button.setImageResource(R.drawable.ic_save_white_24dp)
            if (Build.VERSION.SDK_INT >= 26)
                detail_tool_bar_action_button.tooltipText = resources.getString(R.string.save)

            detail_title.isCursorVisible = true
            detail_text.isCursorVisible = true
            detail_last_modified_date.visibility = View.GONE
        } else {
            detail_tool_bar_action_button.setImageResource(R.drawable.ic_edit_white_24dp)
            if (Build.VERSION.SDK_INT >= 26)
                detail_tool_bar_action_button.tooltipText = resources.getString(R.string.edit)

            detail_title.isCursorVisible = false
            detail_text.isCursorVisible = false

            val lastModifiedDate = DateHelper.dateString(editingNote?.modifiedTimestamp ?: return)
            detail_last_modified_date.text = "${resources.getString(R.string.last_modify)}: $lastModifiedDate"
            detail_last_modified_date.visibility = View.VISIBLE
        }

        detail_image_list.adapter?.notifyDataSetChanged()
        detail_image_thumbnail_list.adapter?.notifyDataSetChanged()
    }

    // URL로 이미지 불러와서 PNG로 저장하기
    private fun writePNGFileFromURL(path: String, filename: String, url: String) {
        // 폴더 생성
        val myFile = File(path)
        if (!myFile.exists()) {
            File(path).mkdirs()
            if (!myFile.createNewFile()) {
                Log.d("yejiurl", "URL로 이미지 불러오기 폴더 생성 실패")
                Toast.makeText(this, resources.getString(R.string.fail_image), Toast.LENGTH_SHORT).show()
                return
            }
        }

        Thread {
            try {
                val connection = java.net.URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()

                val inputStream = connection.inputStream
                val outputStream = FileOutputStream("$path/$filename.png")
                BitmapFactory.decodeStream(inputStream).compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                runOnUiThread {
                    Log.d("yejiurl", "URL로 이미지 불러오기 성공!: $filename, note.imageList.size: ${editingNote?.imageList?.size}")
                    val note = editingNote ?: return@runOnUiThread
                    note.imageList.add(NoteImage(note, "$filename.png"))
                    imageScrollTo(note.imageList.size - 1)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Log.d("yejiurl", "URL로 이미지 불러오기 실패: $e")
                    Toast.makeText(this, resources.getString(R.string.fail_image), Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // 와이파이 연결 여부(null: 네트워크 없음, false: 와이파이 연결안됨, true: 와이파이 연결됨)
    private fun isWifiConnected(context: Context): Boolean? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return null
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null

            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            return if (!connectivityManager.activeNetworkInfo.isConnected) null
            else connectivityManager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }

    // 원본과 비교해서 바뀐게 있는지 확인
    private fun isChanged(): Boolean {
        val originNote = originNote ?: return true
        val editingNote = editingNote ?: return true

        if (editingNote.title != originNote.title)
            return true

        if (editingNote.text != originNote.text)
            return true

        if (editingNote.imageList.size != originNote.imageList.size)
            return true

        if (editingNote.imageList.isNotEmpty()) {
            for (i in 0 until editingNote.imageList.size) {
                if (editingNote.imageList[i].name != originNote.imageList[i].name)
                    return true
            }
        }

        return false
    }
}
