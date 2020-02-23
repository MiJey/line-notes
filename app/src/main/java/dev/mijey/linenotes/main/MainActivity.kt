package dev.mijey.linenotes.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
 *
 * TODO 리사이클러뷰 스크롤바 스타일 적용하기
 * TODO 메모 롱클릭하면 편집모드로 들어가기
 * TODO 제목이 없으면 제목 부분 gone
 * TODO 이미지 오른쪽 상단 라운딩
 * TODO 맨 마지막에 비어있는 항목 하나 넣어서 새 노트 아이콘 공간 만들기
 * TODO 전체 선택 만들기
 */

class MainActivity : AppCompatActivity() {

    private var noteDB: NoteDatabase? = null
    private var mNoteViewModel: NoteViewModel? = null
    private var mNoteListAdapter: NoteListAdapter? = null

    var isEditMode = false
        set(value) {
            // 삭제 작업 도중 삭제 버튼 여러번 누르는 것 방지
            if (field == value) return
            field = value

            if (value) {
                isEditMode = true
                main_tool_bar_action_button.text = resources.getString(R.string.delete)
                note_list.adapter?.notifyDataSetChanged()
            } else {
                // 선택한 노트 삭제 확인
                val notes = mNoteViewModel?.getAllNotes()?.value ?: return

                Thread {
                    var count = 0
                    for (note in notes) {
                        if (note.isSelected)
                            count += 1
                    }

                    // 선택한 메모가 없음 -> 편집 모드에서 빠져나가기
                    if (count == 0) {
                        Handler(Looper.getMainLooper()).post {
                            isEditMode = false
                            main_tool_bar_action_button.text = resources.getString(R.string.edit)
                            note_list.adapter?.notifyDataSetChanged()
                        }
                        return@Thread
                    }

                    Handler(Looper.getMainLooper()).post {
                        AlertDialog.Builder(this)
                            .setMessage(String.format(resources.getString(R.string.delete_confirm), count))
                            .setPositiveButton(resources.getString(R.string.delete)) { dialog, id ->
                                Thread {
                                    // 선택한 노트 삭제
                                    for (note in notes) {
                                        if (note.isSelected)
                                            mNoteViewModel?.delete(note)
                                    }

                                    Handler(Looper.getMainLooper()).post {
                                        // 삭제 완료 후 편집 모드에서 빠져나가기
                                        isEditMode = false
                                        main_tool_bar_action_button.text = resources.getString(R.string.edit)
                                    }
                                }.start()
                            }
                            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, id ->
                                // 편집 모드에 남아있기
                                isEditMode = true
                            }
                            .create()
                            .show()
                    }
                }.start()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // DB에서 노트 가져오기
        noteDB = NoteDatabase.getInstance(this)
        mNoteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        mNoteListAdapter = NoteListAdapter(this)
        note_list.adapter = mNoteListAdapter
        note_list.hasFixedSize()

        mNoteViewModel?.getAllNotes()?.observe(this,
            Observer<List<Note>> { notes ->
                mNoteListAdapter?.setNotes(notes)
            })

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

    override fun onDestroy() {
        NoteDatabase.destroyInstance()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 변경된 노트 저장
        if (requestCode == NoteDetailActivity.EDIT_NOTE_REQUEST_CODE && resultCode == NoteDetailActivity.EDIT_NOTE_RESULT_CODE) {
            val note = data?.getParcelableExtra<Note>("note") ?: return
            mNoteViewModel?.insert(note)
        }
    }
}
