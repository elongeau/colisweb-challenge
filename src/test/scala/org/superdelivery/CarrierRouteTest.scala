package org.superdelivery

import cask.model.Status.OK
import io.undertow.Undertow
import munit.FunSuite
import org.superdelivery.model.{Area, CarrierId, Compatibilities, Point, Timeslot}
import org.superdelivery.repositories.InMemoryCarrierRepository
import org.superdelivery.usecases.{CreateACarrier, GetCarriersByCategory}
import upickle.default._

import java.time.LocalTime

class CarrierRouteTest extends FunSuite {
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
      data = upickle.default.write(Request(Data.command))
    )
    assertEquals(response.statusCode, 201)
    assertEquals(response.text(), """"john-express"""")
  }

  serverFixture.test("return Conflict status and reason when carrier already exists") { _ =>
    val json = upickle.default.write(Request(Data.command))
    requests.post(carrierUrl, data = json, check = false)

    val response =
      requests.post(s"$carrierUrl", data = json, check = false)

    assertEquals(response.statusCode, 409)
    assertEquals(response.text(), """"A carrier with same ID already exists"""")
  }

  serverFixture.test("return carriers with their compatibility against a delivery category") { _ =>
    val johnExpress = upickle.default.write(
      Request(
        CreateACarrier.Command(
          name = "john express",
          workingTimeslot = Timeslot(
            LocalTime.parse("09:00"),
            LocalTime.parse("18:00")
          ),
          workingArea = Area(Point(43.2969901, 5.3789783), 10),
          maxWeight = 200,
          maxVolume = 12,
          maxPacketWeight = 20,
          speed = 50,
          cost = 15
        )
      )
    )
    val marcusChrono = upickle.default.write(
      Request(
        CreateACarrier.Command(
          name = "marcus chrono",
          workingTimeslot = Timeslot(
            LocalTime.parse("09:00"),
            LocalTime.parse("14:00")
          ),
          workingArea = Area(Point(43.2969901, 5.3789783), 10),
          maxWeight = 200,
          maxVolume = 12,
          maxPacketWeight = 20,
          speed = 50,
          cost = 13
        )
      )
    )

    val juliaTruck = upickle.default.write(
      Request(
        CreateACarrier.Command(
          name = "julia truck",
          workingTimeslot = Timeslot(
            LocalTime.parse("09:00"),
            LocalTime.parse("17:00")
          ),
          workingArea = Area(Point(43.2969901, 5.3789783), 10),
          maxWeight = 120,
          maxVolume = 12,
          maxPacketWeight = 20,
          speed = 50,
          cost = 14
        )
      )
    )

    requests.post(carrierUrl, data = johnExpress, check = false)
    requests.post(carrierUrl, data = marcusChrono, check = false)
    requests.post(carrierUrl, data = juliaTruck, check = false)

    val response = requests.get(
      s"$carrierUrl",
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
    val result = upickle.default.read[List[GetCarriersByCategory.Result]](response.text())
    assertEquals(
      result,
      List(
        GetCarriersByCategory.Result(CarrierId("john-express"), Compatibilities.FULL),
        GetCarriersByCategory.Result(CarrierId("marcus-chrono"), Compatibilities.PARTIAL),
        GetCarriersByCategory.Result(CarrierId("julia-truck"), Compatibilities.NONE)
      )
    )
  }

  case class Request(requestCarrier: CreateACarrier.Command)
  object Request {
    implicit val rw: ReadWriter[Request] = macroRW
  }
}
