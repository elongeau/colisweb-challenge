package org.superdelivery.domain.usecases

import org.superdelivery.domain.model.Compatibilities.Compatibility
import org.superdelivery.domain.model.{Area, CarrierId, Compatibilities, Timeslot, VolumeInCubeMeter, WeightInKg}
import org.superdelivery.domain.repositories.CarrierRepository
import org.superdelivery.domain.usecases.GetCarriersForACategory.{Query, Result}
import org.superdelivery.domain.utils.Haversine

class GetCarriersForACategory(carrierRepository: CarrierRepository) {
  def handle(query: Query): List[Result] = carrierRepository.getAll.map { carrier =>
    val distanceCompatibility = {
      val distance    = Haversine.distanceInKm(carrier.workingArea.point, query.deliveryArea.point)
      val maxDistance = distance + query.deliveryArea.radius
      Compatibilities.from(maxDistance <= carrier.workingArea.radius)
    }

    val timeSlotCompatibility = Compatibilities.from(carrier.workingTimeslot.contains(query.deliveryTimeslot))

    val packagingCompatibility = Compatibilities.from(
      carrier.maxWeight >= query.maxWeight &&
        carrier.maxPacketWeight >= query.maxPacketWeight &&
        carrier.maxVolume >= query.maxVolume
    )

    Result(carrier.carrierId, distanceCompatibility <+> timeSlotCompatibility <+> packagingCompatibility)
  }.sortBy(_.compatibility)

}

object GetCarriersForACategory {
  case class Query(
    deliveryTimeslot: Timeslot,
    deliveryArea: Area,
    maxWeight: WeightInKg,
    maxPacketWeight: WeightInKg,
    maxVolume: VolumeInCubeMeter
  )

  case class Result(carrier: CarrierId, compatibility: Compatibility)
}
