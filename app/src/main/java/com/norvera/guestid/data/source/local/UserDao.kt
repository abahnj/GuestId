package com.norvera.guestid.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.norvera.guestid.data.User

/**
 * Data Access Object for the users table.
 */
@Dao
interface UserDao {

    /**
     * Select all users from the users table.
     *
     * @return all users.
     */
    @Query("SELECT * FROM users") fun getUsers(): List<User>

    /**
     * Select a user by id.
     *
     * @param userId the user id.
     * @return the user with userId.
     */
    @Query("SELECT * FROM users WHERE id = :userId") fun getUserById(userId: Int): User?

    /**
     * Select a user by phone number.
     *
     * @param phoneNumber the user's phone number.
     * @return the user with phoneNumber.
     */
    @Query("SELECT * FROM users WHERE phoneNumber LIKE :phoneNumber") fun getUserByPhoneNumber(phoneNumber: String): LiveData<User>

    /**
     * Insert a user in the database. If the user already exists, replace it.
     *
     * @param user the user to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL) fun insertUser(user: User)

    /**
     * Update a user.
     *
     * @param user user to be updated
     * @return the number of users updated. This should always be 1.
     */
    @Update
    fun updateUser(user: User): Int


    /**
     * Delete a user by id.
     *
     * @return the number of users deleted. This should always be 1.
     */
    @Query("DELETE FROM users WHERE id = :userId") fun deleteUserById(userId: String): Int

    /**
     * Delete all users.
     */
    @Query("DELETE FROM users") fun deleteUsers()


}