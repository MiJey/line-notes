package dev.mijey.linenotes

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData


class NoteRepository(application: Application) {
    private val mNoteDao: NoteDao?
    val mAllNotes: LiveData<List<Note>>?

    init {
        val db = NoteDatabase.getInstance(application)
        this.mNoteDao = db?.noteDao()
        this.mAllNotes = mNoteDao?.getAll()
    }

    fun insert(note: Note) {
        InsertAsyncTask(mNoteDao).execute(note)
    }

    private class InsertAsyncTask(val mAsyncTaskDao: NoteDao?) : AsyncTask<Note?, Void?, Void?>() {
        override fun doInBackground(vararg params: Note?): Void? {
            mAsyncTaskDao!!.insert(params[0] ?: return null)
            return null
        }
    }
}