import scala.collection.immutable.IndexedSeq
import scala.concurrent.duration._
import scodec.bits._
import scodec._
import shapeless.Iso

object PcapCodec {
  import scodec.codecs._

  sealed trait ByteOrdering
  case object BigEndian extends ByteOrdering
  case object LittleEndian extends ByteOrdering

  private val magicNumber = 0x000000a1b2c3d4L
  val byteOrdering = "magic_number" | Codec[ByteOrdering](
    (bo: ByteOrdering) => if (bo == BigEndian) uint32.encode(magicNumber) else uint32L.encode(magicNumber),
    (buf: BitVector) => uint32.decode(buf).map { case (rest, mn) =>
      (rest, if (mn == magicNumber) BigEndian else LittleEndian)
    }
  )

  def gint16(implicit ordering: ByteOrdering): Codec[Int] = if (ordering == BigEndian) int16 else int16L
  def guint16(implicit ordering: ByteOrdering): Codec[Int] = if (ordering == BigEndian) uint16 else uint16L
  def gint32(implicit ordering: ByteOrdering): Codec[Int] = if (ordering == BigEndian) int32 else int32L
  def guint32(implicit ordering: ByteOrdering): Codec[Long] = if (ordering == BigEndian) uint32 else uint32L

  case class PcapHeader(ordering: ByteOrdering, versionMajor: Int, versionMinor: Int, thiszone: Int, sigfigs: Long, snaplen: Long, network: Long)
  implicit val pcapHeaderIso = Iso.hlist(PcapHeader.apply _, PcapHeader.unapply _)

  implicit val pcapHeader: Codec[PcapHeader] = {
    ("magic_number"     | byteOrdering             ) >>:~ { implicit ordering =>
      ("version_major"    | guint16                  ) ::
        ("version_minor"    | guint16                  ) ::
        ("thiszone"         | gint32                   ) ::
        ("sigfigs"          | guint32                  ) ::
        ("snaplen"          | guint32                  ) ::
        ("network"          | guint32                  )
    }}.as[PcapHeader]


  case class PcapRecordHeader(timestampSeconds: Long, timestampMicros: Long, includedLength: Long, originalLength: Long) {
    def timestamp: Double = timestampSeconds + (timestampMicros / (1.second.toMicros.toDouble))
    def toBitVector: BitVector = BitVector(timestampSeconds, timestampMicros, includedLength, originalLength)
  }
  implicit val pcapRecordHeaderIso = Iso.hlist(PcapRecordHeader.apply _, PcapRecordHeader.unapply _)

  implicit def pcapRecordHeader(implicit ordering: ByteOrdering) = {
    ("ts_sec"           | guint32                  ) ::
      ("ts_usec"          | guint32                  ) ::
      ("incl_len"         | guint32                  ) ::
      ("orig_len"         | guint32                  )
  }.as[PcapRecordHeader]

  case class PcapRecord(header: PcapRecordHeader, data: BitVector) {
    def toBitVector: BitVector = header.toBitVector ++ data
  }
  implicit val pcapRecordIso = Iso.hlist(PcapRecord.apply _, PcapRecord.unapply _)

  implicit def pcapRecord(implicit ordering: ByteOrdering) = {
    ("record_header"    | pcapRecordHeader                   ) >>:~ { hdr =>
      ("record_data"      | bits(hdr.includedLength.toInt * 8) ).hlist
    }}.as[PcapRecord]

  case class PcapFile(header: PcapHeader, records: IndexedSeq[PcapRecord])
  implicit val pcapFileIso = Iso.hlist(PcapFile.apply _, PcapFile.unapply _)

  implicit val pcapFile = {
    pcapHeader >>:~ { hdr => repeated(pcapRecord(hdr.ordering)).hlist
    }}.as[PcapFile]
}