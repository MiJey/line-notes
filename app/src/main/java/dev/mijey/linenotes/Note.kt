package dev.mijey.linenotes

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(Converters::class)
data class Note(
    @PrimaryKey var createdTimestamp: Long = 0L,
    var modifiedTimestamp: Long = 0L,
    var title: String = "",
    var text: String = "",

    // 내부저장소에 폴더명이 createdTimestamp인 폴더 안에 이미지들을 저장해둠
    // ex) "${context.filesDir}/$createdTimestamp/${images[i]}"
    // ex) /data/user/0/dev.mijey.linenotes/files/1581791454985/20200218_090227.png
    val images: ArrayList<String> = ArrayList()
) : Parcelable {
    @Ignore
    var isSelected = false

    constructor(parcel: Parcel) : this() {
        this.createdTimestamp = parcel.readLong()
        this.modifiedTimestamp = parcel.readLong()
        this.title = parcel.readString() ?: ""
        this.text = parcel.readString() ?: ""

        val parcelImages = parcel.readArrayList(null) as ArrayList<String>
        for (item in parcelImages) {
            if (item.isNotEmpty())  // images가 비어있을 때 parcelImages에는 빈 문자열이 들어가서 빈 문자열인지 검사함
                this.images.add(item)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(createdTimestamp)
        parcel.writeLong(modifiedTimestamp)
        parcel.writeString(title)
        parcel.writeString(text)
        parcel.writeList(images)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note = Note(parcel)
        override fun newArray(size: Int): Array<Note?> = arrayOfNulls(size)
    }
}