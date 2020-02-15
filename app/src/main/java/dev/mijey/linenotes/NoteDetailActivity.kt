package dev.mijey.linenotes

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_note_detail.*
import java.text.SimpleDateFormat

class NoteDetailActivity : AppCompatActivity() {

    companion object {
        const val EDIT_NOTE_REQUEST_CODE = 1234
        const val EDIT_NOTE_RESULT_CODE = 4321
    }

    private var note: Note? = null

    private var isChanged = false   // 변경사항이 있을 때만 저장
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        note = intent.getParcelableExtra("note")
        val note = this.note ?: Note()

        if (note.createdTimestamp == 0L) {
            // 새 노트
            isEditMode = true
            isChanged = true
        } else {
            // 기존 노트 불러오기
            detail_title.setText(note.title)
            detail_text.setText(note.text)

            // TODO 이미지 불러오기

            val lastModifiedDate = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss").format(note.modifiedTimestamp) // DateFormat.getDateInstance(DateFormat.LONG).format(Date(note.modifiedTimestamp))
            detail_last_modified_date.text = "${resources.getString(R.string.last_modify)}: $lastModifiedDate"
        }

        detail_text.requestFocus()

        detail_tool_bar_action_button.setOnClickListener {
            isEditMode = !isEditMode
            setLayout()
        }

        detail_title.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true
                setLayout()
            }
        }

        detail_text.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true
                setLayout()
            }
        }

        detail_image.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true
                setLayout()
                // TODO 이미지는 누르면 바로 해당 이미지 편집
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setLayout()
    }

    override fun onBackPressed() {
        if (isChanged) {
            AlertDialog.Builder(this)
                .setMessage(resources.getString(R.string.save_confirm))
                .setPositiveButton(resources.getString(R.string.save)) { dialogInterface, i ->
                    val note = note ?: Note()
                    note.modifiedTimestamp = System.currentTimeMillis()
                    note.createdTimestamp = if (note.createdTimestamp == 0L) note.modifiedTimestamp else note.createdTimestamp
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

    private fun setLayout() {
        if (isEditMode) {
            detail_tool_bar_action_button.text = resources.getString(R.string.save)

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
            detail_tool_bar_action_button.text = resources.getString(R.string.edit)

            detail_title.isEnabled = false
            detail_text.isEnabled = false

            detail_last_modified_date.visibility = View.VISIBLE
        }
    }
}
