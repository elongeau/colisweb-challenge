package org.superdelivery.domain.repositories

import org.superdelivery.domain.model.{Carrier, CarrierId}

trait CarrierRepository extends Repository[CarrierId, Carrier]
