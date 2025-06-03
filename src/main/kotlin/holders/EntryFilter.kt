package org.kechinvv.entities

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class EntryFilter(
    val methodAnnotation: Set<String>,
    @Serializable(with = RegexSerializer::class) val methodName: Regex?,
    val classAnnotation: Set<String>,
    @Serializable(with = RegexSerializer::class) val className: Regex?,
    val kind: String?,
    val args: List<String>,
    val returnType: String
)

class RegexSerializer : KSerializer<Regex> {

    override fun deserialize(decoder: Decoder): Regex =
        Regex(decoder.decodeString())


    override fun serialize(encoder: Encoder, value: Regex) {
        encoder.encodeString(value.toString())
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Regex", PrimitiveKind.STRING)
}