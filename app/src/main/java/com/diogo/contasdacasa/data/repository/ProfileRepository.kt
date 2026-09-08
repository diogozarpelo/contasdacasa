package com.diogo.contasdacasa.data.repository

import com.diogo.contasdacasa.data.local.ProfileDao
import com.diogo.contasdacasa.data.model.Profile

class ProfileRepository(
    private val profileDao: ProfileDao
) {

    suspend fun createProfile(name: String): Long {
        return profileDao.insert(
            Profile(name = name.trim())
        )
    }

    suspend fun getAllProfiles(): List<Profile> {
        return profileDao.getAll()
    }

    suspend fun getProfileById(id: Long): Profile? {
        return profileDao.getById(id)
    }

    suspend fun deleteProfile(id: Long) {
        profileDao.deleteById(id)
    }
}