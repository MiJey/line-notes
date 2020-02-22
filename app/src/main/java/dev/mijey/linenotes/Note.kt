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
    val images: ArrayList<String> = ArrayList() // TODO imageNameList로 이름 바꾸기
) : Parcelable {
    @Ignore
    var isSelected = false
    @Ignore
    val imageList: ArrayList<NoteImage> = ArrayList()

    constructor(parcel: Parcel) : this() {
        this.createdTimestamp = parcel.readLong()
        this.modifiedTimestamp = parcel.readLong()
        this.title = parcel.readString() ?: ""
        this.text = parcel.readString() ?: ""

        images.clear()
        imageList.clear()
        val parcelList = parcel.readArrayList(null) as ArrayList<String>
        for (imageName in parcelList) {
            // images가 비어있을 때 parcelImages에 빈 문자열이 들어가서 빈 문자열인지 검사함
            if (imageName.isNotEmpty()){
                images.add(imageName)
                imageList.add(NoteImage(this, imageName))
            }
        }
    }

    init {
        imageList.clear()
        for (imageName in images) {
            if (imageName.isNotEmpty()){
                imageList.add(NoteImage(this, imageName))
            }
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