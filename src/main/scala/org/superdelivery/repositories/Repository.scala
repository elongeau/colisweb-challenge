package org.superdelivery.repositories

trait Repository[K, V] {
  def get(key: K): Option[V]
  def getAll: List[V]
  def save(entity: V): Unit
}
