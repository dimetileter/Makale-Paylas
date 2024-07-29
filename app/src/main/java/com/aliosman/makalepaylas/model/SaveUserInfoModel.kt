package com.aliosman.makalepaylas.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SaveUserInfoModel(

    @ColumnInfo(name = "userName")
    val userName: String,

    @ColumnInfo(name = "nickname")
    val nickname: String,

    @ColumnInfo(name = "birthDate")
    val birthDare: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "userUID")
    val userUID: String,

    @ColumnInfo(name = "profilePictureUrl")
    val profilePictureUrl: String,

    @ColumnInfo(name = "profilePicture")
    val profilePicture : ByteArray?,

    @ColumnInfo(name = "profilePictureUUID")
    val profilePictureUUID: String?

){
    @PrimaryKey (autoGenerate = true) var id = 0
}