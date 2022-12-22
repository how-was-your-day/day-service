package model.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.types.ObjectId
import java.util.*

class DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("model.serializers.DateSerializer", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Date =
        Date(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeLong(value.time)
    }
}

class ObjectIdSerializer : KSerializer<ObjectId> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("serializers.ObjectId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ObjectId {
        return ObjectId(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: ObjectId) {
        encoder.encodeString(value.toHexString())
    }

}
