package org.superdelivery

import upickle.default._

import java.time.LocalTime
import java.util.UUID

object model extends Serializer {
  case class Carrier(
    carrierId: CarrierId,
    name: String,
    workingRange: Timeslot,
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

  case class Delivery(
    deliveryId: DeliveryId,
    pickupPoint: Point,
    shippingPoint: Point,
    timeslot: Timeslot,
    packets: Packets
  )
  object Delivery {
    implicit val rw: ReadWriter[Delivery] = macroRW
  }

  case class Packets(packets: List[Packet])
  object Packets {
    implicit val rw: ReadWriter[Packets] = macroRW
  }

  case class Packet(weight: WeightInKg, volume: VolumeInCubeMeter)
  object Packet {
    implicit val rw: ReadWriter[Packet] = macroRW
  }

  case class DeliveryId(id: UUID)
  object DeliveryId {
    implicit val rw: ReadWriter[DeliveryId] = macroRW
  }

  case class Timeslot(start: LocalTime, end: LocalTime)
  object Timeslot {
    implicit val rw: ReadWriter[Timeslot] = macroRW
  }

  case class Area(point: Point, radius: DistanceInKm)
  object Area {
    implicit val rw: ReadWriter[Area] = macroRW
  }

  type VolumeInCubeMeter = Double

  /** ℹ️ 2€ = 200 cents */
  type MoneyInCents = Int

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
