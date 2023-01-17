import model.Day
import model.Occurrence
import model.Quality
import model.User
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.Serializer
import java.util.*

private fun propertiesOf(vararg pairs: Pair<Any, Any>) : Properties = Properties().apply {
    for (pair in pairs)
        put(pair.first, pair.second)
}

typealias KafkaKey = Pair<User, Date>
typealias KafkaValue = Day
class DayProducer : KafkaProducer<KafkaKey, KafkaValue>(
    propertiesOf(
        "bootstrap.servers" to "localhost:9092",
        "linger.ms" to 1
    ), KeySerializer(), ValueSerializer()
)

fun byteArrayOf(vararg arr: ByteArray) : ByteArray {
    var finalArray: ByteArray = byteArrayOf()
    for (byteArray in arr) {
        finalArray += byteArray
    }
    return finalArray
}

private class ValueSerializer : Serializer<KafkaValue> {
    override fun serialize(topic: String, data: KafkaValue): ByteArray {
        assert(topic == "day-events")

        return with (data) {
            byteArrayOf(
                id.toByteArray(),
                user.toByteArray(),
                date.toByteArray(),
                occurrences.toByteArray(),
                quality.toByteArray()
            )
        }
    }
}

private class KeySerializer : Serializer<KafkaKey> {
    override fun serialize(topic: String, data: KafkaKey): ByteArray {
        assert(topic == "day-events")

        return with (data) {
            byteArrayOf(
                first.toByteArray(),
                second.toByteArray(),
            )
        }
    }
}

private fun List<Occurrence>.toByteArray(): ByteArray {
    var finalArray: ByteArray = byteArrayOf()
    for (elem in this) {
        finalArray += elem.toByteArray()
    }
    return finalArray
}

private fun Date.toByteArray(): ByteArray = byteArrayOf(time.toByte())
private fun User.toByteArray(): ByteArray = id.toByteArray()
private fun Occurrence.toByteArray() : ByteArray = text.toByteArray()
private fun Quality.toByteArray(): ByteArray = name.toByteArray()