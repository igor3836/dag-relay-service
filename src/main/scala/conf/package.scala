import zio.{Has, Task, UIO, ULayer, URIO, ZIO, ZLayer}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

package object config {

  case class AppConf(api:ApiConf, storage:StorageConf)
  case class ApiConf(host:String, port:Int)
  case class StorageConf(backend:String)

  type Config = Has[Service]

  trait Service {
    def app: UIO[AppConf]
  }

  val live: ZLayer[Any, Throwable, Config] = ZLayer.fromEffect(
    for {
      conf <- ZIO.fromEither(ConfigSource.default.load[AppConf]).mapError(cfs => new Throwable(cfs.toString))
    } yield new Service { def app = ZIO.succeed(conf) }
  )

  // helpers
  def app: URIO[Config, AppConf] = ZIO.accessM[Config](_.get.app)
}