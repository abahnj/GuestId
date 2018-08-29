/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.norvera.guestid.data.source

import androidx.lifecycle.LiveData

import com.norvera.guestid.data.User


/**
 * Main entry point for accessing data.
 */
interface AppDataSource {

    fun getUserByID(userID: Int): User?

    fun saveUser( user: User)

    fun updateUser(user: User)

    fun refreshJournalEntries()

    fun deleteAllJournalEntries()

    fun deleteJournalEntry(journalEntryId: User)

    fun deleteJournalEntry(journalEntryId: Int)

    fun getUserByPhoneNumber(phoneNumber: String): LiveData<User>
}
