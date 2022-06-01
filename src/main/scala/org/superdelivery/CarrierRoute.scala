package org.superdelivery

import cask._
import cask.endpoints.JsonData
import cask.model.Status.{Conflict, Created, NotFound}
import org.superdelivery.model.{
  Area,
  Carrier,
  CarrierId,
  DistanceInKm,
  Latitude,
  Longitude,
  Point,
  Timeslot,
  VolumeInCubeMeter,
  WeightInKg
}
import org.superdelivery.repositories.Repository
import org.superdelivery.usecases.GetCarriersForACategory.Query
import org.superdelivery.usecases.{CreateACarrier, GetBestCarrierForADelivery, GetCarriersForACategory}

import java.time.LocalTime

class CarrierRoute(carrierRepository: Repository[CarrierId, Carrier]) extends MainRoutes with Serializer {
  private[this] val createACarrier             = new CreateACarrier(carrierRepository)
  private[this] val getCarriersForACategory    = new GetCarriersForACategory(carrierRepository)
  private[this] val getBestCarrierForADelivery = new GetBestCarrierForADelivery(carrierRepository)

  @postJson("/api/carriers")
  def createCarrier(requestCarrier: CreateACarrier.Command): Response[JsonData] = {
    val result = createACarrier.handle(requestCarrier)
    result match {
      case Right(carrier) => Response(carrier.carrierId, Created.code)
      case Left(error)    => Response(error, Conflict.code)
    }
  }

  @getJson("/api/carriers/categories")
  def getByCategory(
    start: String,
    end: String,
    latitude: Latitude,
    longitude: Longitude,
    radius: DistanceInKm,
    maxWeight: WeightInKg,
    maxPacketWeight: WeightInKg,
    maxVolume: VolumeInCubeMeter
  ): List[GetCarriersForACategory.Result] = getCarriersForACategory.handle(
    Query(
      deliveryTimeslot = Timeslot(LocalTime.parse(start), LocalTime.parse(end)),
      deliveryArea = Area(Point(latitude, longitude), radius),
      maxWeight = maxWeight,
      maxPacketWeight = maxPacketWeight,
      maxVolume = maxVolume
    )
  )

  @postJson("/api/carriers/deliveries")
  def findBestForADelivery(query: GetBestCarrierForADelivery.Query): Response[JsonData] =
    getBestCarrierForADelivery.handle(query) match {
      case Some(carrierId) => Response(carrierId)
      case None            => Abort(NotFound.code)
    }

  initialize()
}
