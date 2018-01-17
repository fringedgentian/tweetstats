package com.rebeccagrenier.tweetstats

import akka.actor.{ Actor, Props }
import com.danielasfregola.twitter4s.entities.Entities
import java.net.URL
import util.Try

object URLTracker {
  def props(): Props = Props(new URLTracker())

  final case class ProcessTweet(entityDetails: Option[Entities])
}

class URLTracker() extends Actor {
  import URLTracker._
  import Query._

  var topDomains: Map[String, Int] = Map.empty
  var count = 0

  def recordDomain(domain: String) = topDomains.get(domain) match {
    case Some(count) => topDomains = topDomains + (domain -> (count + 1))
    case None => topDomains = topDomains + (domain -> 1)
  }

  override def receive: Receive = {

    case ProcessTweet(entityDetails) => {
      count = count + 1
      entityDetails match {
        case Some(entities) => {
          entities.urls.map(entity => {
            val url = new URL(entity.expanded_url)
            recordDomain(url.getHost())
          })
        }
        case None => {
          recordDomain("twitter.com")
        }
      }
    }

    case RequestStats() => {
      val queryActor = sender()
      queryActor ! URLStats(count, topDomains)
    }
  }
}