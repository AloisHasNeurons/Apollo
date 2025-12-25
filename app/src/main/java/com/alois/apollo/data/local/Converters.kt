package com.alois.apollo.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Type converters for Room database to handle complex types like Lists. Serialization is handled
 * via kotlinx.serialization.
 */
class Converters {
    @TypeConverter
    fun fromList(value: List<Int>): String = Json.encodeToString<List<Int>>(value)

    @TypeConverter
    fun toList(value: String): List<Int> = Json.decodeFromString<List<Int>>(value)
}
