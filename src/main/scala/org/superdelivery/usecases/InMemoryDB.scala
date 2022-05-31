package org.superdelivery.usecases

class InMemoryDB[K, V](key: V => K) extends Repository[K, V] {
  private[this] var db = Map.empty[K, V]

  def get(key: K): Option[V] = db.get(key)

  def save(value: V): Unit = db = db.updated(key(value), value)

  def getAll: List[V] = db.values.toList
}
