package dao

import org.bson.Document
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

interface DAO <T, TCreate, TFilter, ID> {
    fun create(tc: TCreate) : T

    fun read(id: ID) : T?
    fun all() : List<T>
    fun findOne(filter: TFilter) : T?
    fun findMany(filter: TFilter) : List<T>

    fun update(t: T) : T

    fun delete(id: ID) : Boolean
}

fun Any.toDocument() : Document {
    val doc = Document()

    val klass = this::class

    val props = klass.declaredMemberProperties as Collection<KProperty1<Any, Any>>

    for (prop in props) {
        doc[prop.name] = prop.get(this)
    }

    return doc
}

fun documentOf(vararg pairs: Pair<String, Any>) : Document = Document(mapOf(*pairs))