package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {
    @Query("SELECT * FROM registros WHERE userEmail = :email ORDER BY timestamp DESC, id DESC")
    fun getRegistrosByUser(email: String): Flow<List<RegistroEntity>>

    @Query("SELECT * FROM registros ORDER BY timestamp DESC, id DESC")
    fun getAllRegistros(): Flow<List<RegistroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistro(registro: RegistroEntity)

    @Query("DELETE FROM registros WHERE id = :id")
    suspend fun deleteRegistroById(id: Int)

    @Update
    suspend fun updateRegistro(registro: RegistroEntity)
}
