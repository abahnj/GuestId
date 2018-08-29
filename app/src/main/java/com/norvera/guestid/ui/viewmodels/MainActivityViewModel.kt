package com.norvera.guestid.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.norvera.guestid.data.User
import com.norvera.guestid.data.source.AppRepository

class MainActivityViewModel(private val appRepository: AppRepository) : ViewModel() {

    init {

    }
    // TODO: Implement the ViewModel

    fun saveUser(user: User) {
        appRepository.saveUser(user)
    }

    fun findUserByNumber(phoneNumber : String) : LiveData<User> {

        return appRepository.getUserByPhoneNumber(phoneNumber)
    }
}
