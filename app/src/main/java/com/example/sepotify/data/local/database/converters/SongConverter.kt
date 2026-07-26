package com.example.sepotify.data.local.database.converters

import androidx.room.TypeConverter
import com.example.sepotify.data.remote.Supabase.json
import com.example.sepotify.domain.model.Song

class SongConverter {
    @TypeConverter
    fun fromSong(song: Song): String = json.encodeToString(song)

    @TypeConverter
    fun toSong(jsonString: String): Song = json.decodeFromString(jsonString)
}