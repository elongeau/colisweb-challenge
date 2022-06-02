package org.superdelivery.domain.usecases

import munit.FunSuite
import org.superdelivery.Data
import org.superdelivery.Data.command
import org.superdelivery.domain.model.{Carrier, CarrierId}
import org.superdelivery.domain.repositories.CarrierRepository
import org.superdelivery.infrastructure.repositories.InMemoryCarrierRepository

class CreateACarrierTest extends FunSuite {
  private val fixture: FunFixture[(CreateACarrier, CarrierRepository)] =
    FunFixture.apply[(CreateACarrier, CarrierRepository)](
      setup = _ => {
        val repository = new InMemoryCarrierRepository
        val sut        = new CreateACarrier(repository)
        (sut, repository)
      },
      teardown = _ => ()
    )

  fixture.test("return a Carrier with slugified name as ID and same properties") { case (sut, _) =>
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

  fixture.test("save created carrier in DB") { case (sut, repository) =>
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

  fixture.test("reject if a carrier with same ID exists") { case (sut, _) =>
    sut.handle(Data.command)

    val result = sut.handle(Data.command.copy(maxVolume = 100))

    val expected = Left("A carrier with same ID already exists")
    assertEquals(result, expected)
  }

}
