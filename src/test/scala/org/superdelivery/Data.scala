package org.superdelivery

import org.superdelivery.domain.model.{Area, Carrier, CarrierId, Packet, Point, Timeslot}
import org.superdelivery.domain.usecases.{CreateACarrier, GetBestCarrierForADelivery}

import java.time.LocalTime

object Data {
  lazy val command: CreateACarrier.Command = CreateACarrier.Command(
    name = "John express",
    workingTimeslot = Timeslot(LocalTime.parse("09:00"), LocalTime.parse("18:00")),
    workingArea = Area(Point(43.2969901, 5.3789783), 42),
    maxWeight = 200,
    maxVolume = 12,
    maxPacketWeight = 20,
    speed = 50,
    cost = 15
  )

  lazy val defaultCarrier: Carrier = Carrier(
    carrierId = CarrierId("john-express"),
    name = "John express",
    workingTimeslot = Timeslot(LocalTime.parse("09:00"), LocalTime.parse("18:00")),
    workingArea = Area(Point(43.2969901, 5.3789783), 10),
    maxWeight = 200,
    maxVolume = 12,
    maxPacketWeight = 20,
    speed = 50,
    cost = 15
  )

  lazy val defaultGetBestCarrierQuery: GetBestCarrierForADelivery.Query = GetBestCarrierForADelivery.Query(
    pickupPoint = Data.defaultCarrier.workingArea.point,
    shippingPoint = Point(43.2978255, 5.3771758),
    timeslot = Timeslot(LocalTime.parse("09:00"), LocalTime.parse("10:00")),
    packets = List(
      Packet(10, 2),
      Packet(5, 3)
    )
  )
}
