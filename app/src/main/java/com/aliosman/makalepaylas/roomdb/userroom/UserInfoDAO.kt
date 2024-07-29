package com.aliosman.makalepaylas.roomdb.userroom

import androidx.room.Dao
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

    @Query("SELECT nickname FROM SaveUserInfoModel")
    suspend fun getNickname(): String?

    @Query("SELECT userUID FROM SaveUserInfoModel")
    suspend fun getUserUUID(): String?

    @Query("UPDATE SaveUserInfoModel SET profilePicture = :data")
    suspend fun updateProfilePicture(data: ByteArray)

    @Query("SELECT profilePictureUUID FROM SaveUserInfoModel")
    suspend fun getProfilePictureUUID(): String?
}