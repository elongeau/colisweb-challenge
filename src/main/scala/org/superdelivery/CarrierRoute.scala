package org.superdelivery

import cask._
import cask.model.Status.{Conflict, Created}
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
import org.superdelivery.usecases.GetCarriersByCategory.Query
import org.superdelivery.usecases.{CreateACarrier, GetCarriersByCategory}

import java.time.LocalTime

class CarrierRoute(repository: Repository[CarrierId, Carrier]) extends MainRoutes with Serializer {
  private[this] val createACarrier = new CreateACarrier(repository)
  private[this] val getCarriers    = new GetCarriersByCategory(repository)

  @postJson("/api/carriers")
  def createCarrier(requestCarrier: CreateACarrier.Command): Response[String] = {
    val result = createACarrier.handle(requestCarrier)
    result match {
      case Right(carrier) => Response(carrier.carrierId.id, Created.code)
      case Left(error)    => Response(error, Conflict.code)
    }
  }

  @getJson("/api/carriers")
  def getByCategory(
    start: String,
    end: String,
    latitude: Latitude,
    longitude: Longitude,
    radius: DistanceInKm,
    maxWeight: WeightInKg,
    maxPacketWeight: WeightInKg,
    maxVolume: VolumeInCubeMeter
  ): List[GetCarriersByCategory.Result] = getCarriers.handle(
    Query(
      deliveryRange = Timeslot(LocalTime.parse(start), LocalTime.parse(end)),
      deliveryArea = Area(Point(latitude, longitude), radius),
      maxWeight = maxWeight,
      maxPacketWeight = maxPacketWeight,
      maxVolume = maxVolume
    )
  )

  initialize()
}
