package com.norvera.guestid.data.source.local

import androidx.lifecycle.LiveData
import com.norvera.guestid.data.User
import com.norvera.guestid.data.source.AppDataSource
import com.norvera.guestid.utilities.AppExecutors

class AppLocalDataSource private constructor( private val appExecutors: AppExecutors, private val appDatabase: AppDatabase) : AppDataSource{
    private val userDao : UserDao = appDatabase.userDao()

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: AppLocalDataSource? = null

        fun getInstance(appExecutors: AppExecutors, appDatabase: AppDatabase) =
                instance ?: synchronized(this) {
                    instance ?: AppLocalDataSource(appExecutors, appDatabase).also { instance = it }
                }
    }

    override fun getUserByID(userID: Int): User? {
        var user : User? = null
        AppExecutors.instance.diskIO().execute { user = userDao.getUserById(userID)}

        return user

    }

    override fun getUserByPhoneNumber(phoneNumber: String): LiveData<User> {

        return userDao.getUserByPhoneNumber(phoneNumber)
    }



    override fun refreshJournalEntries() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAllJournalEntries() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteJournalEntry(journalEntryId: User) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteJournalEntry(journalEntryId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveUser(user: User) {
        AppExecutors.instance.diskIO().execute { userDao.insertUser(user) }
    }

    override fun updateUser(user: User) {
        AppExecutors.instance.diskIO().execute { userDao.updateUser(user) }
    }

}