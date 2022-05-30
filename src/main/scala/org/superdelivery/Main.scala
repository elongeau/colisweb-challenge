package org.superdelivery

import cask._
import cask.model.Status.Created
import org.superdelivery.Commands.CreateCarrierCommand

object Main extends MainRoutes {
  @postJson("/api/carriers")
  def createCarrier(
      requestCarrier: CreateCarrierCommand
  ): Response[CreateCarrierCommand] = {
    println(requestCarrier)
    Response(requestCarrier, Created.code)
  }

  initialize()
}
