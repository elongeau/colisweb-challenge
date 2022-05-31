package org.superdelivery

import cask.MainRoutes
import org.superdelivery.model.{Carrier, CarrierId}
import org.superdelivery.usecases.InMemoryDB

object Main extends CarrierRoute(new InMemoryDB[CarrierId, Carrier](_.carrierId)) {}
