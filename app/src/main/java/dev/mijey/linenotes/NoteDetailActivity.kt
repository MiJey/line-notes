package dev.mijey.linenotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_note_detail.*

class NoteDetailActivity : AppCompatActivity() {

    companion object {
        const val EDIT_NOTE_REQUEST_CODE = 1234
        const val EDIT_NOTE_RESULT_CODE = 4321
    }

    private var createdTimestamp: Long = 0L
    private var isChanged = false   // 변경사항이 있을 때만 저장
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        createdTimestamp = intent.getLongExtra("timestamp", 0L)

        if (createdTimestamp == 0L) {
            // 새 노트
            isEditMode = true
            isChanged = true
        } else {
            // TODO 기존 노트 불러오기
            detail_title.setText("타이틀 $createdTimestamp")
            detail_text.setText("본문 $createdTimestamp")
            setLayout()
        }

        detail_tool_bar_action_button.setOnClickListener {
            isEditMode = !isEditMode

            if (isEditMode) {

            } else {
                // TODO 저장
            }

            setLayout()
        }
    }

    override fun onBackPressed() {
        if (isChanged) {
            AlertDialog.Builder(this)
                .setMessage(resources.getString(R.string.save_confirm))
                .setPositiveButton(resources.getString(R.string.save)) { dialogInterface, i ->
                    val modifiedTimestamp = System.currentTimeMillis()
                    val returnIntent = Intent()
                    // TODO Parcelable로 바꾸기
                    returnIntent.putExtra(
                        "createdTimestamp",
                        if (createdTimestamp == 0L) modifiedTimestamp else createdTimestamp
                    )
                    returnIntent.putExtra("modifiedTimestamp", modifiedTimestamp)
                    returnIntent.putExtra("title", detail_title.text.toString())
                    returnIntent.putExtra("text", detail_text.text.toString())
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
        } else {
            detail_tool_bar_action_button.text = resources.getString(R.string.edit)

            detail_title.isEnabled = false
            detail_text.isEnabled = false
        }
    }
}
