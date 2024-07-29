package com.aliosman.makalepaylas.model


data class SavesPdfModel(

    val artName: String,
    val artDesc: String,
    val pdfUrl: String,
    val pdfBitmapUrl: String,
    val createdAt: String,
    val nickname: String,
    val pdfUUID: String,
)