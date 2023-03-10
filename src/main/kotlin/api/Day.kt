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
import model.Day
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
@Serializable data class DayUpdateDTO(val id: String, val date: Long, val user: String, val occurrences: List<String>, val quality: Quality) {
    fun toDay() : Day = Day(ObjectId(id), Date(date), User(user), occurrences.map { Occurrence(it) }, quality)
}
@Serializable data class DayDTO(val id: String, val user: String, val date: Long, val occurrences: List<String>, val quality: Quality) {
    constructor(day: Day) : this(day.id.toHexString(), day.user.id.toHexString(), day.date.time, day.occurrences.map { it.text }, day.quality)
}

private suspend fun ApplicationCall.respond(statusCode: HttpStatusCode, day: Day) {
    respond(statusCode, DayDTO(day))
}
private suspend fun ApplicationCall.respond(statusCode: HttpStatusCode, days: List<Day>) {
    respond(statusCode, List(days.size) { DayDTO(days[it]) })
}

fun Route.dayRoute(dayRepo: DayRepo, dayProducer: DayProducer) {
    route("/day") {
        options {
            call.response.headers.append("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
            call.respond(HttpStatusCode.NoContent)
        }

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

        route("{id?}") {
            options {
                call.response.headers.append("Access-Control-Allow-Methods", "GET, OPTIONS, PUT, DELETE")
                call.respond(HttpStatusCode.NoContent)
            }
            get {
                val hexString = call.parameters["id"]

                sanitizePayload(call.parameters, "/day/{id}: id must be a 12-byte hex string") { ObjectId.isValid(this["id"]) }

                val id = ObjectId(hexString)

                when (val day = dayRepo[id]) {
                    null -> call.respond(HttpStatusCode.NotFound)
                    else -> call.respond(HttpStatusCode.OK, day)
                }
            }

            put {
                val hexString = call.parameters["id"]

                if (ObjectId.isValid(hexString)) {
                    val id = ObjectId(hexString)

                    if (id !in dayRepo) {
                        call.respond(HttpStatusCode.NotFound)
                        return@put
                    }

                    val newDay = call.receive<DayUpdateDTO>()

                    dayRepo.update(id, newDay.toDay()).onSuccess {
                        call.respond(HttpStatusCode.OK, it)
                    }.onFailure {
                        if (it.message == null) {
                            call.respond(HttpStatusCode.InternalServerError)
                        } else {
                            call.respondText(it.message!!, status = HttpStatusCode.InternalServerError)
                        }

                    }

                } else {
                    call.respondText("$hexString is not a valid ID", status = HttpStatusCode.BadRequest)
                }
            }

            delete {
                val hexString = call.parameters["id"]

                if (ObjectId.isValid(hexString)) {
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
}
