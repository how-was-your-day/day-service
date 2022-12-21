import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val insert1 = launch {
            println("insert 1")
            DB.insert(1)
        }
        val insert2 = launch {
            println("insert 2")
            DB.insert(2)
        }

        insert1.join()
        insert2.join()
        println("finished inserts")

        val moods = async {
            println("read")
            DB.read()
        }.await()

        println("finished read")

        println(moods)

        moods.forEach {
            println(it)
        }

        launch {
            println("delete")
            DB.deleteAll()
        }
    }
}