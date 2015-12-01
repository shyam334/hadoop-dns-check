package com.simple.util

import java.io.{BufferedReader, InputStreamReader}
import java.net.InetAddress
import javax.naming.directory.InitialDirContext

import scala.util.{Failure, Success, Try}

object checkDNS {

  def main(args: Array[String]): Unit = {
    if(args.length > 0) {
      args(0) match {
        case "--pipe"                       => readPipe
        case "--file" if (args.length > 1)  => args.toSeq.drop(1).map(readFile)
        case "--help" | "-help"             => printUsage
        case y if(!y.isEmpty)               => y.split(' ').map(checkDNS)
        case _                              => printUsage
      }
    } else checkDNS("")
  }

 lazy val printUsage = println(s""" |
                                    |DNS checker.
                                    |
                                    |Utility for investigating DNS behaviour in java.
                                    |Useful in hadoop env.
                                    |
                                    |USAGE:
                                    |File input                         :     checkDNS --file <file-1> <file-2> ...
                                    |Stdin/Usage in pipe                :     checkDNS --pipe
                                    |Command line                       :     checkDNS <host-string-1> <host-string-2> ...
                                    |Command line with no argument attempt perform a localhost dns lookup.
                                    |
                                 """.stripMargin
                          )

  def readPipe              = Try(new BufferedReader(new InputStreamReader(System.in)))
                               .foreach { x => Stream
                                                 .continually(x.readLine())
                                                 .takeWhile(_ != null)
                                                 .foreach(checkDNS)
                                               x.close()
                                }

  def readFile(file: String) = scala.io.Source.fromFile(file).getLines().foreach(checkDNS)

  def checkDNS(host: String) = {
    0 to 79 foreach(_ => print("-"))
    val inet = if(host.isEmpty)
                  timer(InetAddress.getLocalHost, "java local forward lookup").result
                else
                  timer(InetAddress.getByName(host), "java forward lookup").result

    inet match {
      case Success(x) => println(s"Host : ${x.getHostAddress}");reverseLookup(x)
      case _          => println("Host look up failed.")
    }
  }

  //From hadoop `DNS`
  def reverseLookup(inet: InetAddress, ns: String = "") = {
    val parts     = inet.getHostAddress.split("\\.")
    val reverseIp = s"${parts.reverse.mkString(".")}.in-addr.arpa"
    val ictx      = new InitialDirContext()

    timer(ictx.getAttributes(s"dns://$ns/$reverseIp", Array({"PTR"})), "JNDI reverse lookup")
      .result
      .map(_.get("PTR").get().toString)
      .map(x => if(x.charAt(x.length - 1) == '.') x.substring(0, x.length - 1) else "")
      .foreach(println)

    ictx.close()
  }

  def timer[A](c: => A, description: String = ""): Timer[A] = {
    val start = System.currentTimeMillis
    val res   = Try(c)
    val end   = System.currentTimeMillis
    Timer(end - start, res, description)
      .print
      .get
  }

  case class Timer[A](time: Long, result: Try[A], description: String) {
    override def toString = result match {
      case Success(x) =>  s"""|
                              |Operation ${description} succeeded. ${x}.
                              |Time taken for the operation : ${time} milli seconds.
                              |""".stripMargin

      case Failure(x) =>  s"""|
                              |Operation ${description} failed. ${x}.
                              |Time taken for the operation : ${time} milli seconds.
                              |""".stripMargin
    }

    def print = {
      println(this)
      this
    }

    def get = this
  }
}
