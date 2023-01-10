package repo

interface Repo<T, TCreate, ID> {
    operator fun get(id: ID) : T?
    fun delete(id: ID) : Boolean
    fun update(id: ID, newValue: T) : T
    fun create(value : TCreate) : T
    fun all(): List<T>
    operator fun contains(id: ID): Boolean
}
