package org.superdelivery.usecases

import munit.FunSuite
import org.superdelivery.Data
import org.superdelivery.model.Compatibilities.{FULL, NONE, PARTIAL}
import org.superdelivery.model.{Area, Carrier, CarrierId, Point, Timeslot, VolumeInCubeMeter, WeightInKg}
import org.superdelivery.repositories.InMemoryCarrierRepository

import java.time.LocalTime

class GetCarriersByCategoryTest extends FunSuite {
  private val repository = new InMemoryCarrierRepository
  private val sut        = new GetCarriersForACategory(repository)
  test("should return carrier with FULL compatibility when all criteria matches") {
    repository.save(Data.defaultCarrier)

    val value = sut.handle(
      GetCarriersForACategory.Query(
        deliveryTimeslot = Data.defaultCarrier.workingTimeslot,
        deliveryArea = Data.defaultCarrier.workingArea,
        maxWeight = Data.defaultCarrier.maxWeight,
        maxPacketWeight = Data.defaultCarrier.maxPacketWeight,
        maxVolume = Data.defaultCarrier.maxVolume
      )
    )

    assertEquals(
      value,
      List(
        GetCarriersForACategory.Result(Data.defaultCarrier.carrierId, FULL)
      )
    )
  }

  test("should return carrier with PARTIAL compatibility when working area does not match") {
    repository.save(Data.defaultCarrier)

    val value = sut.handle(
      GetCarriersForACategory.Query(
        deliveryTimeslot = Data.defaultCarrier.workingTimeslot,
        deliveryArea = Area(Point(48.891305, 2.3529867), 1),
        maxWeight = Data.defaultCarrier.maxWeight,
        maxPacketWeight = Data.defaultCarrier.maxPacketWeight,
        maxVolume = Data.defaultCarrier.maxVolume
      )
    )

    assertEquals(
      value,
      List(
        GetCarriersForACategory.Result(Data.defaultCarrier.carrierId, PARTIAL)
      )
    )
  }

  List(
    "08:00" -> "14:00",
    "10:00" -> "19:00",
    "18:30" -> "20:00"
  ).map { case (start, end) =>
    Timeslot(LocalTime.parse(start), LocalTime.parse(end))
  }.foreach { timeslot =>
    test(s"should return carrier with PARTIAL compatibility when working timeslot does not contain $timeslot") {
      repository.save(Data.defaultCarrier)

      val value = sut.handle(
        GetCarriersForACategory.Query(
          deliveryTimeslot = timeslot,
          deliveryArea = Data.defaultCarrier.workingArea,
          maxWeight = Data.defaultCarrier.maxWeight,
          maxPacketWeight = Data.defaultCarrier.maxPacketWeight,
          maxVolume = Data.defaultCarrier.maxVolume
        )
      )

      assertEquals(
        value,
        List(
          GetCarriersForACategory.Result(Data.defaultCarrier.carrierId, PARTIAL)
        )
      )
    }
  }

  List[(String, WeightInKg, WeightInKg, VolumeInCubeMeter)](
    ("max weight", Data.defaultCarrier.maxWeight + 1, 0, 0),
    ("max packet weight", 0, Data.defaultCarrier.maxPacketWeight + 1, 0),
    ("max volume", 0, 0, Data.defaultCarrier.maxVolume + 1)
  ).foreach { case (label, maxWeight, maxPacketWeight, maxVolume) =>
    test(s"should return carrier with PARTIAL compatibility when packaging does not fit $label") {
      repository.save(Data.defaultCarrier)

      val value = sut.handle(
        GetCarriersForACategory.Query(
          deliveryTimeslot = Data.defaultCarrier.workingTimeslot,
          deliveryArea = Data.defaultCarrier.workingArea,
          maxWeight = maxWeight,
          maxPacketWeight = maxPacketWeight,
          maxVolume = maxVolume
        )
      )

      assertEquals(
        value,
        List(
          GetCarriersForACategory.Result(Data.defaultCarrier.carrierId, PARTIAL)
        )
      )
    }
  }

  val updateArea: GetCarriersForACategory.Query => GetCarriersForACategory.Query = _.copy(
    deliveryArea = Area(Point(48.891305, 2.3529867), 1)
  )

  val updateTimeslot: GetCarriersForACategory.Query => GetCarriersForACategory.Query = _.copy(
    deliveryTimeslot = Timeslot(LocalTime.parse("07:00"), LocalTime.parse("10:00"))
  )

  val updatePackaging: GetCarriersForACategory.Query => GetCarriersForACategory.Query = _.copy(
    maxWeight = Data.defaultCarrier.maxWeight + 1
  )

  List(
    ("area and timeslot", updateArea.andThen(updateTimeslot)),
    ("area and packaging", updateArea.andThen(updatePackaging)),
    ("packaging and timeslot", updatePackaging.andThen(updateTimeslot))
  ).foreach { case (label, updateFn) =>
    test(s"should return carrier with PARTIAL compatibility when carrier does not match category on $label") {
      repository.save(Data.defaultCarrier)
      val query = GetCarriersForACategory.Query(
        deliveryTimeslot = Data.defaultCarrier.workingTimeslot,
        deliveryArea = Data.defaultCarrier.workingArea,
        maxWeight = Data.defaultCarrier.maxWeight,
        maxPacketWeight = Data.defaultCarrier.maxPacketWeight,
        maxVolume = Data.defaultCarrier.maxVolume
      )

      val value = sut.handle(updateFn(query))

      assertEquals(
        value,
        List(
          GetCarriersForACategory.Result(Data.defaultCarrier.carrierId, PARTIAL)
        )
      )
    }
  }

  test(s"should return carrier with NONE compatibility when carrier does not match any criteria") {
    repository.save(Data.defaultCarrier)

    val value = sut.handle(
      GetCarriersForACategory.Query(
        deliveryTimeslot = Timeslot(LocalTime.parse("07:00"), LocalTime.parse("10:00")),
        deliveryArea = Area(Point(48.891305, 2.3529867), 1),
        maxWeight = Data.defaultCarrier.maxWeight + 1,
        maxPacketWeight = Data.defaultCarrier.maxPacketWeight + 1,
        maxVolume = Data.defaultCarrier.maxVolume + 1
      )
    )

    assertEquals(
      value,
      List(
        GetCarriersForACategory.Result(Data.defaultCarrier.carrierId, NONE)
      )
    )
  }

  test(s"should return carriers sorted by decreasing compatibility") {
    repository.save(
      Carrier(
        carrierId = CarrierId("marcus-chrono"),
        name = "marcus chrono",
        workingTimeslot = Timeslot(
          LocalTime.parse("09:00"),
          LocalTime.parse("14:00")
        ),
        workingArea = Area(Point(43.2969901, 5.3789783), 10),
        maxWeight = 200,
        maxVolume = 12,
        maxPacketWeight = 20,
        speed = 50,
        cost = 13
      )
    )

    repository.save(
      Carrier(
        carrierId = CarrierId("julia-truck"),
        name = "julia truck",
        workingTimeslot = Timeslot(
          LocalTime.parse("09:00"),
          LocalTime.parse("14:00")
        ),
        workingArea = Area(Point(43.2969901, 5.3789783), 8),
        maxWeight = 120,
        maxVolume = 12,
        maxPacketWeight = 20,
        speed = 50,
        cost = 14
      )
    )
    repository.save(Data.defaultCarrier)

    val value = sut.handle(
      GetCarriersForACategory.Query(
        deliveryTimeslot = Timeslot(
          LocalTime.parse("10:00"),
          LocalTime.parse("16:00")
        ),
        deliveryArea = Area(Point(43.3321852, 5.3880718), 5),
        maxWeight = 130,
        maxPacketWeight = 15,
        maxVolume = 1
      )
    )

    assertEquals(
      value,
      List(
        GetCarriersForACategory.Result(Data.defaultCarrier.carrierId, FULL),
        GetCarriersForACategory.Result(CarrierId("marcus-chrono"), PARTIAL),
        GetCarriersForACategory.Result(CarrierId("julia-truck"), NONE)
      )
    )
  }

}
