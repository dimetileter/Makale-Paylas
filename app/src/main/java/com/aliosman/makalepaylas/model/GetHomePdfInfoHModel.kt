package com.aliosman.makalepaylas.model

data class GetHomePdfInfoHModel(

    val artName: String,
    val artDesc: String,
    val pdfUrl: String,
    val pdfBitmapUrl: String?,
    val createdAt: String,
    val nickname: String

)