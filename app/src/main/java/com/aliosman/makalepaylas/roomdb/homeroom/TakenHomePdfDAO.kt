package com.aliosman.makalepaylas.roomdb.homeroom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aliosman.makalepaylas.model.HomePagePdfInfo

@Dao
interface TakenHomePdfDAO {

    @Query("SELECT * FROM HomePagePdfInfo")
    suspend fun getAll(): List<HomePagePdfInfo>

    @Insert
    suspend fun add(data: HomePagePdfInfo)

    @Insert
    suspend fun addAll(data: List<HomePagePdfInfo>)

    @Query("DELETE FROM HomePagePdfInfo")
    suspend fun deleteAll()

    @Query("DELETE FROM HOMEPAGEPDFINFO WHERE pdfUUID=:uuid")
    suspend fun deletePdf(uuid: String)

}