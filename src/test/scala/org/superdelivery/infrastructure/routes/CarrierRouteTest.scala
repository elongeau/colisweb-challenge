package org.superdelivery.infrastructure.routes

import cask.model.Status.{NotFound, OK}
import io.undertow.Undertow
import munit.FunSuite
import org.superdelivery.Data
import org.superdelivery.domain.model.{CarrierId, Compatibilities, Packet, Point, Timeslot}
import org.superdelivery.domain.usecases.{CreateACarrier, GetBestCarrierForADelivery, GetCarriersForACategory}
import org.superdelivery.infrastructure.repositories.InMemoryCarrierRepository
import upickle.default._

import java.time.LocalTime

class CarrierRouteTest extends FunSuite with JsonRW {
  private val serverFixture = FunFixture.apply[Undertow](
    setup = _ => {
      val server = Undertow.builder
        .addHttpListener(8081, "localhost")
        .setHandler(new CarrierRoute(new InMemoryCarrierRepository).defaultHandler)
        .build
      server.start()
      server
    },
    teardown = server => server.stop()
  )
  private val carrierUrl = "http://localhost:8081/api/carriers"

  serverFixture.test("return Created status with ID when carrier is created") { _ =>
    val response = requests.post(
      s"$carrierUrl",
      data = upickle.default.write(RequestCommand(Data.command))
    )
    assertEquals(response.statusCode, 201)
    assertEquals(upickle.default.read[CarrierId](response.text()), CarrierId("john-express"))
  }

  serverFixture.test("return Conflict status and reason when carrier already exists") { _ =>
    val json = upickle.default.write(RequestCommand(Data.command))
    requests.post(carrierUrl, data = json, check = false)

    val response =
      requests.post(s"$carrierUrl", data = json, check = false)

    assertEquals(response.statusCode, 409)
    assertEquals(response.text(), """"A carrier with same ID already exists"""")
  }

  serverFixture.test("return carriers with their compatibility against a delivery category") { _ =>
    requests.post(
      url = carrierUrl,
      data = upickle.default.write(RequestCommand(Data.command)),
      check = false
    )

    val response = requests.get(
      url = s"$carrierUrl/categories",
      params = Map(
        "start"           -> "09:00",
        "end"             -> "18:00",
        "latitude"        -> "43.2969901",
        "longitude"       -> "5.3789783",
        "radius"          -> "10",
        "maxWeight"       -> "20",
        "maxPacketWeight" -> "30",
        "maxVolume"       -> "40"
      )
    )

    assertEquals(response.statusCode, OK.code)
    assertEquals(
      upickle.default.read[List[GetCarriersForACategory.Result]](response.text()),
      List(
        GetCarriersForACategory.Result(CarrierId("john-express"), Compatibilities.PARTIAL)
      )
    )
  }

  serverFixture.test("get best carrier for a delivery") { _ =>
    requests.post(
      url = carrierUrl,
      data = upickle.default.write(RequestCommand(Data.command)),
      check = false
    )

    val response = requests.post(
      url = s"$carrierUrl/deliveries",
      data = upickle.default.write(
        RequestQuery(
          GetBestCarrierForADelivery.Query(
            pickupPoint = Point(43.2969901, 5.3789783),
            shippingPoint = Point(43.2969901, 5.3789783),
            timeslot = Timeslot(
              LocalTime.parse("09:00"),
              LocalTime.parse("18:00")
            ),
            packets = List(
              Packet(10, 2),
              Packet(15, 4)
            )
          )
        )
      )
    )

    assertEquals(response.statusCode, OK.code)
    assertEquals(
      upickle.default.read[CarrierId](response.text()),
      CarrierId("john-express")
    )
  }

  serverFixture.test("get no carrier for a delivery") { _ =>
    val response = requests.post(
      url = s"$carrierUrl/deliveries",
      check = false,
      data = upickle.default.write(
        RequestQuery(
          GetBestCarrierForADelivery.Query(
            pickupPoint = Point(43.2969901, 5.3789783),
            shippingPoint = Point(43.2969901, 5.3789783),
            timeslot = Timeslot(
              LocalTime.parse("09:00"),
              LocalTime.parse("18:00")
            ),
            packets = List(
              Packet(10, 2),
              Packet(15, 4)
            )
          )
        )
      )
    )

    assertEquals(response.statusCode, NotFound.code)
  }

  case class RequestCommand(requestCarrier: CreateACarrier.Command)
  object RequestCommand {
    implicit val rw: ReadWriter[RequestCommand] = macroRW
  }

  case class RequestQuery(query: GetBestCarrierForADelivery.Query)
  object RequestQuery {
    implicit val rw: ReadWriter[RequestQuery] = macroRW
  }
}
