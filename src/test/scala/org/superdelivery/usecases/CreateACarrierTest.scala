package org.superdelivery.usecases

import munit.FunSuite
import org.superdelivery.Data
import org.superdelivery.Data.command
import org.superdelivery.model.{Carrier, CarrierId}
import org.superdelivery.repositories.InMemoryCarrierRepository

class CreateACarrierTest extends FunSuite {
  private val repository = new InMemoryCarrierRepository
  private val sut        = new CreateACarrier(repository)

  test("return a Carrier with slugified name as ID and same properties") {
    val result = sut.handle(command)

    val expected = Carrier(
      carrierId = CarrierId("john-express"),
      name = "John express",
      workingTimeslot = command.workingTimeslot,
      workingArea = command.workingArea,
      maxWeight = command.maxWeight,
      maxVolume = command.maxVolume,
      maxPacketWeight = command.maxPacketWeight,
      speed = command.speed,
      cost = command.cost
    )
    assertEquals(result, Right(expected))
  }

  test("save created carrier in DB") {
    sut.handle(command)

    val expected = Some(
      Carrier(
        carrierId = CarrierId("john-express"),
        name = "John express",
        workingTimeslot = command.workingTimeslot,
        workingArea = command.workingArea,
        maxWeight = command.maxWeight,
        maxVolume = command.maxVolume,
        maxPacketWeight = command.maxPacketWeight,
        speed = command.speed,
        cost = command.cost
      )
    )
    assertEquals(repository.get(CarrierId("john-express")), expected)
  }

  test("reject if a carrier with same ID exists") {
    sut.handle(Data.command)

    val result = sut.handle(Data.command.copy(maxVolume = 100))

    val expected = Left("A carrier with same ID already exists")
    assertEquals(result, expected)
  }

}
