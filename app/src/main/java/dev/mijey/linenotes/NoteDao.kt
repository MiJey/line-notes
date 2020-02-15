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

//    @Query("SELECT * FROM note WHERE createdTimestamp IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<Note>
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): Note

    @Insert(onConflict = REPLACE)
    fun insert(note: Note)

    @Insert
    fun insertAll(vararg users: Note)

    @Delete
    fun delete(note: Note)
}