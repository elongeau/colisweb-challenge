package org.superdelivery.usecases

import munit.FunSuite
import org.superdelivery.Commands.CreateCarrierCommand
import org.superdelivery.model.{Area, Carrier, CarrierId, Point, Timeslot}

import java.time.LocalTime

class CreateACarrierTest extends FunSuite {
  private val sut = new CreateACarrier()
  test("return a Carrier with slugified name as ID and same properties") {
    val result = sut.handle(
      CreateCarrierCommand(
        name = "John express",
        workingRange =
          Timeslot(LocalTime.parse("09:00"), LocalTime.parse("18:00")),
        workingArea = Area(Point(43.2969901, 5.3789783), 42),
        maxWeight = 50,
        maxVolume = 40,
        maxPacketWeight = 30,
        speed = 20,
        cost = 10
      )
    )

    assertEquals(
      result,
      Carrier(
        carrierId = CarrierId("john-express"),
        name = "John express",
        workingRange =
          Timeslot(LocalTime.parse("09:00"), LocalTime.parse("18:00")),
        workingArea = Area(Point(43.2969901, 5.3789783), 42),
        maxWeight = 50,
        maxVolume = 40,
        maxPacketWeight = 30,
        speed = 20,
        cost = 10
      )
    )
  }
}
