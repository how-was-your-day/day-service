import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import kotlinx.coroutines.async
import kotlinx.html.*
import repo.day.DayMapper
import repo.day.DayRepo

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

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
