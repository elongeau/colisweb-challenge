package org.superdelivery.infrastructure.routes

import cask._
import cask.endpoints.JsonData
import cask.model.Status.{Conflict, Created, NotFound}
import org.superdelivery.domain.model.{
  Area,
  DistanceInKm,
  Latitude,
  Longitude,
  Point,
  Timeslot,
  VolumeInCubeMeter,
  WeightInKg
}
import org.superdelivery.domain.repositories.CarrierRepository
import org.superdelivery.domain.usecases.GetCarriersForACategory.Query
import org.superdelivery.domain.usecases.{CreateACarrier, GetBestCarrierForADelivery, GetCarriersForACategory}

import java.time.LocalTime

class CarrierRoute(carrierRepository: CarrierRepository) extends MainRoutes with JsonReadWriters {
  private[this] val createACarrier             = new CreateACarrier(carrierRepository)
  private[this] val getCarriersForACategory    = new GetCarriersForACategory(carrierRepository)
  private[this] val getBestCarrierForADelivery = new GetBestCarrierForADelivery(carrierRepository)

  /**
   * Create a carrier
   * @param requestCarrier
   *   the data about the carrier
   * @return
   *   the carrier ID else a Conflict code if it already exists
   */
  @postJson("/api/carriers")
  def createCarrier(requestCarrier: CreateACarrier.Command): Response[JsonData] = {
    val result = createACarrier.handle(requestCarrier)
    result match {
      case Right(carrier) => Response(carrier.carrierId, Created.code)
      case Left(error)    => Response(error, Conflict.code)
    }
  }

  /**
   * Get carriers with their
   * [[org.superdelivery.domain.model.Compatibilities.Compatibility]] against a
   * category
   * @param start
   *   delivery timeslot start
   * @param end
   *   delivery timeslot end
   * @param latitude
   *   delivery area point latitude
   * @param longitude
   *   delivery area point longitude
   * @param radius
   *   around the delivery area point
   * @param maxWeight
   *   max weight of the category
   * @param maxPacketWeight
   *   max packet weight of the category
   * @param maxVolume
   *   max volume of the category
   * @return
   *   a list of [[Carrier]]s with their
   *   [[org.superdelivery.domain.model.Compatibilities.Compatibility]]
   */
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

  /**
   * Find best carrier, if any, that match a delivery
   * @param query
   *   the delivery
   * @return
   *   the best carrier or NotFound
   */
  @postJson("/api/carriers/deliveries")
  def findBestForADelivery(query: GetBestCarrierForADelivery.Query): Response[JsonData] =
    getBestCarrierForADelivery.handle(query) match {
      case Some(carrierId) => Response(carrierId)
      case None            => Abort(NotFound.code)
    }

  initialize()
}
