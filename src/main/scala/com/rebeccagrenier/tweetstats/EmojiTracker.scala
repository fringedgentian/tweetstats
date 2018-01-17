package com.rebeccagrenier.tweetstats

import akka.actor.{ Actor, Props }
import com.danielasfregola.twitter4s.entities.Tweet
import scala.io.Source
import net.liftweb.json._
import java.io.{ FileNotFoundException, IOException }

object EmojiTracker {
  def props(): Props = Props(new EmojiTracker())

  //recieves a list of utf32 encoded integers, one for each character in string
  final case class ProcessTweet(codePoints: List[Int])

  //List of emoji json from file in src/main/resources/
  val json = Source.fromResource("emoji.json").getLines().mkString
  val emojis = parse(json).children

}

class EmojiTracker() extends Actor {
  import EmojiTracker._
  import Query._

  var topEmojis: Map[String, Int] = Map.empty
  var count = 0

  //Return a list of matching emojis
  def getEmoji(unicodeHex: String): List[String] = for {
    JObject(ems) <- emojis
    JField("unified", JString(unified)) <- ems
    JField("short_name", JString(name)) <- ems
    if (unified == unicodeHex)
  } yield name

  override def receive: Receive = {

    case ProcessTweet(codePoints) => {
      count = count + 1

      codePoints.foreach(x => {

        //Character outside the 'normal' unicode range of characters
        if (x > 65533) {
          //convert integer to hexadecimal notation found in json
          val hexVal = Integer.toString(x, 16).toUpperCase()

          val emojiName: String = getEmoji(hexVal).headOption.getOrElse("undefined")

          //is the name in the Map already?
          topEmojis.get(emojiName) match {
            case Some(number: Int) => {
              topEmojis = topEmojis + (emojiName -> (number + 1))
            }
            case None => {
              topEmojis = topEmojis + (emojiName -> 1)
            }
          }
        }
      })
    }

    case RequestStats() => {
      val queryActor = sender()
      queryActor ! EmojiStats(count, topEmojis)
    }
  }
}