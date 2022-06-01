package org.superdelivery.usecases

import org.superdelivery.model.{Carrier, CarrierId, Packets, Point, Timeslot}
import org.superdelivery.repositories.Repository
import org.superdelivery.usecases.GetBestCarrierForADelivery.Query
import org.superdelivery.usecases.Internals._
import upickle.default._

import java.time.Duration
import scala.math.Ordered.orderingToOrdered

class GetBestCarrierForADelivery(repository: Repository[CarrierId, Carrier]) {
  def handle(query: Query): Option[CarrierId] = {
    val matcheds = repository.getAll.map { carrier =>
      MatchingCarrier(
        carrier = carrier,
        criterias = List(
          inWorkingArea(query, carrier),
          overlapTimeslot(query, carrier),
          packageFit(query, carrier),
          inWorkingTimeslot(query, carrier)
        ).flatten
      )
    }
      .filter(_.matchAll)
      .sortBy(_.carrier.cost)

    matcheds
      .find(_.criterias.contains(InWorkingTimeslotBonus))
      .orElse(matcheds.headOption)
      .map(_.carrier.carrierId)

  }

  private def inWorkingTimeslot(query: Query, carrier: Carrier) =
    Option.when {
      val distance             = Haversine.distanceInKm(query.pickupPoint, query.shippingPoint)
      val timeItTakesToDeliver = Duration.ofSeconds((distance / carrier.speed * 3600).toLong)
      timeItTakesToDeliver <= query.timeslot.duration
    }(InWorkingTimeslotBonus)

  private def packageFit(query: Query, carrier: Carrier) =
    Option.when {
      val weights = query.packets.packets.map(_.weight)
      weights.sum <= carrier.maxWeight &&
      weights.max <= carrier.maxPacketWeight &&
      query.packets.packets.map(_.volume).max <= carrier.maxVolume
    }(FitPackaging)

  private def overlapTimeslot(query: Query, carrier: Carrier) =
    Option.when(carrier.workingTimeslot.overlap(query.timeslot))(OverlapTimeslot)

  private def inWorkingArea(query: Query, carrier: Carrier) =
    Option.when {
      val distanceToPickup   = Haversine.distanceInKm(carrier.workingArea.point, query.pickupPoint)
      val distanceToShipping = Haversine.distanceInKm(carrier.workingArea.point, query.shippingPoint)
      carrier.workingArea.radius >= distanceToPickup && carrier.workingArea.radius >= distanceToShipping
    }(InWorkingArea)
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
}
