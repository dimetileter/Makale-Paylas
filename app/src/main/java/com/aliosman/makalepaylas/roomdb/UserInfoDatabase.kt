package com.aliosman.makalepaylas.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aliosman.makalepaylas.model.SaveUserInfoModel

@Database(entities = [SaveUserInfoModel::class], version = 1)
abstract class UserInfoDatabase : RoomDatabase() {
    abstract fun userDao(): UserInfoDAO
}