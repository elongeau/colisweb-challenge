package org.superdelivery

import upickle.default._

object Commands {
  object CreateCarrierCommand {
    implicit val rw: ReadWriter[CreateCarrierCommand] = macroRW
  }
}
