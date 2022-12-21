package repo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.typesafe.config.Config

import com.typesafe.config.ConfigFactory

interface Repo<T, ID> {
    operator fun get(id: ID) : T?
    fun delete(id: ID) : Boolean
    fun update(id: ID, newValue: T) : T
    fun create(value : Map<String, Any>) : T
    fun all(): List<T>
}

private val conf: Config = ConfigFactory.load()

abstract class MongoRepo<T, ID> : Repo<T, ID> {
    private val CONN_STRING = "${conf.getString("mongo.address.host")}:${conf.getString("mongo.address.port")}"

    private val clientSettings: MongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(CONN_STRING))
        .retryWrites(true)
        .build()

    fun useClient(block: MongoClient.() -> Unit) {
        lateinit var mongoClient : MongoClient
        try {
            mongoClient = MongoClients.create(clientSettings)

            mongoClient.apply(block)
        } finally {
            mongoClient.close()
        }
    }

    fun <A> letClient(block: MongoClient.() -> A) : A {
        lateinit var mongoClient : MongoClient
        try {
            mongoClient = MongoClients.create(clientSettings)

            return mongoClient.let(block)
        } finally {
            mongoClient.close()
        }
    }
}