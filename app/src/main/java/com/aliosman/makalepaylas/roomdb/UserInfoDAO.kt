package com.aliosman.makalepaylas.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aliosman.makalepaylas.model.SaveUserInfoModel

@Dao
interface UserInfoDAO {

    @Query("SELECT * FROM SaveUserInfoModel")
    suspend fun getAll(): List<SaveUserInfoModel>

    @Insert
    suspend fun insertAll(userInfo: SaveUserInfoModel)

    @Update
    suspend fun updateAll(userInfo: List<SaveUserInfoModel>)

    @Query("DELETE FROM SaveUserInfoModel")
    suspend fun deleteAll()

    @Query("SELECT profilePicture FROM SaveUserInfoModel")
    suspend fun getProfilePicture(): ByteArray?

    @Query("SELECT userName FROM SAVEUSERINFOMODEL")
    suspend fun getUserName(): String
}