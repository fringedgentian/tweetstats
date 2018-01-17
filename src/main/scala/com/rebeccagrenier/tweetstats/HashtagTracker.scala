package com.rebeccagrenier.tweetstats

import akka.actor.{ Actor, Props }
import com.danielasfregola.twitter4s.entities.HashTag

object HashtagTracker {
  def props(): Props = Props(new HashtagTracker())

  final case class ProcessTweet(hashtags: Seq[HashTag])
}

class HashtagTracker() extends Actor {
  import HashtagTracker._
  import Query._

  var topHashtags: Map[String, Int] = Map.empty

  override def receive: Receive = {

    case ProcessTweet(hashtags) => {
      hashtags.map(hashtag => {
        topHashtags.get(hashtag.text) match {
          case Some(count) => topHashtags = topHashtags + (hashtag.text -> (count + 1))
          case None => topHashtags = topHashtags + (hashtag.text -> 1)
        }
      })
    }

    case RequestStats() => {
      val queryActor = sender()
      queryActor ! HashtagStats(topHashtags)
    }
  }
}