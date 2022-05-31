package org.superdelivery

import org.superdelivery.repositories.InMemoryCarrierRepository

object Main extends CarrierRoute(new InMemoryCarrierRepository) {}
