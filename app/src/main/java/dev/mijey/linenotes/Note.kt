package dev.mijey.linenotes

class Note {
    var title = ""
    var text = ""
    val images = ArrayList<String>()

    private val createdTimestamp: Long
    private val modifiedTimestamp: Long

    constructor(timestamp: Long) {
        this.createdTimestamp = timestamp
        this.modifiedTimestamp = timestamp

        this.title = "샘플 타이틀 $timestamp"
        this.text = "샘플 내용 $timestamp 샘플 내용 $timestamp 샘플 내용 $timestamp"
    }
}