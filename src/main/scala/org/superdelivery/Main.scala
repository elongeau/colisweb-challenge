package org.superdelivery

import cask._
import cask.model.Status.Created
import org.superdelivery.Commands.CreateCarrierCommand
import org.superdelivery.model.{Carrier, CarrierId}
import org.superdelivery.usecases.{CreateACarrier, InMemoryDB}

object Main extends MainRoutes {
  val carrierRepository = new InMemoryDB[CarrierId, Carrier](_.carrierId)
  @postJson("/api/carriers")
  def createCarrier(
      requestCarrier: CreateCarrierCommand
  ): Response[CarrierId] = {
    val result = new CreateACarrier(carrierRepository).handle(requestCarrier)
    Response(result.carrierId, Created.code)
  }

  initialize()
}
