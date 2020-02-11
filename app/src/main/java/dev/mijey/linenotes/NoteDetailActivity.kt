package dev.mijey.linenotes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_note_detail.*

class NoteDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        val timestamp = intent.getLongExtra("timestamp", 0L)

        // TODO 파일 찾기

        detail_title.text = "타이틀 $timestamp"
        detail_text.text = "본문 $timestamp"
    }
}
