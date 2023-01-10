package repo

import dao.DayCreate
import dao.DayDAO
import dao.documentOf
import model.Day
import org.bson.types.ObjectId

interface DayRepo : Repo<Day, DayCreate, ObjectId> {
    fun findByUser(id: ObjectId): List<Day>
}

class DayRepoImpl(private val dayDAO: DayDAO) : DayRepo {
    override fun findByUser(id: ObjectId): List<Day> = dayDAO.findMany(documentOf("user" to id))

    override fun get(id: ObjectId): Day? = dayDAO.findOne(documentOf("_id" to id))

    fun delete(obj: Day): Boolean = delete(obj.id)
    override fun delete(id: ObjectId): Boolean = dayDAO.delete(id)

    override fun update(id: ObjectId, newValue: Day): Day {
        TODO("Not yet implemented")
    }

    override fun create(value: DayCreate): Day = dayDAO.create(value)

    override fun all(): List<Day> = dayDAO.all()

    override fun contains(id: ObjectId): Boolean {
        return this[id] != null
    }

}
