package helix.util

object Hex {
  def encode(in: Array[Byte]): String = {
    val sb = new StringBuilder
    val len = in.length
    def addDigit(in: Array[Byte], pos: Int, len: Int, sb: StringBuilder) {
      if (pos < len) {
        val b: Int = in(pos)
        val msb = (b & 0xf0) >> 4
        val lsb = (b & 0x0f)
        sb.append((if (msb < 10) ('0' + msb).asInstanceOf[Char] else ('a' + (msb - 10)).asInstanceOf[Char]))
        sb.append((if (lsb < 10) ('0' + lsb).asInstanceOf[Char] else ('a' + (lsb - 10)).asInstanceOf[Char]))

        addDigit(in, pos + 1, len, sb)
      }
    }
    addDigit(in, 0, len, sb)
    sb.toString
  }
  
  def decode(str: String): Array[Byte] = {
    val max = str.length / 2
    val ret = new Array[Byte](max)
    var pos = 0
    
    def byteOf(in: Char): Int = in match {
      case '0' => 0
      case '1' => 1
      case '2' => 2
      case '3' => 3
      case '4' => 4
      case '5' => 5
      case '6' => 6
      case '7' => 7
      case '8' => 8
      case '9' => 9
      case 'a' | 'A' => 10
      case 'b' | 'B' => 11
      case 'c' | 'C' => 12
      case 'd' | 'D' => 13
      case 'e' | 'E' => 14
      case 'f' | 'F' => 15
        case _ => 0
    }
    
    while (pos < max) {
      val two = pos * 2
      val ch: Char = str.charAt(two)
      val cl: Char = str.charAt(two + 1)
      ret(pos) = (byteOf(ch) * 16 + byteOf(cl)).toByte
      pos += 1
    }
    
    ret
  }
}