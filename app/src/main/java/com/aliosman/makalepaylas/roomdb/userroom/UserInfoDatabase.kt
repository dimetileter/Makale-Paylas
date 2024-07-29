package com.aliosman.makalepaylas.roomdb.userroom

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aliosman.makalepaylas.model.SaveUserInfoModel
import com.aliosman.makalepaylas.roomdb.profileroom.TakenProfilePdfDatabase

@Database(entities = [SaveUserInfoModel::class], version = 1)
abstract class UserInfoDatabase : RoomDatabase() {
    abstract fun userDao(): UserInfoDAO


    //Aşağıdaki kodlar bu koruma işlemini yapar
    @Volatile
    private var instance: UserInfoDatabase? = null

    private var lock = Any()
    //Bir nesne oluşturulduğunda çalışan fonksiyondur. Eğer burada instansce varsa bir
    //başka nesne (aktivite) buradadır ve kilitler. Eeğr yoksa kilitleme olmaz ve
    //buraya gelen nesne çalışır
    operator fun invoke(context: Context) = instance ?: synchronized(lock)
    {
        instance ?: databaseOlustur(context).also {it ->
            instance = it
        }
    }

    private fun databaseOlustur(context: Context) = Room.databaseBuilder(
        context.applicationContext,
        UserInfoDatabase::class.java,
        "UserInfos"
    ).build()

}