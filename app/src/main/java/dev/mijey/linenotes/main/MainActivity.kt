package dev.mijey.linenotes.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dev.mijey.linenotes.*
import dev.mijey.linenotes.detail.NoteDetailActivity
import kotlinx.android.synthetic.main.activity_main.*


/**
 * 기능 요구사항
 *
 * 기능1: 메모리스트
 *   1. 로컬 영역에 저장된 메모를 읽어 리스트 형태로 화면에 표시합니다.
 *   2. 리스트에는 메모에 첨부되어있는 이미지의 썸네일, 제목, 글의 일부가 보여집니다. (이미지가 n개일 경우, 첫 번째 이미지가 썸네일이 되어야 함)
 *   3. 리스트의 메모를 선택하면 메모 상세 보기 화면으로 이동합니다.
 *   4. 새 메모 작성하기 기능을 통해 메모 작성 화면으로 이동할 수 있습니다.
 *
 * 기능2: 메모 상세 보기
 *   1. 작성된 메모의 제목과 본문을 볼 수 있습니다.
 *   2. 메모에 첨부되어있는 이미지를 볼 수 있습니다. (이미지는 n개 존재 가능)
 *   3. 메뉴를 통해 메모 내용 편집 또는 삭제가 가능합니다.
 *
 * 기능3: 메모 편집 및 작성
 *   1. 제목 입력란과 본문 입력란, 이미지 첨부란이 구분되어 있어야 합니다. (글 중간에 이미지가 들어갈 수 있는 것이 아닌, 첨부된 이미지가 노출되는 부분이 따로 존재)
 *   2. 이미지 첨부란의 ‘추가' 버튼을 통해 이미지 첨부가 가능합니다. 첨부할 이미지는 다음 중 한 가지 방법을 선택해서 추가할 수 있습니다. 이미지는 0개 이상 첨부할 수 있습니다. 외부 이미지의 경우, 이미지를 가져올 수 없는 경우(URL이 잘못되었거나)에 대한 처리도 필요합니다.
 *     - 사진첩에 저장되어 있는 이미지
 *     - 카메라로 새로 촬영한 이미지
 *     - 외부 이미지 주소(URL)
 *   3. 편집 시에는 기존에 첨부된 이미지가 나타나며, 이미지를 더 추가하거나 기존 이미지를 삭제할 수 있습니다.
 */

class MainActivity : AppCompatActivity() {

    private var noteDB: NoteDatabase? = null
    private var noteList: LiveData<List<Note>>? = null
    private var mNoteViewModel: NoteViewModel? = null
    private var mNoteListAdapter: NoteListAdapter? = null

    var isEditMode = false
    set(value) {
        field = value

        if (isEditMode) {
            main_tool_bar_action_button.text = resources.getString(R.string.delete)
        } else {
            // 선택한 노트 삭제
//                val iter = noteList.iterator()
//                while (iter.hasNext()) {
//                    if (iter.next().isSelected)
//                        iter.remove()
//                }

            main_tool_bar_action_button.text = resources.getString(R.string.edit)
        }

        note_list.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mNoteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        mNoteListAdapter = NoteListAdapter(this)
        note_list.adapter = mNoteListAdapter
        note_list.hasFixedSize()

        mNoteViewModel?.getAllNotes()?.observe(this,
            Observer<List<Note>> { notes ->
                mNoteListAdapter?.setNotes(notes)
            })

        // DB 가져오기
        noteDB = NoteDatabase.getInstance(this)

        Thread {
            noteList = noteDB?.noteDao()?.getAll() ?: return@Thread
        }.start()

        // 편집, 삭제
        main_tool_bar_action_button.setOnClickListener {
            isEditMode = !isEditMode
        }

        // 새 노트
        add_note_button.setOnClickListener {
            startActivityForResult(
                Intent(this, NoteDetailActivity::class.java),
                NoteDetailActivity.EDIT_NOTE_REQUEST_CODE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        note_list.adapter?.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        if (isEditMode) {
            isEditMode = !isEditMode
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("yejimain", "onActivityResult. requestCode: $requestCode, resultCode: $resultCode, data: $data")

        // 변경된 노트 저장
        if (requestCode == NoteDetailActivity.EDIT_NOTE_REQUEST_CODE && resultCode == NoteDetailActivity.EDIT_NOTE_RESULT_CODE) {
            val note = data?.getParcelableExtra<Note>("note") ?: return
            Log.d("yejimain", "변경된 노트 저장 note: $note")
            mNoteViewModel?.insert(note)
        }
    }
}
