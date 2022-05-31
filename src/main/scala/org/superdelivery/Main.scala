package org.superdelivery

import cask._
import cask.model.Status.{Conflict, Created}
import org.superdelivery.model.{Area, Carrier, CarrierId, DistanceInKm, Latitude, Longitude, Point, Timeslot, VolumeInCubeMeter, WeightInKg}
import org.superdelivery.usecases.GetCarriersByCategory.GetCarriersByCategoryQuery
import org.superdelivery.usecases.{CreateACarrier, GetCarriersByCategory, InMemoryDB}

import java.time.LocalTime

object Main extends MainRoutes with Serializer {
  private[this] val carrierRepository = new InMemoryDB[CarrierId, Carrier](_.carrierId)
  private[this] val createACarrier    = new CreateACarrier(carrierRepository)
  private[this] val getCarriers       = new GetCarriersByCategory(carrierRepository)

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
  ): List[Carrier] = getCarriers.handle(
    GetCarriersByCategoryQuery(
      deliveryRange = Timeslot(LocalTime.parse(start), LocalTime.parse(end)),
      deliveryArea = Area(Point(latitude, longitude), radius),
      maxWeight = maxWeight,
      maxPacketWeight = maxPacketWeight,
      maxVolume = maxVolume
    )
  )
  initialize()
}
