package org.superdelivery.domain.utils

import org.superdelivery.domain.model.{DistanceInKm, Point}

object Haversine {

  /**
   * Compute the distance between two Geocoordinates
   * @return
   *   the distance in kilometer
   */
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
