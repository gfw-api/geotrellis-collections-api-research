import scala.concurrent._
import scala.concurrent.Future
import ExecutionContext.Implicits.global

import akka.http.scaladsl.unmarshalling.Unmarshaller._
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
import com.typesafe.config.ConfigFactory
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import org.apache.log4j.Logger
import org.apache.spark._

import geotrellis.spark.io.hadoop._

case class GeoJsonData(geometry: String)
case class ResponseData(response: Map[String, Int])

object RequestResponseProtocol extends DefaultJsonProtocol {
  implicit val requestFormat = jsonFormat1(GeoJsonData)
  implicit val responseFormat = jsonFormat1(ResponseData)
}

object Server extends HttpApp with App with Geoprocessing {
  import RequestResponseProtocol._
  val logger = Logger.getLogger(this.getClass.getName)

  val conf: SparkConf =
    new SparkConf()
      .setIfMissing("spark.master", "local[*]")
      .setAppName("Demo Server")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryo.registrator", "geotrellis.spark.io.kryo.KryoRegistrator")

  implicit val sc = new SparkContext(conf)

  // source: https://github.com/geotrellis/geotrellis-landsat-emr-demo/blob/master/server/src/main/scala/demo/Main.scala#L47
  val path = new org.apache.hadoop.fs.Path("/tmp/land-cover-data/catalog")
  val attributeStore = HadoopAttributeStore(path)
  val layerReader = HadoopLayerReader(attributeStore)

  def route: Route = cors() {
      get {
        path("ping") {
          entity(as[String]) { _ =>
            complete {
              Future {
                "pong"
              }
            }
          }
        }
      } ~
      post {
        path("panlcdcount") {
          entity(as[GeoJsonData]) { shape =>
            complete {
              Future {
                getPANLCDCount(shape, layerReader, sc)
              }
            }
          }
        }
      }
    }

  val config = ConfigFactory.load()
  val port = config.getInt("geoprocessing-server.port")
  val host = config.getString("geoprocessing-server.host")

  startServer(host, port)
}
