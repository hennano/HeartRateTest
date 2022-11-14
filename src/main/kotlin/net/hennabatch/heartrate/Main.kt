import net.hennabatch.heartrate.endpoint.ServerEndPoint
import org.glassfish.tyrus.server.Server
import java.lang.Thread.sleep

fun main(args: Array<String>) {

    val hostname = "localhost"
    val port = 4455

    val server = Server(hostname, port, "/", mapOf(), ServerEndPoint::class.java)
    server.start()
    sleep(1000000)
    server.start()
}