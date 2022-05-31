package org.superdelivery.usecases

import munit.FunSuite
import org.superdelivery.Data
import org.superdelivery.model.Compatibilities.{FULL, PARTIAL}
import org.superdelivery.model.{Area, Point}
import org.superdelivery.repositories.InMemoryCarrierRepository

class GetCarriersByCategoryTest extends FunSuite {
  private val repository = new InMemoryCarrierRepository
  private val sut        = new GetCarriersByCategory(repository)
  /*
   * Pour un transporteur, on peut vérifier s’il est compatible avec une catégorie de livraison.
Une catégorie de livraison est composée de :
Une plage de livraison possible (horaire de début et fin)
Une zone de livraison, sous forme d’une cercle défini par :
des coordonnées GPS du centre
une distance autour du centre
Un poids maximal total de livraison
Un poids maximal d’un colis
Un volume maximal total

Un transporteur peut avoir plusieurs niveaux de compatibilité avec une catégorie de livraison :
Il est totalement compatible si
Sa zone de travail recouvre complètement la zone de livraison
Sa plage de travail recouvre complètement la plage de livraison
Il peut prendre en charge les poids et volumes maximum de la catégorie de livraison
Il est partiellement compatible si
Au moins un des 3 critères ci-dessus est vérifié, mais pas les 3
Il n’est pas compatible si
Les 3 critères ne sont pas vérifiés

   * */
  test("should return carrier with FULL compatibility when all criteria matches") {
    repository.save(Data.defaultCarrier)

    val value = sut.handle(
      GetCarriersByCategory.Query(
        deliveryRange = Data.defaultCarrier.workingRange,
        deliveryArea = Data.defaultCarrier.workingArea,
        maxWeight = Data.defaultCarrier.maxWeight,
        maxPacketWeight = Data.defaultCarrier.maxPacketWeight,
        maxVolume = Data.defaultCarrier.maxVolume
      )
    )

    assertEquals(
      value,
      List(
        GetCarriersByCategory.Result(Data.defaultCarrier.name, FULL)
      )
    )
  }

  test("should return carrier with PARTIAL compatibility when working range do not matches") {
    repository.save(Data.defaultCarrier)

    val value = sut.handle(
      GetCarriersByCategory.Query(
        deliveryRange = Data.defaultCarrier.workingRange,
        deliveryArea = Area(Point(48.891305, 2.3529867), 1),
        maxWeight = Data.defaultCarrier.maxWeight,
        maxPacketWeight = Data.defaultCarrier.maxPacketWeight,
        maxVolume = Data.defaultCarrier.maxVolume
      )
    )

    assertEquals(
      value,
      List(
        GetCarriersByCategory.Result(Data.defaultCarrier.name, PARTIAL)
      )
    )
  }

}
