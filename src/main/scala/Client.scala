import java.net.Socket
import Utils._

object Client {
  val masterKey: Double = 15

  def main(args: Array[String]) {
    val keyServerSocket = new Socket("localhost", 3456)
    val targetServerSocket = new Socket("localhost", 7070)
    try {
      val (isK, osK) = generateStreams(keyServerSocket)

      val randomNumber = generateRandomKey
      val request: String = s"A|B|$randomNumber"
      osK.println(request)
      osK.flush()

      val kdcResponse = isK.readLine()
      val requestToB =
        decrypt(masterKey, kdcResponse)
          .replace(s"|$request", "")
          .replace(s"|$randomNumber", "")

      println("requestToB: " + requestToB)
      val sessionKey = requestToB.split("\\|").head.toDouble

      val (isS, osS) = generateStreams(targetServerSocket)
      osS.println(requestToB)
      osS.flush()

      val targetResponse = isS.readLine()
      println(decrypt(sessionKey, targetResponse))
      decrypt(sessionKey, targetResponse).split("\\|").toList match {
        case randomTargetNumber :: targetName :: Nil if targetName == "B" =>
          osS.println(encrypt(sessionKey, (randomTargetNumber.toDouble + 100).toString))
          osS.flush()
        case _ =>
          osS.println("Error! Something goes wrong!")
          osS.flush()
      }

      val targetConnectionStatus = isS.readLine()
      println(targetConnectionStatus)

    } finally {
      keyServerSocket.close()
      targetServerSocket.close()
    }
  }
}
