
import com.packetloop.realtime.util.misc.Conversions
import java.io.{FileInputStream, File}
import java.util.Properties
import kafka.producer.{KeyedMessage, ProducerConfig, Producer}
import scodec.stream._
import kafka._

import scalaz.std.AllInstances._


object Main extends App {
  import PcapCodec._
  val topicName = "exp-packets"

  val props = new Properties()
  props.put("metadata.broker.list", "192.168.33.30")

  val producer = new Producer[String, Array[Byte]](new ProducerConfig(props))
  def sendBin(bin: Vector[PcapCodec.PcapRecord]) = {
    val payload = LameSerializer.serialise(bin)
    val message = new KeyedMessage[String, Array[Byte]](topicName, payload)
    producer.send(message)
  }


  val pcapRecords: StreamDecoder[PcapRecord] = for {
    header <- decode.once[PcapHeader]
    packet <- decode.many(pcapRecord(header.ordering))
  } yield packet

  def channel = new FileInputStream(new File("/Users/alexey/Work/Captures/smh-capture.pcap")).getChannel

  val r = pcapRecords.decodeMmap(channel)
    .chunkBy2((a, b) => a.header.timestamp == b.header.timestamp)
    .runFoldMap(x => { sendBin(x); 1 })
    .run
}
