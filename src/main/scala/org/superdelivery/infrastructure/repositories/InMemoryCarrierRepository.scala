package org.superdelivery.infrastructure.repositories

import org.superdelivery.domain.model.{Carrier, CarrierId}
import org.superdelivery.domain.repositories.CarrierRepository

class InMemoryCarrierRepository extends InMemoryRepository[CarrierId, Carrier](_.carrierId) with CarrierRepository
