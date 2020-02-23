package dev.mijey.linenotes

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface NoteDao {
    @Query("SELECT * FROM note ORDER BY modifiedTimestamp DESC")
    fun getAll(): LiveData<List<Note>>

    @Insert(onConflict = REPLACE)
    fun insert(note: Note)

    @Insert
    fun insertAll(vararg users: Note)

    @Delete
    fun delete(note: Note)
}