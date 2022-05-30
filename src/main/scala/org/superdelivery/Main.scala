package org.superdelivery

import cask._
import cask.model.Status.{Conflict, Created}
import org.superdelivery.Commands.CreateCarrierCommand
import org.superdelivery.model.{Carrier, CarrierId}
import org.superdelivery.usecases.{CreateACarrier, InMemoryDB}

object Main extends MainRoutes {
  val carrierRepository = new InMemoryDB[CarrierId, Carrier](_.carrierId)
  @postJson("/api/carriers")
  def createCarrier(
      requestCarrier: CreateCarrierCommand
  ): Response[String] = {
    val result = new CreateACarrier(carrierRepository).handle(requestCarrier)
    result match {
      case Right(carrier) => Response(carrier.carrierId.id, Created.code)
      case Left(error)    => Response(error, Conflict.code)
    }
  }

  initialize()
}
