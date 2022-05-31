package org.superdelivery.repositories

import org.superdelivery.model.{Carrier, CarrierId}

class InMemoryCarrierRepository extends InMemoryRepository[CarrierId, Carrier](_.carrierId)
