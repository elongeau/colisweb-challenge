package org.superdelivery.usecases

import org.superdelivery.Commands
import org.superdelivery.model.{Carrier, CarrierId}

class CreateACarrier(repository: Repository[CarrierId, Carrier]) {
  def handle(command: Commands.CreateCarrierCommand): Either[String,Carrier] = {
    val id = CarrierId(slugify(command.name))
    repository.get(id)
      .fold[Either[String,Carrier]](createCarrier(command, id)) {
       _ => Left("A carrier with same ID already exists")
      }
  }

  private def createCarrier(command: Commands.CreateCarrierCommand, id: CarrierId) = {
    val carrier = Carrier(
      carrierId = id,
      name = command.name,
      workingRange = command.workingRange,
      workingArea = command.workingArea,
      maxWeight = command.maxWeight,
      maxVolume = command.maxVolume,
      maxPacketWeight = command.maxPacketWeight,
      speed = command.speed,
      cost = command.cost
    )
    repository.save(carrier)
    Right(carrier)
  }

  private[this] def slugify(input: String): String = {
    import java.text.Normalizer
    Normalizer
      .normalize(input, Normalizer.Form.NFD)
      .replaceAll(
        "[^\\w\\s-]",
        ""
      ) // Remove all non-word, non-space or non-dash characters
      .replace('-', ' ') // Replace dashes with spaces
      .trim // Trim leading/trailing whitespace (including what used to be leading/trailing dashes)
      .replaceAll(
        "\\s+",
        "-"
      ) // Replace whitespace (including newlines and repetitions) with single dashes
      .toLowerCase // Lowercase the final results
  }

}
