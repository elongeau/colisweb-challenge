# ColisWeb Challenge

# How to start

```shell
sbt ~reStart
```

# Add carriers

```shell
curl --request POST \
  --url http://localhost:8080/api/carriers \
  --header 'Content-Type: application/json' \
  --cookie mongo-express=s%253AJvIfVTwd7lmxnGV7Ab0OumUEBDu1g66_.54kXuIWBFiviPP3kmIKwtSb062tveeu0vgAGroXfdPQ \
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

# Check carrier compatibility against a category

```shell
curl --request GET \
  --url 'http://localhost:8080/api/carriers/categories?start=10%3A00&end=11%3A00&latitude=48.891305&longitude=2.3529867&radius=1&maxWeight=130&maxPacketWeight=15&maxVolume=1'
```

# Find best carrier for a delivery

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
