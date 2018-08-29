package com.norvera.guestid.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["phoneNumber"], unique = true)])
data class User(@PrimaryKey(autoGenerate = true) val id: Int,
                var name: String,
                var age: Int,
                var gender: String,
                var phoneNumber: String)
