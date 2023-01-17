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
    val user: User,
    val occurrences: List<Occurrence>,
    val quality: Quality
    )

@Serializable
data class User(
    @Serializable(ObjectIdSerializer::class) val id: ObjectId,
) {
    constructor(id: String) : this(ObjectId(id))
}

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