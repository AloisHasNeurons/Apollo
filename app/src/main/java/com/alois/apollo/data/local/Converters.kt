package com.alois.apollo.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromList(value: List<Int>): String = Json.encodeToString<List<Int>>(value)

    @TypeConverter
    fun toList(value: String): List<Int> = Json.decodeFromString<List<Int>>(value)
}
