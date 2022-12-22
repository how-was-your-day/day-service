import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import model.Day
import model.Quality
import model.serializers.ObjectIdSerializer
import org.bson.types.ObjectId
import repo.Repo

fun Route.dayRoute(dayRepo: Repo<Day, ObjectId>) {
    route("/day") {
        get {
            val days = dayRepo.all()

            call.respond(HttpStatusCode.OK, days)
        }

        get("{id?}") {
            val hexString = call.parameters["id"]

            if (ObjectId.isValid(hexString)) {
                val id = ObjectId(hexString)

                when (val day = dayRepo[id]) {
                    null -> call.respond(HttpStatusCode.NotFound)
                    else -> call.respond(HttpStatusCode.OK, day)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        @Serializable
        data class DayCreationObject(val date: Long, val occurrences: List<String>, val quality: Quality)
        post {
            val day = call.receive<DayCreationObject>()

            val createdDay = dayRepo.create(
                mapOf(
                    "date" to day.date,
                    "occurrences" to day.occurrences,
                    "quality" to day.quality
                )
            )

            call.respond(HttpStatusCode.Created, createdDay)
        }

        delete("{id?}") {
            val hexString = call.parameters["id"]

            if (ObjectId.isValid(hexString)) {
                val id = ObjectId(hexString)

                if (id !in dayRepo) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }

                val status = dayRepo.delete(id)
                @Serializable data class DeleteResponse(@Serializable(ObjectIdSerializer::class) val id: ObjectId, val deleted: Boolean)

                call.respond(HttpStatusCode.OK, DeleteResponse(id, status))
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}