package repo.day

import model.Day
import model.Occurrence
import model.Quality
import org.bson.Document
import org.bson.types.ObjectId
import repo.*
import java.util.*

class DayRepo(private val dayMapper: MongoObjectMapper<Day>) : MongoRepo<Day, ObjectId>() {

    override fun get(id: ObjectId): Day? =
        letClient {
            val days = getDatabase("mood-tracker-test").getCollection("day")

            val filter = Document()
            filter["_id"] = id

            return@letClient days.find(filter).first()?.let { this@DayRepo.dayMapper.map(it) }
        }

    fun delete(obj: Day): Boolean = delete(obj.id)
    override fun delete(id: ObjectId): Boolean =
        letClient {
            val days = getDatabase("mood-tracker-test").getCollection("day")

            val filter = Document()
            filter["id"] = id

            val deleteResult = days.deleteOne(filter)

            return@letClient deleteResult.deletedCount > 0
        }

    override fun update(id: ObjectId, newValue: Day): Day {
        TODO("Not yet implemented")
    }

    override fun create(value: Map<String, Any>): Day =
        letClient {
            val days = getDatabase("mood-tracker-test").getCollection("day")

            val doc = Document(value)

            val insertOneResult = days.insertOne(doc)

            doc["_id"] = insertOneResult.insertedId?.asObjectId()

            return@letClient this@DayRepo.dayMapper.map(doc)
        }

    override fun all(): List<Day> =
        letClient {
            val days = getDatabase("mood-tracker-test").getCollection("day")

            return@letClient days.find().into(mutableListOf()).map(this@DayRepo.dayMapper::map)
        }

}

class DayMapper : MongoObjectMapper<Day> {
    override fun map(doc: Document): Day =
        Day(
            doc.getObjectId("_id"),
            Date(doc["date"] as Long),
            doc.getList("occurrences", String::class.java).map { Occurrence(it) },
            Quality.valueOf(doc.getString("quality"))
        )

    override fun unmap(obj: Day): Document =
        Document(
            mapOf(
                "date" to obj.date.time,
                "occurrences" to obj.occurrences.map { it.text },
                "quality" to obj.quality
            )
        )
}