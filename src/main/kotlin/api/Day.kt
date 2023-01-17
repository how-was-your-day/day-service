package api.v1.day

import DayProducer
import api.ErrorBody
import api.firstOf
import api.rootCause
import api.sanitizePayload
import dao.DayCreate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import model.Occurrence
import model.Quality
import model.User
import model.serializers.ObjectIdSerializer
import org.apache.kafka.clients.producer.ProducerRecord
import org.bson.types.ObjectId
import repo.DayRepo
import java.util.*

@Serializable
data class DayCreationDTO(val date: Long, val user: String, val occurrences: List<String>, val quality: Quality)

fun Route.dayRoute(dayRepo: DayRepo, dayProducer: DayProducer) {
    route("/day") {
        get {
            val userId = call.request.queryParameters.firstOf("u", "user")

            if (userId == null) {
                call.respond(HttpStatusCode.OK, dayRepo.all())
            } else {
                if (ObjectId.isValid(userId)) {
                    call.respond(HttpStatusCode.OK, dayRepo.findByUser(ObjectId(userId)))
                } else {
                    call.respondText("/day/?{u|user}=id: id must be a 12-byte hex string", status = HttpStatusCode.BadRequest)
                }

            }
        }

        get("{id?}") {
            val hexString = call.parameters["id"]

            sanitizePayload(call.parameters, "/day/{id}: id must be a 12-byte hex string") { ObjectId.isValid(this["id"]) }

            val id = ObjectId(hexString)

            when (val day = dayRepo[id]) {
                null -> call.respond(HttpStatusCode.NotFound)
                else -> call.respond(HttpStatusCode.OK, day)
            }
        }

        options {
            call.response.headers.append("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
            call.respond(HttpStatusCode.NoContent)
        }
        post {
            try {
                val day = call.receive<DayCreationDTO>()

                sanitizePayload(day, "user must be a valid 12-byte hex string") { ObjectId.isValid(user) }

                val createdDay = dayRepo.create(
                    with(day) {
                        DayCreate(
                            Date(date),
                            User(user),
                            occurrences.map { Occurrence(it) },
                            quality
                        )
                    }
                )

                call.respond(HttpStatusCode.Created, createdDay)

                dayProducer.send(
                    ProducerRecord("day", createdDay.user to createdDay.date, createdDay)
                )

                dayProducer.close()
            } catch (ex: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("Bad Request", ex.rootCause().message))
            }
        }

        delete("{id?}") {
            val hexString = call.parameters["id"]

                val id = ObjectId(hexString)

                if (id !in dayRepo) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }

                val status = dayRepo.delete(id)
                @Serializable
                data class DeleteResponse(@Serializable(ObjectIdSerializer::class) val id: ObjectId, val deleted: Boolean)

                call.respond(HttpStatusCode.OK, DeleteResponse(id, status))
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}
