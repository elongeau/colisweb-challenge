package org.superdelivery.domain.usecases

import org.superdelivery.domain.model
import org.superdelivery.domain.model.Compatibilities.{Compatibility, NONE}
import org.superdelivery.domain.model.{Area, Compatibilities, Timeslot, VolumeInCubeMeter, WeightInKg}
import org.superdelivery.domain.utils.Haversine

case class DeliveryCategory(
  deliveryTimeslot: Timeslot,
  deliveryArea: Area,
  maxWeight: WeightInKg,
  maxPacketWeight: WeightInKg,
  maxVolume: VolumeInCubeMeter
) {
  def matchDistance(carrier: model.Carrier): Compatibility = {
    val distance    = Haversine.distanceInKm(carrier.workingArea.point, deliveryArea.point)
    val maxDistance = distance + deliveryArea.radius
    Compatibilities.from(maxDistance <= carrier.workingArea.radius)
  }
}
