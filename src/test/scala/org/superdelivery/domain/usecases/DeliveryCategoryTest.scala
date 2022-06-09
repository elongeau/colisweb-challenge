package org.superdelivery.domain.usecases

import munit.FunSuite
import org.superdelivery.Data
import org.superdelivery.domain.model.{Area, Compatibilities, Point}

class DeliveryCategoryTest extends FunSuite {
  test("should have FULL compatibility when working area match delivery area") {
    val deliveryCategory = DeliveryCategory(
      deliveryTimeslot = Data.defaultCarrier.workingTimeslot,
      deliveryArea = Data.defaultCarrier.workingArea,
      maxWeight = Data.defaultCarrier.maxWeight,
      maxPacketWeight = Data.defaultCarrier.maxPacketWeight,
      maxVolume = Data.defaultCarrier.maxVolume
    )

    val result = deliveryCategory.matchDistance(Data.defaultCarrier)

    assertEquals(result, Compatibilities.FULL)
  }

  test("should have NONE compatibility when working area match delivery area") {
    val deliveryCategory = DeliveryCategory(
      deliveryTimeslot = Data.defaultCarrier.workingTimeslot,
      deliveryArea = Area(Point(48.891305, 2.3529867), 1),
      maxWeight = Data.defaultCarrier.maxWeight,
      maxPacketWeight = Data.defaultCarrier.maxPacketWeight,
      maxVolume = Data.defaultCarrier.maxVolume
    )

    val result = deliveryCategory.matchDistance(Data.defaultCarrier)

    assertEquals(result, Compatibilities.NONE)
  }

  test("should have FULL compatibility when working area match delivery area") {
    val deliveryCategory = DeliveryCategory(
      deliveryTimeslot = Data.defaultCarrier.workingTimeslot,
      deliveryArea = Data.defaultCarrier.workingArea,
      maxWeight = Data.defaultCarrier.maxWeight,
      maxPacketWeight = Data.defaultCarrier.maxPacketWeight,
      maxVolume = Data.defaultCarrier.maxVolume
    )

    val result = deliveryCategory.matchDistance(Data.defaultCarrier)

    assertEquals(result, Compatibilities.FULL)
  }

}
