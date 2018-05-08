# geotrellis-glad-analysis


A test to see if geotrellis can execute large user drawn AOIs against the GLAD alerts data.

### Requirements

* Make
* curl
* [java](http://openjdk.java.net/)
* [sbt](http://www.scala-sbt.org/download.html)
* [Spark 2.x](https://spark.apache.org/downloads.html)

To ingest the geotiff data used in the app, you'll also need to ensure that you've got `spark-submit` on your local path.

### Getting started

#### Setup

Clone the project and run:

```sh
make
```

This will

- compile the API
- [download a 1x1 degree geotiff of GLAD data from the western Brazil](http://s3.amazonaws.com/gfw2-data/alerts-tsv/temp/example-glad-dataset/clip.tif)
- TIF extent is [here](http://geojson.io/#data=data:application/json,%7B%22type%22%3A%22Feature%22%2C%22properties%22%3A%7B%7D%2C%22geometry%22%3A%7B%22type%22%3A%22Polygon%22%2C%22coordinates%22%3A%5B%5B%5B-60%2C-13%5D%2C%5B-59%2C-13%5D%2C%5B-59%2C-12%5D%2C%5B-60%2C-12%5D%2C%5B-60%2C-13%5D%5D%5D%7D%7D)
- ingest the geotiff as an RDD for GeoTrellis

Now run the API:
```
make server
```

Then pass a request to localhost:7000/glad-alerts with a payload in this format to test:
```
{"geometry":"{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[-60,-13],[-59,-13],[-59,-12],[-60,-12],[-60,-13]]]}}"}
```


### Make rules

| Rule | Description |
| --- | --- |
| `make compile` | Compile api for CI |
| `make api-console` | Log into API with `./sbt console` |
| `make server` | Start API service |
| `make download-tif` | Download the 1x1 degree geotiff of GLAD in Brazil
| `make ingest` | Load the GLAD geotiff into a GeoTrellis RDD |
