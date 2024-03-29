package org.superdelivery.infrastructure.repositories

import org.superdelivery.domain.repositories.Repository

class InMemoryRepository[K, V](key: V => K) extends Repository[K, V] {
  private[this] var db = Map.empty[K, V]

  def get(key: K): Option[V] = db.get(key)

  def save(value: V): Unit = db = db.updated(key(value), value)

  def getAll: List[V] = db.values.toList

  def clear(): Unit = db = Map.empty
}
