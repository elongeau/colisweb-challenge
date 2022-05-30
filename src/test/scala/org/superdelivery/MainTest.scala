package org.superdelivery

import io.undertow.Undertow
import munit.FunSuite
import org.superdelivery.model.CarrierId

class MainTest extends FunSuite {
  private[this] val hostFixture: FunFixture[String] = FunFixture[String](
    setup = _ => {
      val server = Undertow.builder
        .addHttpListener(8081, "localhost")
        .setHandler(Main.defaultHandler)
        .build
      server.start()
      "http://localhost:8081"
    },
    teardown = _ => ()
  )

  hostFixture.test("poc") { host =>
    val response = requests.post(
      s"$host/api/carriers",
      data = ujson.Obj(
        "requestCarrier" -> ujson.Obj(
          "name" -> "John express",
          "workingRange" -> ujson.Obj(
            "start" -> "09:00",
            "end" -> "18:00"
          ),
          "workingArea" -> ujson.Obj(
            "point" -> ujson.Obj(
              "latitude" -> 43.2969901,
              "longitude" -> 5.3789783
            ),
            "radius" -> 42
          ),
          "maxWeight" -> 50,
          "maxVolume" -> 40,
          "maxPacketWeight" -> 30,
          "speed" -> 20,
          "cost" -> 10
        )
      )
    )
    assertEquals(response.statusCode, 201)
    assertEquals(
      response.text(),
      upickle.default.write(CarrierId("john-express"))
    )
  }
}
