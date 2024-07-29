package com.aliosman.makalepaylas.roomdb.profileroom

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aliosman.makalepaylas.model.GetProfilePdfInfoModel

@Database(entities = [GetProfilePdfInfoModel::class], version = 1)
abstract class TakenProfilePdfDatabase : RoomDatabase() {
    abstract fun userDao(): TakenProfilePdfDAO


    //Aşağıdaki kodlar bu koruma işlemini yapar
    @Volatile
    private var instance: TakenProfilePdfDatabase? = null

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
        TakenProfilePdfDatabase::class.java,
        "TakenProfilePdf"
    ).build()

}