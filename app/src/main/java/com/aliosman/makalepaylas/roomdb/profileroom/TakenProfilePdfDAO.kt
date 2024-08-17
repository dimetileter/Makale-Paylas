package com.aliosman.makalepaylas.roomdb.profileroom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aliosman.makalepaylas.model.GetProfilePdfInfoModel

@Dao
interface TakenProfilePdfDAO {

    @Query("SELECT * FROM GetProfilePdfInfoModel")
    suspend fun getAll(): List<GetProfilePdfInfoModel>

    @Insert
    suspend fun add(data: GetProfilePdfInfoModel)

    @Query("DELETE FROM GetProfilePdfInfoModel")
    suspend fun delteAll()

    @Query("DELETE FROM GetProfilePdfInfoModel WHERE pdfUUID = :pdfUUID")
    suspend fun deletPdf(pdfUUID: String)
}