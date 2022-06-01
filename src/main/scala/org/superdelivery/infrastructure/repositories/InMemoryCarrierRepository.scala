package org.superdelivery.infrastructure.repositories

import org.superdelivery.domain.model.{Carrier, CarrierId}

class InMemoryCarrierRepository extends InMemoryRepository[CarrierId, Carrier](_.carrierId)
