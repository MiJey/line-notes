package dev.mijey.linenotes.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dev.mijey.linenotes.FileIOHelper
import dev.mijey.linenotes.Note
import dev.mijey.linenotes.R
import kotlinx.android.synthetic.main.activity_note_detail.*
import kotlinx.android.synthetic.main.dialog_add_image.view.*
import java.text.SimpleDateFormat

class NoteDetailActivity : AppCompatActivity() {

    companion object {
        const val EDIT_NOTE_REQUEST_CODE = 1234
        const val EDIT_NOTE_RESULT_CODE = 4321

        const val GALLERY_REQUEST_CODE = 2222
    }

    private var note: Note? = null
    var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        note = intent.getParcelableExtra("note")
        val note = this.note ?: Note()

        if (note.createdTimestamp == 0L) {
            // 새 노트
            isEditMode = true
        } else {
            // 기존 노트 불러오기
            detail_title.setText(note.title)
            detail_text.setText(note.text)

            // TODO 이미지 불러오기
        }

        detail_image_list.adapter = ImageListAdapter(this, note.images)

        detail_text.requestFocus()

        detail_tool_bar_add_image.setOnClickListener {
            // TODO BottomSheetDialog로 변경
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_image, null)
            val dialog = android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            // 카메라
            dialogView.add_image_from_camera.setOnClickListener {

            }

            // 갤러리
            dialogView.add_image_from_gallery.setOnClickListener {
                val galleryIntent = Intent()
                galleryIntent.type = "image/*"
                galleryIntent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(galleryIntent, ""), GALLERY_REQUEST_CODE)

                dialog.dismiss()
            }

            // 외부 링크
            dialogView.add_image_from_link.setOnClickListener {

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
    }

    override fun onResume() {
        super.onResume()
        setLayout()
    }

    override fun onBackPressed() {
        val isChanged = true
        // TODO 디비랑 비교해서 바뀐게 있는지 확인

        if (isChanged) {
            AlertDialog.Builder(this)
                .setMessage(resources.getString(R.string.save_confirm))
                .setPositiveButton(resources.getString(R.string.save)) { dialogInterface, i ->
                    val note = note ?: Note()
                    note.modifiedTimestamp = System.currentTimeMillis()
                    note.createdTimestamp =
                        if (note.createdTimestamp == 0L) note.modifiedTimestamp else note.createdTimestamp
                    note.title = detail_title.text.toString()
                    note.text = detail_text.text.toString()

                    val returnIntent = Intent()
                    returnIntent.putExtra("note", note)
                    setResult(EDIT_NOTE_RESULT_CODE, returnIntent)

                    super.onBackPressed()
                }
                .setNegativeButton(resources.getString(R.string.not_save)) { dialogInterface, i ->
                    super.onBackPressed()
                }
                .setNeutralButton(resources.getString(R.string.cancel)) { dialogInterface, i ->
                }
                .create()
                .show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 갤러리에서 이미지 가져오기 결과
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data ?: return
            val imageFileName = FileIOHelper.getFileNameFromUri(this, uri) ?: return
            val dstPath = "${filesDir.path}/${note?.createdTimestamp ?: return}"
            Log.d("yejiuri", "onActivityResult\n" +
                    "data: $data\n" +
                    "uri: $uri\n" +
                    "dstPath: $dstPath\n" +
                    "imageFileName: $imageFileName")

            Thread {
                Log.d("yejiuri", "이미지 복사 시작: ${System.currentTimeMillis()}")
                val result = FileIOHelper.copyFile(this, uri, dstPath, imageFileName)
                Log.d("yejiuri", "이미지 복사 끝: ${System.currentTimeMillis()}, result: $result")

                if (result) {
                    // TODO 이미지 복사 완료
                    runOnUiThread {
                        val images = note?.images ?: return@runOnUiThread
                        images.add(imageFileName)
                        detail_image_list.adapter?.notifyItemChanged(images.size - 1)
                    }
                }
            }.start()
        }
    }

    private fun setLayout() {
        if (isEditMode) {
            detail_tool_bar_action_button.setImageResource(R.drawable.ic_save_white_24dp)
            if (Build.VERSION.SDK_INT >= 26)
                detail_tool_bar_action_button.tooltipText = resources.getString(R.string.save)

            detail_title.isEnabled = true
            detail_text.isEnabled = true

            detail_last_modified_date.visibility = View.GONE

            // 포커스 주고 키보드 자동으로 올리기
            detail_text.postDelayed({
                detail_text.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(detail_text, InputMethodManager.SHOW_IMPLICIT)
            }, 30)
        } else {
            detail_tool_bar_action_button.setImageResource(R.drawable.ic_edit_white_24dp)
            if (Build.VERSION.SDK_INT >= 26)
                detail_tool_bar_action_button.tooltipText = resources.getString(R.string.edit)

            detail_title.isEnabled = false
            detail_text.isEnabled = false

            val lastModifiedDate =
                SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss").format(note?.modifiedTimestamp) // DateFormat.getDateInstance(DateFormat.LONG).format(Date(note.modifiedTimestamp))
            detail_last_modified_date.text =
                "${resources.getString(R.string.last_modify)}: $lastModifiedDate"
            detail_last_modified_date.visibility = View.VISIBLE
        }
    }
}
