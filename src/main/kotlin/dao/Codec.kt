package dao

import model.Day
import model.Occurrence
import model.Quality
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import java.util.*

class DayCodec : Codec<Day> {
    override fun encode(writer: BsonWriter, value: Day, encoderContext: EncoderContext) {
        writer.writeStartDocument()

        writer.writeObjectId("_id", value.id)
        writer.writeObjectId("user", value.user)
        writer.writeDateTime("date", value.date.time)
        writer.writeStartArray("occurrences")
        writer.writeInt32(value.occurrences.size)
        for (occurrence in value.occurrences)
            writer.writeString(occurrence.text)
        writer.writeEndArray()
        writer.writeInt32("quality", value.quality.ordinal)

        writer.writeEndDocument()
    }

    override fun getEncoderClass(): Class<Day> = Day::class.java

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Day {
        reader.readStartDocument()
        val id = reader.readObjectId("_id")
        val user = reader.readObjectId("user")
        val date = Date(reader.readDateTime("date"))
        reader.readStartArray()
        val occurrences = List(reader.readInt32()) {
            Occurrence(reader.readString())
        }
        reader.readEndArray()
        val quality = Quality.values()[reader.readInt32("quality")]

        reader.readEndDocument()

        return Day(
            id, date, user, occurrences, quality
        )
    }
}


class DayCreateCodec : Codec<DayCreate> {
    override fun encode(writer: BsonWriter, value: DayCreate, encoderContext: EncoderContext) {
        writer.writeStartDocument()

        writer.writeObjectId("user", value.user)
        writer.writeDateTime("date", value.date.time)
        writer.writeStartArray("occurrences")
        writer.writeInt32(value.occurrences.size)
        for (occurrence in value.occurrences)
            writer.writeString(occurrence.text)
        writer.writeEndArray()
        writer.writeInt32("quality", value.quality.ordinal)

        writer.writeEndDocument()
    }

    override fun getEncoderClass(): Class<DayCreate> = DayCreate::class.java

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): DayCreate {
        reader.readStartDocument()

        val user = reader.readObjectId("user")
        val date = Date(reader.readDateTime("date"))
        reader.readStartArray()
        val occurrences = List(reader.readInt32()) {
            Occurrence(reader.readString())
        }
        reader.readEndArray()
        val quality = Quality.values()[reader.readInt32("quality")]

        reader.readEndDocument()

        return DayCreate(
            date, user, occurrences, quality
        )
    }
}