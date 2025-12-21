package com.alois.apollo.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromList(value: List<Int>) = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String) = Json.decodeFromString<List<Int>>(value)
}
