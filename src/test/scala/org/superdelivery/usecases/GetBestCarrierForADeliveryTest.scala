package org.superdelivery.usecases

import munit.FunSuite
import org.superdelivery.Data
import org.superdelivery.model.{Carrier, CarrierId, Point, Timeslot}
import org.superdelivery.repositories.InMemoryCarrierRepository
import org.superdelivery.usecases.GetBestCarrierForADelivery.Query

import java.time.LocalTime

class GetBestCarrierForADeliveryTest extends FunSuite {
  private val repository = new InMemoryCarrierRepository
  private val sut        = new GetBestCarrierForADelivery(repository)

  override def beforeEach(context: BeforeEach): Unit = repository.clear()

  test("should return carrier when the only carrier match all criteria") {
    repository.save(Data.defaultCarrier)

    val result = sut.handle(Data.defaultGetBestCarrierQuery)

    assertEquals(result, Some(Data.defaultCarrier.carrierId))
  }

  private val updatePickup: Query => Query   = _.copy(pickupPoint = Point(48.891305, 2.3529867))
  private val updateShipping: Query => Query = _.copy(shippingPoint = Point(48.891305, 2.3529867))

  List(
    ("the pickup", updatePickup),
    ("the shipping", updateShipping),
    ("both", updatePickup.andThen(updateShipping))
  ).foreach { case (label, updateFn) =>
    test(s"should return nothing when the carrier working area does not contain $label point(s) of the delivery") {
      repository.save(Data.defaultCarrier)

      val result = sut.handle(updateFn(Data.defaultGetBestCarrierQuery))

      assertEquals(result, None)
    }
  }

  test(s"should return nothing when the carrier working timeslot does not contain the timeslot of the delivery") {
    repository.save(Data.defaultCarrier)

    val result = sut.handle(
      Data.defaultGetBestCarrierQuery.copy(
        timeslot = Timeslot(LocalTime.parse("19:00"), LocalTime.parse("20:00"))
      )
    )

    assertEquals(result, None)
  }

  List(
    "08:00" -> "14:00",
    "10:00" -> "12:00",
    "16:00" -> "20:00"
  ).map { case (start, end) =>
    Timeslot(LocalTime.parse(start), LocalTime.parse(end))
  }.foreach { timeslot =>
    test(s"should return carrier when the carrier working timeslot overlap the timeslot of the delivery: $timeslot") {
      repository.save(Data.defaultCarrier)

      val result = sut.handle(Data.defaultGetBestCarrierQuery.copy(timeslot = timeslot))

      assertEquals(result, Some(Data.defaultCarrier.carrierId))
    }
  }

  private val updateMaxWeight: Carrier => Carrier       = _.copy(maxWeight = 10)
  private val updateMaxPacketWeight: Carrier => Carrier = _.copy(maxPacketWeight = 5)
  private val updateMaxVolume: Carrier => Carrier       = _.copy(maxVolume = 1)
  List(
    ("max weight", updateMaxWeight),
    ("max packet weight", updateMaxPacketWeight),
    ("max volume", updateMaxVolume)
  ).foreach { case (label, updateFn) =>
    test(s"should return nothing when packaging does not fit $label") {
      repository.save(updateFn(Data.defaultCarrier))

      val result = sut.handle(Data.defaultGetBestCarrierQuery)

      assertEquals(result, None)
    }
  }

  test("should return one of carriers when both are equivalent and match all criteria") {
    repository.save(Data.defaultCarrier)
    repository.save(
      Data.defaultCarrier.copy(
        carrierId = CarrierId("another-one")
      )
    )

    val result = sut.handle(Data.defaultGetBestCarrierQuery)

    assert(result.isDefined)
  }

  test("should return cheapest carrier when both are equivalent and match all criteria") {
    repository.save(Data.defaultCarrier)
    repository.save(
      Data.defaultCarrier.copy(
        carrierId = CarrierId("cheapest"),
        cost = 1
      )
    )

    val result = sut.handle(Data.defaultGetBestCarrierQuery)

    assertEquals(result, Some(CarrierId("cheapest")))
  }

  test("should return carrier with bonus when both match all criteria but one can deliver in time") {
    repository.save(
      Data.defaultCarrier.copy(
        speed = 0.1
      )
    )
    repository.save(
      Data.defaultCarrier.copy(
        carrierId = CarrierId("with-bonus")
      )
    )

    val result = sut.handle(Data.defaultGetBestCarrierQuery)

    assertEquals(result, Some(CarrierId("with-bonus")))
  }
}
