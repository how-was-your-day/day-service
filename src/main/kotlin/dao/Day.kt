package dao

import com.mongodb.client.MongoCollection
import model.Day
import model.Occurrence
import model.Quality
import mongoConnection
import org.bson.Document
import org.bson.types.ObjectId
import java.util.*

data class DayCreate(
    val date: Date,
    val user: ObjectId,
    val occurrences: List<Occurrence>,
    val quality: Quality
)

class DayDAO : DAO<Day, DayCreate, Document, ObjectId> {
    fun <T> useDayCollection(block: MongoCollection<Day>.() -> T) =
        mongoConnection {
            database("mood-tracker-test") {
                getCollection("day", Day::class.java).let(block)
            }
        }

    override fun create(tc: DayCreate): Day = mongoConnection {
        database("mood-tracker-test") {
            getCollection("day", DayCreate::class.java).let {
                val insertResult = it.insertOne(tc)

                val id = insertResult.insertedId?.asObjectId()?.value ?: throw Exception("Not able to create days.")

                Day(
                    id,
                    tc.date,
                    tc.user,
                    tc.occurrences,
                    tc.quality
                )
            }
        }
    }

    override fun all(): List<Day> = useDayCollection {
        find().into(mutableListOf())
    }

    override fun delete(id: ObjectId): Boolean = useDayCollection {
        val filter = documentOf("_id" to id)

        val deleteResult = deleteOne(filter)

        return@useDayCollection deleteResult.deletedCount > 0
    }

    override fun update(t: Day): Day {
        TODO("Not yet implemented")
    }

    override fun findMany(filter: Document): List<Day> = useDayCollection {
        find(filter).into(mutableListOf())
    }

    override fun findOne(filter: Document): Day? = useDayCollection {
        return@useDayCollection find(filter).first()
    }

    override fun read(id: ObjectId): Day? = findOne(documentOf("_id" to id))
}