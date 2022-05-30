package org.superdelivery

import org.superdelivery.model.{Area, MoneyInCents, SpeedInKmH, Timeslot, Volume, WeightInKg}
import upickle.default._

object Commands {
    case class CreateCarrierCommand(
      name: String,
      workingRange: Timeslot,
      workingArea: Area,
      maxWeight: WeightInKg,
      maxVolume: Volume,
      maxPacketWeight: WeightInKg,
      speed: SpeedInKmH,
      cost: MoneyInCents
  )
  object CreateCarrierCommand {
    implicit val rw: ReadWriter[CreateCarrierCommand] = macroRW
  }
}
