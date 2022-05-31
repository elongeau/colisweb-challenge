package org.superdelivery

import upickle.default._

import java.time.LocalTime

trait Serializer {
  implicit val localTimeReader: ReadWriter[LocalTime] = upickle.default
    .readwriter[String]
    .bimap(
      f = _.toString,
      g = {
        case s: String => LocalTime.parse(s)
        case x         => throw new RuntimeException(s"Invalid time $x")
      }
    )

}
