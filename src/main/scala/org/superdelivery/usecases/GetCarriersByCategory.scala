package org.superdelivery.usecases

import org.superdelivery.model.Compatibilities.{Compatibility, NONE}
import org.superdelivery.model.{Area, Carrier, CarrierId, Timeslot, VolumeInCubeMeter, WeightInKg}
import org.superdelivery.usecases.GetCarriersByCategory.{Query, Result}
import upickle.default._

class GetCarriersByCategory(carrierRepository: Repository[CarrierId, Carrier]) {
  def handle(query: Query): List[Result] = carrierRepository.getAll.map { carrier =>
    println(carrier)
    Result(carrier.name, NONE)
  }
}

object GetCarriersByCategory {
  case class Query(
    deliveryRange: Timeslot,
    deliveryArea: Area,
    maxWeight: WeightInKg,
    maxPacketWeight: WeightInKg,
    maxVolume: VolumeInCubeMeter
  )

  case class Result(name: String, compatibility: Compatibility)
  object Result {
    implicit val rw: ReadWriter[Result] = macroRW
  }

}
