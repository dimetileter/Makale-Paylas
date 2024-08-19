package com.aliosman.makalepaylas.roomdb.profileroom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aliosman.makalepaylas.model.ProfilePagePdfInfo

@Dao
interface TakenProfilePdfDAO {

    @Query("SELECT * FROM ProfilePagePdfInfo")
    suspend fun getAll(): List<ProfilePagePdfInfo>

    @Insert
    suspend fun add(data: ProfilePagePdfInfo)

    @Query("DELETE FROM ProfilePagePdfInfo")
    suspend fun delteAll()

    @Query("DELETE FROM ProfilePagePdfInfo WHERE pdfUUID = :pdfUUID")
    suspend fun deletPdf(pdfUUID: String)
}