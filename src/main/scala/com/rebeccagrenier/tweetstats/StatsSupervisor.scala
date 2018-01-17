package com.rebeccagrenier.tweetstats

import akka.actor.{ Actor, Props }
import com.danielasfregola.twitter4s.entities.Tweet

object StatsSupervisor {
  def props(): Props = Props(new StatsSupervisor())

  final case class ProcessTweet(tweet: Tweet)

  // Convert string to UTF32 encodings
  def UTF32point(s: String, idx: Int = 0, found: List[Int] = Nil): List[Int] = {
    if (idx >= s.length) found.reverse
    else {
      val point = s.codePointAt(idx)
      UTF32point(s, idx + java.lang.Character.charCount(point), point :: found)
    }
  }
}

class StatsSupervisor() extends Actor {
  import StatsSupervisor._
  import Query._

  var count: Int = 0
  var picsCount: Int = 0
  val startTime = System.currentTimeMillis

  val emojiTracker = context.actorOf(EmojiTracker.props(), name = "emojiTracker")
  val urlTracker = context.actorOf(URLTracker.props(), name = "urlTracker")
  val hashtagTracker = context.actorOf(HashtagTracker.props(), name = "hashtagTracker")

  override def receive: Receive = {
    //Route Tweet to appropriate stat detail tracker
    case ProcessTweet(tweet) => {
      count = count + 1

      val codePoints = UTF32point(tweet.text)
      //Check for character in the emoji range
      if (codePoints.exists(_ > 65533)) {
        emojiTracker ! EmojiTracker.ProcessTweet(codePoints)
      }

      if (tweet.text.contains("://")) {
        urlTracker ! URLTracker.ProcessTweet(tweet.entities)
      }

      tweet.entities.map(entity => {
        entity.media.map(media => {
          if (media.`type` == "photo") {
            picsCount = picsCount + 1
          }
        })
      })

      if (tweet.text.contains("#")) {
        tweet.entities.map(entity => {
          hashtagTracker ! HashtagTracker.ProcessTweet(entity.hashtags)
        })
      }
    }

    case RequestStats() => {
      val queryActor = sender()
      queryActor ! SupervisorStats(count, picsCount, startTime)
    }

  }

}