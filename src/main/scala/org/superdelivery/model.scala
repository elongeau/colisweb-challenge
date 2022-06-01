package org.superdelivery

import upickle.default._

import java.time.{Duration, LocalTime}

object model extends Serializer {
  case class Carrier(
    carrierId: CarrierId,
    name: String,
    workingTimeslot: Timeslot,
    workingArea: Area,
    maxWeight: WeightInKg,
    maxVolume: VolumeInCubeMeter,
    maxPacketWeight: WeightInKg,
    speed: SpeedInKmH,
    cost: MoneyInCents
  )
  object Carrier {
    implicit val rw: ReadWriter[Carrier] = macroRW
  }

  case class CarrierId(id: String)
  object CarrierId {
    implicit val rw: ReadWriter[CarrierId] = macroRW
  }

  case class Packets(packets: List[Packet])
  object Packets {
    implicit val rw: ReadWriter[Packets] = macroRW
  }

  case class Packet(weight: WeightInKg, volume: VolumeInCubeMeter)
  object Packet {
    implicit val rw: ReadWriter[Packet] = macroRW
  }

  private[model] implicit class LocalTimeExtensions(time: LocalTime) {
    def isBeforeOrEquals(other: LocalTime): Boolean = time.equals(other) || time.isBefore(other)
    def isAfterOrEquals(other: LocalTime): Boolean  = time.equals(other) || time.isAfter(other)
  }

  case class Timeslot(start: LocalTime, end: LocalTime) {
    def overlap(other: Timeslot): Boolean = containsTime(other.start) || containsTime(other.end)

    def contains(other: Timeslot): Boolean = start.isBeforeOrEquals(other.start) && end.isAfterOrEquals(other.end)

    lazy val duration: Duration = Duration.between(start, end)

    private def containsTime(time: LocalTime): Boolean = start.isBeforeOrEquals(time) && end.isAfterOrEquals(time)
  }

  object Timeslot {
    implicit val rw: ReadWriter[Timeslot] = macroRW
  }

  case class Area(point: Point, radius: DistanceInKm)
  object Area {
    implicit val rw: ReadWriter[Area] = macroRW
  }

  type VolumeInCubeMeter = Double

  /** 2€ = 200 cents */
  type MoneyInCents = Int

  object Compatibilities extends Enumeration {
    type Compatibility = Value
    val FULL, PARTIAL, NONE = Value
    implicit val rw: ReadWriter[Compatibility] = readwriter[String].bimap(
      f = _.toString,
      g = s => Compatibilities.values.find(v => v.toString == s.toUpperCase).get
    )

    def from(b: Boolean): Compatibility = if (b) FULL else NONE

    implicit class CompatibilityExtension(compatibility: Compatibility) {
      def <+>(other: Compatibility): Compatibility = (compatibility, other) match {
        case (FULL, NONE) => PARTIAL
        case (FULL, x)    => x
        case (PARTIAL, _) => PARTIAL
        case (NONE, NONE) => NONE
        case (NONE, _)    => PARTIAL
      }
    }
  }

  // From ColisWeb OpenSource
  //  https://gitlab.com/colisweb-idl/colisweb-open-source/scala/scala-distances/-/blob/master/core/src/main/scala/com/colisweb/distances/model/Point.scala
  case class Point(latitude: Latitude, longitude: Longitude) {
    def toRadians: Point =
      Point(math.toRadians(latitude), math.toRadians(longitude))
  }
  object Point {
    implicit val rw: ReadWriter[Point] = macroRW
  }

  // https://gitlab.com/colisweb-idl/colisweb-open-source/scala/scala-distances/-/blob/master/core/src/main/scala/com/colisweb/distances/model/package.scala
  type SpeedInKmH    = Double
  type WeightInKg    = Double
  type DimensionInCm = Double

  type DistanceInKm      = Double
  type DurationInSeconds = Long

  type Latitude  = Double
  type Longitude = Double
}
