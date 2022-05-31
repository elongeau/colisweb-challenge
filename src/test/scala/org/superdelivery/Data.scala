package org.superdelivery

import org.superdelivery.model.{Area, Carrier, CarrierId, Point, Timeslot}
import org.superdelivery.usecases.CreateACarrier

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
}
