package org.superdelivery

import org.superdelivery.infrastructure.repositories.InMemoryCarrierRepository
import org.superdelivery.infrastructure.routes.CarrierRoute

object Main extends CarrierRoute(new InMemoryCarrierRepository) {}
