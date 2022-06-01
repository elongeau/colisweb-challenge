package org.superdelivery.usecases

import org.superdelivery.model.Compatibilities.Compatibility
import org.superdelivery.model.{
  Area,
  Carrier,
  CarrierId,
  Compatibilities,
  DistanceInKm,
  Point,
  Timeslot,
  VolumeInCubeMeter,
  WeightInKg
}
import org.superdelivery.repositories.Repository
import org.superdelivery.usecases.GetCarriersForACategory.{Query, Result}
import upickle.default._

class GetCarriersForACategory(carrierRepository: Repository[CarrierId, Carrier]) {
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
  object Result {
    implicit val rw: ReadWriter[Result] = macroRW
  }

}
