package org.superdelivery.usecases

import org.superdelivery.model.{Area, Carrier, CarrierId, Timeslot, VolumeInCubeMeter, WeightInKg}
import org.superdelivery.usecases.GetCarriersByCategory.GetCarriersByCategoryQuery

class GetCarriersByCategory(carrierRepository: InMemoryDB[CarrierId, Carrier]) {
  def handle(query: GetCarriersByCategoryQuery): List[Carrier] = ???
}

object GetCarriersByCategory {
  case class GetCarriersByCategoryQuery(
    deliveryRange: Timeslot,
    deliveryArea: Area,
    maxWeight: WeightInKg,
    maxPacketWeight: WeightInKg,
    maxVolume: VolumeInCubeMeter
  )
}
