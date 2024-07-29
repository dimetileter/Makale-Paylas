package com.aliosman.makalepaylas.roomdb.homeroom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aliosman.makalepaylas.model.GetHomePdfInfoHModel

@Dao
interface TakenHomePdfDAO {

    @Query("SELECT * FROM GetHomePdfInfoHModel")
    suspend fun getAll(): List<GetHomePdfInfoHModel>

    @Insert
    suspend fun add(data: List<GetHomePdfInfoHModel>)

    @Query("DELETE FROM GetHomePdfInfoHModel")
    suspend fun delteAll()

}