package org.superdelivery.infrastructure.routes

import org.superdelivery.domain.model.Compatibilities.Compatibility
import org.superdelivery.domain.model.{Area, Carrier, CarrierId, Compatibilities, Packet, Point, Timeslot}
import org.superdelivery.domain.usecases.{CreateACarrier, GetBestCarrierForADelivery, GetCarriersForACategory}
import upickle.default._

import java.time.LocalTime

trait JsonRW {
  implicit val carrierRW: ReadWriter[Carrier]     = macroRW
  implicit val carrierIdRW: ReadWriter[CarrierId] = macroRW
  implicit val packetRW: ReadWriter[Packet]       = macroRW
  implicit val timeslotRW: ReadWriter[Timeslot]   = macroRW
  implicit val areaRW: ReadWriter[Area]           = macroRW
  implicit val pointRW: ReadWriter[Point]         = macroRW

  implicit val createACarrierCommandRW: ReadWriter[CreateACarrier.Command]                   = macroRW
  implicit val getBestCarrierForADeliveryQuery: ReadWriter[GetBestCarrierForADelivery.Query] = macroRW
  implicit val getCarriersForACategoryResult: ReadWriter[GetCarriersForACategory.Result]     = macroRW

  implicit val compatibilityRW: ReadWriter[Compatibility] = readwriter[String].bimap(
    f = _.toString,
    g = s => Compatibilities.values.find(v => v.toString == s.toUpperCase).get
  )

  implicit val localTimeReader: ReadWriter[LocalTime] = upickle.default
    .readwriter[String]
    .bimap(
      f = _.toString,
      g = {
        case s: String => LocalTime.parse(s)
        case x         => throw new RuntimeException(s"Invalid time $x")
      }
    )

}
