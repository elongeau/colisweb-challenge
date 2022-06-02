# ColisWeb Challenge

# Technical design

This application allows to find a **carrier** that can deliver a delivery

## Code organization

I chose to structure the code with [CQRS](https://martinfowler.com/bliki/CQRS.html)
and [hexagonal architecture](https://alistair.cockburn.us/hexagonal-architecture/) in mind.

Then the code is splitted in two main package:

- `domain`: contains all the code related to the business cases, like:
    - `usecases` that represent the features
    - `model` for model of the domain
    - `repositories` to declare the interface of the persistence layer
- `infrastructure`: the *IO* parts like
    - `routes` for the routes of the application, they delegates everything to `usescases`
    - `repositories` to provide concrete implementation of the domain repositories

At the root of the application sits the `Main` that allows to start the application.

## Routes

ℹ️ All units use the same unit measure:

- weight: kilogram `kg`
- distance: kilometer `km`
- volume: cube meter `mˆ3`

The application offers 3 routes:

### create a carrier: `POST /api/carriers`

#### Request

```json
{
  "requestCarrier": {
    "name": "fastest",
    "workingTimeslot": {
      "start": "10:00",
      "end": "14:00"
    },
    "workingArea": {
      "point": {
        "latitude": 43.2969901,
        "longitude": 5.3789783
      },
      "radius": 10
    },
    "maxWeight": 50,
    "maxVolume": 40,
    "maxPacketWeight": 30,
    "speed": 25,
    "cost": 1
  }
}
```

#### Response

##### Success: the carrier ID

status code: `201 Created`

```json
{
  "id": "fastest"
}
```

##### Failure: Conflit

status code: `409 Conflict`

```
"A carrier with same ID already exists"
```

### Get carriers compatibility

#### Request `GET /api/carriers/categories`

Pass parameters as query parameters:

- `start`: `10:00`
- `end`: `11:00`
- `latitude`: `48.891305`
- `longitude`: `2.3529867`
- `radius`: `1`
- `maxWeight`: `130`
- `maxPacketWeight`: `15`
- `maxVolume`: `1`

#### Response

##### Success: carriers with compatibilities

status code: `200 OK`

```json
[
  {
    "carrier": {
      "id": "express"
    },
    "compatibility": "PARTIAL"
  },
  {
    "carrier": {
      "id": "faster"
    },
    "compatibility": "PARTIAL"
  }
]
```

### Get best carrier for a delivery: `POST /api/carriers/deliveries`

#### Request

```json
{
  "query": {
    "timeslot": {
      "start": "10:00",
      "end": "14:00"
    },
    "pickupPoint": {
      "latitude": 43.2969901,
      "longitude": 5.3789783
    },
    "shippingPoint": {
      "latitude": 43.2969901,
      "longitude": 5.3789783
    },
    "packets": [
      {
        "weight": 2,
        "volume": 2
      },
      {
        "weight": 10,
        "volume": 2
      }
    ]
  }
}
```

#### Response

##### Success: the carrier ID

status code: `200 OK`

```json
{
  "id": "fastest"
}
```

##### Failure: not found

status code: `404 not found`

# Use the application

## How to start

```shell
sbt ~reStart
```

## Add carriers

```shell
curl --request POST \
  --url http://localhost:8080/api/carriers \
  --header 'Content-Type: application/json' \
  --data '{
	"requestCarrier": {
		"name": "fastest",
		"workingTimeslot": {
			"start": "10:00",
			"end": "14:00"
		},
		"workingArea": {
			"point": {
				"latitude": 43.2969901,
				"longitude": 5.3789783
			},
			"radius": 10
		},
		"maxWeight": 50,
		"maxVolume": 40,
		"maxPacketWeight": 30,
		"speed": 25,
		"cost": 1
	}
}'
```

## Check carrier compatibility against a category

```shell
curl --request GET \
  --url 'http://localhost:8080/api/carriers/categories?start=10%3A00&end=11%3A00&latitude=48.891305&longitude=2.3529867&radius=1&maxWeight=130&maxPacketWeight=15&maxVolume=1'
```

## Find best carrier for a delivery

```shell
curl --request POST \
  --url http://localhost:8080/api/carriers/deliveries \
  --header 'Content-Type: application/json' \
  --data '{
	"query": {
		"timeslot": {
			"start": "10:00",
			"end": "14:00"
		},
		"pickupPoint": {
			"latitude": 43.2969901,
			"longitude": 5.3789783
		},
		"shippingPoint": {
			"latitude": 43.2969901,
			"longitude": 5.3789783
		},
		"packets": [
			{
				"weight": 2,
				"volume": 2
			},
			{
				"weight": 10,
				"volume": 2
			}
		]
	}
}'
```

