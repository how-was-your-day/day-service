package repo

import org.bson.Document

interface MongoObjectMapper<T> {
    fun map(doc: Document) : T
    fun unmap(obj: T) : Document
}