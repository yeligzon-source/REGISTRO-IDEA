package com.example.data

import kotlinx.coroutines.flow.Flow

class RegistroRepository(private val registroDao: RegistroDao, private val userDao: UserDao) {
    
    fun getRegistrosByUser(email: String): Flow<List<RegistroEntity>> {
        return registroDao.getRegistrosByUser(email)
    }

    val allRegistros: Flow<List<RegistroEntity>> = registroDao.getAllRegistros()

    suspend fun insert(registro: RegistroEntity) {
        registroDao.insertRegistro(registro)
    }

    suspend fun delete(id: Int) {
        registroDao.deleteRegistroById(id)
    }

    suspend fun update(registro: RegistroEntity) {
        registroDao.updateRegistro(registro)
    }

    // User operations for Auth
    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }
}
