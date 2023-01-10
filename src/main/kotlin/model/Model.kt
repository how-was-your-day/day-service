package model

import kotlinx.serialization.Serializable
import model.serializers.DateSerializer
import model.serializers.ObjectIdSerializer
import org.bson.types.ObjectId
import java.util.*

@Serializable
data class Day(
    @Serializable(ObjectIdSerializer::class) val id: ObjectId,
    @Serializable(DateSerializer::class) val date: Date,
    @Serializable(ObjectIdSerializer::class) val user: ObjectId,
    val occurrences: List<Occurrence>,
    val quality: Quality
    )

@Serializable
enum class Quality {
    AMAZING,
    GREAT,
    GOOD,
    OK,
    BAD,
    TERRIBLE
}

@Serializable
data class Occurrence(val text: String)