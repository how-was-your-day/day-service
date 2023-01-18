package api

import DayProducer
import api.v1.day.dayRoute
import dao.DayDAO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import repo.DayRepoImpl

fun Application.configureRouting() {
    routing {
        install(CORS) {
            anyHost()
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Patch)
            allowMethod(HttpMethod.Options)
            allowHeader(HttpHeaders.Accept)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            allowCredentials = true
        }

        dayRoute(DayRepoImpl(DayDAO()), DayProducer())
    }
}

@Serializable
data class ErrorBody(val error: String, val cause: String?)

fun Throwable.rootCause() : Throwable {
    var currCause: Throwable = this
    while(currCause.cause != null)
        currCause = currCause.cause!!
    return currCause
}

suspend inline fun <D> PipelineContext<Unit, ApplicationCall>.sanitizePayload(payload: D, message: String, block: D.() -> Boolean) {
    val valid = payload.let(block)

    if (!valid) {
        call.respond(HttpStatusCode.BadRequest, ErrorBody("Bad Request", message))
        return
    }
}

fun Parameters.firstOf(vararg ss: String): String? {
    for (s in ss) {
        val params = get(s)
        if (params != null) return params
    }
    return null
}