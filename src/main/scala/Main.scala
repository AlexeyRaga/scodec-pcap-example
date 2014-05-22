import java.io.{FileInputStream, File}
import java.util.Properties
import kafka.producer.{KeyedMessage, ProducerConfig, Producer}
import scalaz.concurrent.Task
import scodec.stream._
import scalaz.stream._

object Main extends App {
  import PcapCodec._
  val topicName = "exp-packets"

  type Bin = Vector[PcapCodec.PcapRecord]

  val props = new Properties()
  props.put("metadata.broker.list", "192.168.33.30:9092")

  val producer = new Producer[String, Array[Byte]](new ProducerConfig(props))

  def kafkaOut: Sink[Task, Bin] = io.channel((bin: Bin) => Task.delay {
    val payload = LameSerializer.serialise(bin)
    val message = new KeyedMessage[String, Array[Byte]](topicName, payload)
    producer.send(message)
  })

  val pcapRecords: StreamDecoder[PcapRecord] = for {
    header <- decode.once[PcapHeader]
    packet <- decode.many(pcapRecord(header.ordering))
  } yield packet

  def dataChannel = new FileInputStream(new File("/Users/alexey/Work/Captures/smh-capture.pcap")).getChannel
  val r = pcapRecords.decodeMmap(dataChannel)
    .chunkBy2((a, b) => a.header.timestamp != b.header.timestamp)
    .to(kafkaOut)
    .run.run
}
