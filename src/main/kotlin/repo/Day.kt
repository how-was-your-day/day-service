package repo.day

import collection
import model.Day
import model.Occurrence
import model.Quality
import mongoConnection
import org.bson.Document
import org.bson.types.ObjectId
import repo.*
import java.util.*

class DayRepo(private val dayMapper: MongoObjectMapper<Day>) : Repo<Day, ObjectId> {

    override fun get(id: ObjectId): Day? =
        mongoConnection {
            val day = database("mood-tracker-test") {
                collection("day") {
                    val filter = Document()
                    filter["_id"] = id

                    find(filter).first()
                }
            }

            return@mongoConnection day?.let { this@DayRepo.dayMapper.map(it) }
        }

    fun delete(obj: Day): Boolean = delete(obj.id)
    override fun delete(id: ObjectId): Boolean =
        mongoConnection {
            val deleteResult = database("mood-tracker-test") {
                collection("day") {
                    val filter = Document()
                    filter["_id"] = id

                    deleteOne(filter)
                }
            }

            return@mongoConnection deleteResult.deletedCount > 0
        }

    override fun update(id: ObjectId, newValue: Day): Day {
        TODO("Not yet implemented")
    }

    override fun create(value: Map<String, Any>): Day =
        mongoConnection {
            val doc = database("mood-tracker-test"){
                collection("day") {
                    val doc = Document(value)

                    val insertOneResult = insertOne(doc)

                    val id = insertOneResult.insertedId?.asObjectId()?.value ?: throw Exception("Not able to create days.")

                    doc["_id"] = id

                    doc
                }
            }

            return@mongoConnection this@DayRepo.dayMapper.map(doc)
        }

    override fun all(): List<Day> =
        mongoConnection {
            database("mood-tracker-test"){
                collection("day") {
                    find().into(mutableListOf()).map(this@DayRepo.dayMapper::map)
                }
            }
        }

    override fun contains(id: ObjectId): Boolean {
        return this[id] != null
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
                "quality" to obj.quality.toString()
            )
        )
}