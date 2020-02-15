package dev.mijey.linenotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class NoteViewModel(application: Application): AndroidViewModel(application) {
    private val mRepository = NoteRepository(application)
    private val mAllNotes = mRepository.mAllNotes

    fun getAllNotes(): LiveData<List<Note>>? {
        return mAllNotes
    }

    fun insert(note: Note) {
        mRepository.insert(note)
    }
}