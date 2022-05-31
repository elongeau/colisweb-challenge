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
import org.superdelivery.usecases.GetCarriersByCategory.{Query, Result}
import upickle.default._

class GetCarriersByCategory(carrierRepository: Repository[CarrierId, Carrier]) {
  def handle(query: Query): List[Result] = carrierRepository.getAll.map { carrier =>
    val distanceCompatibility = {
      val distance    = distanceInKm(carrier.workingArea.point, query.deliveryArea.point)
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

  def distanceInKm(origin: Point, destination: Point): DistanceInKm = {
    import scala.math._
    val Point(oLat, oLon)   = origin.toRadians
    val Point(dLat, dLon)   = destination.toRadians
    val deltaLat            = dLat - oLat
    val deltaLon            = dLon - oLon
    val hav                 = pow(sin(deltaLat / 2), 2) + cos(oLat) * cos(dLat) * pow(sin(deltaLon / 2), 2)
    val greatCircleDistance = 2 * atan2(sqrt(hav), sqrt(1 - hav))
    val earthRadiusMiles    = 3958.761
    val earthRadiusMeters   = earthRadiusMiles / 0.00062137
    val distanceInMeters    = earthRadiusMeters * greatCircleDistance
    distanceInMeters / 1000
  }

}

object GetCarriersByCategory {
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
