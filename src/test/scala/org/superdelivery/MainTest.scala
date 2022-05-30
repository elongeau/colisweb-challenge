package org.superdelivery

import io.undertow.Undertow
import munit.FunSuite

class MainTest extends FunSuite {
  override def beforeAll(): Unit = {
   val server = Undertow.builder
        .addHttpListener(8081, "localhost")
        .setHandler(Main.defaultHandler)
        .build
      server.start()
  }

  test("return Created status with ID when carrier is created") {
      val response = requests.post(
        s"http://localhost:8081/api/carriers",
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
      assertEquals(response.text(), """"john-express"""")
  }

  test(
    "return Conflict status and reason when carrier already exists"
  ) {
    val json = ujson.Obj(
      "requestCarrier" -> ujson.Obj(
        "name" -> "Super Delivery",
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
    requests.post(s"http://localhost:8081/api/carriers", data = json, check = false)

    val response = requests.post(s"http://localhost:8081/api/carriers", data = json, check = false)

    assertEquals(response.statusCode, 409)
    assertEquals(response.text(), """"A carrier with same ID already exists"""")
  }

}
