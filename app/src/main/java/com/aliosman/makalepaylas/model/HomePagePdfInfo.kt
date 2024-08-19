package com.aliosman.makalepaylas.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HomePagePdfInfo(

    @ColumnInfo(name = "pdfUUID")
    val pdfUUID: String,

    @ColumnInfo(name = "artName")
    val artName: String,

    @ColumnInfo(name = "nickname")
    val nickname: String,

    @ColumnInfo(name = "pdfUrlBitmap")
    val pdfBitmapUrl: String?,

){
    @PrimaryKey (autoGenerate = true) var id = 0
}