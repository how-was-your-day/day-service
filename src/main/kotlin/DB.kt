import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.result.InsertOneResult
import org.bson.BsonDocument
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

object DB {
    private const val CONN_STRING = "mongodb://127.0.0.1:27017"

    @JvmStatic
    private val clientSettings: MongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(CONN_STRING))
        .retryWrites(true)
        .build()

    private fun <T> moodRequest(block: MongoCollection<Document>.() -> T) : T {
        lateinit var mongoClient: MongoClient;
        try {
            mongoClient = MongoClients.create(clientSettings)

            val database = mongoClient.getDatabase("mood-tracker-test")

            val moods = database.getCollection("moods")

            return moods.let(block)
        } finally {
            mongoClient.close()
        }
    }

    fun insert(value: Int): InsertOneResult = moodRequest {
        val doc = Document()
        doc["key"] = value

        insertOne(doc)
    }

    fun read() : List<Document> = moodRequest {
        return@moodRequest find().toList()
    }

    private fun <T> FindIterable<T>.toList() : List<T> {
        val mutList = mutableListOf<T>()
        for (elem in this) {
            mutList.add(elem)
        }
        return mutList

    }

    fun deleteAll() = moodRequest {
        val filter = object : Bson {
            override fun <TDocument : Any?> toBsonDocument(
                documentClass: Class<TDocument>?,
                codecRegistry: CodecRegistry?
            ): BsonDocument {
                return BsonDocument()
            }
        }
        return@moodRequest deleteMany(filter)

    }
}