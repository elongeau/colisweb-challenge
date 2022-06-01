package org.superdelivery.usecases

import org.superdelivery.model.{Carrier, CarrierId, DurationInSeconds, Packets, Point, Timeslot}
import org.superdelivery.repositories.Repository
import org.superdelivery.usecases.GetBestCarrierForADelivery.{Query, Result}
import org.superdelivery.usecases.Internals._
import upickle.default._

import java.time.Duration

class GetBestCarrierForADelivery(repository: Repository[CarrierId, Carrier]) {
  def handle(query: Query): Result = {
    val matcheds = repository.getAll.map { carrier =>
      val inWorkingArea = Option.when {
        val distanceToPickup   = Haversine.distanceInKm(carrier.workingArea.point, query.pickupPoint)
        val distanceToShipping = Haversine.distanceInKm(carrier.workingArea.point, query.shippingPoint)
        carrier.workingArea.radius >= distanceToPickup && carrier.workingArea.radius >= distanceToShipping
      }(InWorkingArea)

      val overlapTimeslot = Option.when(carrier.workingTimeslot.overlap(query.timeslot))(OverlapTimeslot)

      val packageFit = Option.when {
        val weights = query.packets.packets.map(_.weight)
        weights.sum <= carrier.maxWeight &&
        weights.max <= carrier.maxPacketWeight &&
        query.packets.packets.map(_.volume).max <= carrier.maxVolume
      }(FitPackaging)

      val bonus = Option.when {
        val distance                       = Haversine.distanceInKm(query.pickupPoint, query.shippingPoint)
        val timeItTakes: DurationInSeconds = (distance / carrier.speed * 3600).toLong
        query.timeslot.duration.compareTo(Duration.ofSeconds(timeItTakes)) > 0
      }(InWorkingTimeslotBonus)

      MatchingCarrier(carrier, List(inWorkingArea, overlapTimeslot, packageFit, bonus).flatten)
    }
      .filter(_.matchAll)
      .sortBy(_.carrier.cost)

    Result {
      matcheds
        .find(_.criterias.contains(InWorkingTimeslotBonus))
        .orElse(matcheds.headOption)
        .map(_.carrier.carrierId)
    }

  }
}

private object Internals {
  sealed trait Criteria
  case object InWorkingArea          extends Criteria
  case object OverlapTimeslot        extends Criteria
  case object FitPackaging           extends Criteria
  case object InWorkingTimeslotBonus extends Criteria

  case class MatchingCarrier(carrier: Carrier, criterias: List[Criteria]) {
    def matchAll: Boolean = criterias.filterNot(_ == InWorkingTimeslotBonus).size == 3
  }
}

object GetBestCarrierForADelivery {
  case class Query(pickupPoint: Point, shippingPoint: Point, timeslot: Timeslot, packets: Packets)
  object Query {
    implicit val rw: ReadWriter[Query] = macroRW
  }

  case class Result(carrierId: Option[CarrierId])
  object Result {
    implicit val rw: ReadWriter[Result] = macroRW
  }

}
