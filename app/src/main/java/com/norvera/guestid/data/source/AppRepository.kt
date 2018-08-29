package com.norvera.guestid.data.source

import androidx.lifecycle.LiveData
import com.norvera.guestid.data.User

class AppRepository private constructor(private var mRemoteDataSource: AppDataSource?,
                                        private var mLocalDataSource: AppDataSource) : AppDataSource {



    @Volatile
    private var INSTANCE: AppRepository? = null


    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    //Map<String, JournalEntry> mCachedEntries;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    private val mCacheIsDirty = false


    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AppRepository? = null

        /**
         * Returns the single instance of this class, creating it if necessary.
         *
         * @param mRemoteDataSource the backend data source
         * @param mLocalDataSource  the device storage data source
         * @return the [AppRepository] instance
         */
        fun getInstance(mRemoteDataSource: AppDataSource?, mLocalDataSource: AppDataSource) =
                instance ?: synchronized(this) {
                    instance
                            ?: AppRepository(mRemoteDataSource, mLocalDataSource).also { instance = it }
                }

        /**
         * Used to force [.getInstance] to create a new instance
         * next time it's called.
         */
        fun destroyInstance() {
            instance = null
        }
    }

    override fun getUserByPhoneNumber(phoneNumber: String): LiveData<User> {
        return mLocalDataSource.getUserByPhoneNumber(phoneNumber)
    }

    override fun getUserByID(userID: Int): User {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveUser(user: User) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateUser(user: User) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
}