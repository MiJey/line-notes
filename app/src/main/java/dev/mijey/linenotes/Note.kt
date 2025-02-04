package dev.mijey.linenotes

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.io.File

@Entity
@TypeConverters(Converters::class)
data class Note(
    @PrimaryKey var createdTimestamp: Long = 0L,
    var modifiedTimestamp: Long = 0L,
    var title: String = "",
    var text: String = "",
    val imageNameList: ArrayList<String> = ArrayList()
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

        imageNameList.clear()
        imageList.clear()
        val parcelList = parcel.readArrayList(null) as ArrayList<String>
        for (imageName in parcelList) {
            // images가 비어있을 때 parcelImages에 빈 문자열이 들어가서 빈 문자열인지 검사함
            if (imageName.isNotEmpty()){
                Log.d("yejinote", "11111 createdTimestamp: $createdTimestamp, imageName: $imageName")
                imageNameList.add(imageName)
                imageList.add(NoteImage(this, imageName))
            }
        }
    }

    init {
        imageList.clear()
        for (imageName in imageNameList) {
            if (imageName.isNotEmpty()){
                Log.d("yejinote", "22222 createdTimestamp: $createdTimestamp, imageName: $imageName")
                imageList.add(NoteImage(this, imageName))
            }
        }
    }

    fun createDirectoryFromTemp(context: Context) {
        // 임시 폴더 이름을 바꿔서 노트에 종속시키기
        val tempDirectoryPath = "${context.filesDir.path}/0"
        val noteDirectoryPath = getDirectoryPath(context)
        FileIOHelper.rename(tempDirectoryPath, noteDirectoryPath)
    }

    fun getDirectory(context: Context): File? {
        val noteDirectory = File(getDirectoryPath(context))
        if (!noteDirectory.exists()) {
            if (!noteDirectory.mkdirs())
                return null
        }

        return noteDirectory
    }

    fun getDirectoryPath(context: Context): String {
        return "${context.filesDir.path}/$createdTimestamp"
    }

    // 리스트에 없는 이미지는 파일 삭제, 파일에 없는 이미지는 리스트에서 삭제
    fun syncImageFileList(context: Context) {
        // imageList -> imageNameList 동기화
        imageNameList.clear()
        for (image in imageList)
            imageNameList.add(image.name)

        // imageNameList <-> 파일 동기화
        val directoryPath = getDirectoryPath(context)
        val imageFileNameList = getDirectory(context)?.list()

        if (imageFileNameList != null && imageFileNameList.isNotEmpty()) {
            // 파일은 있는데 리스트에 없으면 파일 삭제
            for (imageFileName in imageFileNameList) {
                if (!imageNameList.contains(imageFileName))
                    File("$directoryPath/$imageFileName").delete()
            }

            // 리스트엔 있는데 파일이 없으면 리스트 삭제
            for (imageName in imageNameList) {
                if (!imageFileNameList.contains(imageName))
                    File("$directoryPath/$imageName").delete()
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(createdTimestamp)
        parcel.writeLong(modifiedTimestamp)
        parcel.writeString(title)
        parcel.writeString(text)
        parcel.writeList(imageNameList)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note = Note(parcel)
        override fun newArray(size: Int): Array<Note?> = arrayOfNulls(size)
    }
}