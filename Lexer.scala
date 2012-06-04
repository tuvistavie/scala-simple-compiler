object Lexer {
  abstract class Token
  case class IntTok(v: Int) extends Token
  case class DoubleTok(v: Double) extends Token
  case class Id(s: String) extends Token
  case class Op(s: String) extends Token
  case class One(c: Char) extends Token
  case class KeyWord(s: String) extends Token
  case object Eof extends Token

  val KeyWords = List("Fun")

  class BadTokenException(s: String) extends Exception {
    def printError = println(s)
  }

  class Lex(s: String) {
    var line = s

    def advance = if (!line.isEmpty) line = line.substring(1)
    def lookAhead: Option[Char] = line.headOption

    def parseNumber = {
      def getIntString(s: String, decPart: Boolean): String = lookAhead match {
        case None => s
        case Some(c) => c match {
          case c if c.isDigit => advance; getIntString(s + c, decPart)
          case '.' if decPart => throw new BadTokenException("multiple points in number")
          case _ => s
        }
      }
      val intPart = getIntString("", false)
      lookAhead match {
        case None => IntTok(intPart.toInt)
        case Some(c) => c match {
          case '.' => advance; DoubleTok((intPart + c + getIntString("", true)).toDouble)
          case _ => IntTok(intPart.toInt)
        }
      }
    }

    def getToken: Token = nativeToken match {
      case One(' ') | One('\t') => getToken
      case One('\n') => Eof
      case token => token
    }

    def getId(s: String): Token = lookAhead match {
      case None => Id(s)
      case Some(c) => c match {
        case c if c.isLetter => advance; getId(s + c)
        case _ => if(KeyWords.contains(s)) KeyWord(s) else Id(s)
      }
    }

    def nativeToken: Token = lookAhead match {
      case None => Eof
      case Some(c) => c match {
        case c if c.isDigit => parseNumber
        case c if c.isLetter => getId("")
        case c if c.toString.matches("[=+/*'^-]") => advance; Op(c.toString)
        case _ => advance; One(c)
      }
    }
  }
}