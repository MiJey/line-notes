package dev.mijey.linenotes

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(Converters::class)
data class Note(
    @PrimaryKey val createdTimestamp: Long,
    var modifiedTimestamp: Long,
    var title: String,
    var text: String,
    val images: ArrayList<String>   // 이미지 filename list
) {
    @Ignore
    var isSelected = false

    constructor(timestamp: Long) : this(
        createdTimestamp = timestamp,
        modifiedTimestamp = timestamp,
        title = "샘플 타이틀 $timestamp",
        text = "샘플 내용 $timestamp 샘플 내용 $timestamp 샘플 내용 $timestamp",
        images = ArrayList<String>()
    )
}