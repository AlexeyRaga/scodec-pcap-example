import java.io.{ObjectOutputStream, ByteArrayOutputStream, ObjectInputStream, ByteArrayInputStream}

trait Serialisation[S] {
  def serialise(sketch: S): Array[Byte]
  def deserialise(bytes: Array[Byte]): S
}

trait JavaSerialisation[S] extends Serialisation[S] {
  def deserialise(bytes: Array[Byte]) = {
    val bis = new ByteArrayInputStream(bytes)
    val ois = new ObjectInputStream(bis)
    ois.readObject().asInstanceOf[S]
  }

  def serialise(s: S) = {
    val bos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(bos)
    oos.writeObject(s)
    bos.toByteArray
  }
}

object LameSerializer extends JavaSerialisation[Vector[PcapCodec.PcapRecord]]