package dev.mijey.linenotes

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.json.JSONArray

@Entity
@TypeConverters(Converters::class)
data class Note(
    @PrimaryKey var createdTimestamp: Long = 0L,
    var modifiedTimestamp: Long = 0L,
    var title: String = "",
    var text: String = "",
    val images: ArrayList<String> = ArrayList()   // 이미지 filename list
): Parcelable {
    @Ignore
    var isSelected = false

    constructor(parcel: Parcel): this() {
        this.createdTimestamp = parcel.readLong()
        this.modifiedTimestamp = parcel.readLong()
        this.title = parcel.readString()
        this.text = parcel.readString()

        val imagesJson = JSONArray(parcel.readString())
        for (i in 0 until imagesJson.length()) {
            images.add(imagesJson[i].toString())
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(createdTimestamp)
        parcel.writeLong(modifiedTimestamp)
        parcel.writeString(title)
        parcel.writeString(text)

        val imagesJson = JSONArray()
        for (item in images) {
            imagesJson.put(item)
        }
        parcel.writeString(imagesJson.toString())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note = Note(parcel)
        override fun newArray(size: Int): Array<Note?> = arrayOfNulls(size)
    }
}