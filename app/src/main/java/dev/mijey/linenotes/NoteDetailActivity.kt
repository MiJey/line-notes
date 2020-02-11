package dev.mijey.linenotes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_note_detail.*

class NoteDetailActivity : AppCompatActivity() {
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        val timestamp = intent.getLongExtra("timestamp", 0L)

        // TODO 파일 찾기
        // TODO 파일 못찾으면 파일을 찾을 수 없습니다 띄우고 finish()
        if (timestamp == 0L)
            isEditMode = true

        detail_title.setText("타이틀 $timestamp")
        detail_text.setText("본문 $timestamp")
        setLayout()

        detail_tool_bar_action_button.setOnClickListener {
            isEditMode = !isEditMode

            if (isEditMode) {

            } else {
                // TODO 저장
            }

            setLayout()
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
