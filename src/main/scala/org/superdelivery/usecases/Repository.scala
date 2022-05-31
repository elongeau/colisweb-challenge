package org.superdelivery.usecases

trait Repository[K, T] {
  def get(key: K): Option[T]
  def save(entity: T): Unit
}
