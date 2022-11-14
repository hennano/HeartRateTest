package net.hennabatch.heartrate.endpoint

import com.illposed.osc.OSCMessage
import com.illposed.osc.transport.OSCPortOut
import jakarta.websocket.*
import jakarta.websocket.server.ServerEndpoint
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArraySet

@ServerEndpoint("/")
class ServerEndPoint {

    private val sendPort = 9000
    private val ip = InetAddress.getLoopbackAddress()
    private val sender = OSCPortOut(InetSocketAddress(ip, sendPort))

    private val address = "/avatar/parameters/HeartRateFloat01"

    private val openResponse = "{\"d\":{\"obsWebSocketVersion\":\"5.0.1\",\"rpcVersion\":1},\"op\":0}"
    private val handShakeRequest = "{\"d\":{\"eventSubscriptions\":0,\"rpcVersion\":1},\"op\":1}"
    private val handShakeResponse = "{\"d\":{\"negotiatedRpcVersion\":1},\"op\":2}"

    companion object {
        private val sessions = CopyOnWriteArraySet<Session>()
    }

    @OnOpen
    fun onOpen(session: Session){
        session.basicRemote.sendText(openResponse)
        sessions.add(session)
    }

    @OnMessage
    fun onMessage(message: String, session: Session){
        if(message == handShakeRequest){
            session.basicRemote.sendText(handShakeResponse)
            return
        }

        val heartRate = extractHeatRate(message) ?: return
        println("心拍数: $heartRate")
        val oscMessage = OSCMessage(address, mutableListOf((heartRate / 255.0f).coerceAtMost(1.0f)))
        sender.send(oscMessage)
    }


    @OnClose
    fun onClose(session: Session) {
        sessions.remove(session)
    }

    @OnError
    fun onError(session: Session, t: Throwable) {
    }

    private fun extractHeatRate(message: String): Int? {
        val regex = Regex("\"text\":\"[0-9]{1,3}\"")
        val param = regex.find(message)
        val data = param?.value?.replace("\"", "")?.split(":") ?: return null
        if (data.size != 2) return null
        return data[1].toInt()
    }
}