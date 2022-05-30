package org.superdelivery.usecases

import munit.FunSuite
import org.superdelivery.Data
import org.superdelivery.Data.command
import org.superdelivery.model.{Area, Carrier, CarrierId, Point, Timeslot}

import java.time.LocalTime

class CreateACarrierTest extends FunSuite {
  private val repository = new InMemoryDB[CarrierId, Carrier](_.carrierId)
  private val sut = new CreateACarrier(repository)
  test("return a Carrier with a random ID and same properties") {
    val result = sut.handle(command)

    val expected = Carrier(
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
    assertEquals(result, Right(expected))
  }

  test("save created carrier in DB") {
    sut.handle(command)

    val expected = Some(
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
    assertEquals(repository.get(CarrierId("john-express")), expected)
  }

  test("reject if a carrier with same ID exists") {
    sut.handle(Data.command)

    val result = sut.handle(
      Data.command.copy(
        maxVolume = 100
      )
    )

    val expected = Left("A carrier with same ID already exists")
    assertEquals(result, expected)
  }

}
