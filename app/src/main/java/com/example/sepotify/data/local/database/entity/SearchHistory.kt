package com.example.sepotify.data.local.database.entity

import androidx.room.Entity

@Entity(tableName = "search_history", primaryKeys = ["userId", "query"])
data class SearchHistory(
    val userId: String,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)