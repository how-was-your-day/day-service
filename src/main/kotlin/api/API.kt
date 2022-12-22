package api

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import kotlinx.serialization.Serializable
import repo.day.DayMapper
import repo.day.DayRepo

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title {
                        +"Mood Tracker"
                    }
                }
                body {
                    h1 {
                        +"Hello World!"
                    }
                }
            }
        }

        dayRoute(DayRepo(DayMapper()))
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