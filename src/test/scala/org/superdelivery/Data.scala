package org.superdelivery

import org.superdelivery.model.{Area, Point, Timeslot}
import org.superdelivery.usecases.CreateACarrier

import java.time.LocalTime

object Data {
  lazy val command: CreateACarrier.Command = CreateACarrier.Command(
    name = "John express",
    workingRange = Timeslot(LocalTime.parse("09:00"), LocalTime.parse("18:00")),
    workingArea = Area(Point(43.2969901, 5.3789783), 42),
    maxWeight = 50,
    maxVolume = 40,
    maxPacketWeight = 30,
    speed = 20,
    cost = 10
  )

}
