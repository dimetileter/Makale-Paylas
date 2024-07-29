package com.aliosman.makalepaylas.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GetProfilePdfInfoModel(

    @ColumnInfo(name = "artName")
    val artName: String,

    @ColumnInfo(name = "artDesc")
    val artDesc: String,

    @ColumnInfo(name = "pdfUrl")
    val pdfUrl: String,

    @ColumnInfo(name = "pdfBitmapUrl")
    val pdfBitmapUrl: String,

    @ColumnInfo(name = "createdAt")
    val createdAt: String,

    @ColumnInfo(name = "nickname")
    val nickname: String,

    @ColumnInfo(name = "pdfUUID")
    val pdfUUID: String

   ) {
    @PrimaryKey (autoGenerate = true) var id = 0
}