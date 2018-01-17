
package com.rebeccagrenier.tweetstats

import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.enums.Language
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import scala.io.StdIn
import StatsSupervisor._
import Query._

object TweetStream extends App {

  val streamingClient = TwitterStreamingClient()

  val showLanguages = Seq(Language.English)

  val system = ActorSystem("stat-system")

  try {
    // Create top level supervisor
    val supervisor = system.actorOf(StatsSupervisor.props(), "stat-supervisor")

    streamingClient.sampleStatuses(languages = showLanguages) {
      case tweet: Tweet => supervisor ! ProcessTweet(tweet)
    }

    val queryActor = system.actorOf(Query.props(), "query")

    //show query printout every 5 seconds
    system.scheduler.schedule(5 seconds, 5 seconds) {
      queryActor ! RequestAllStats()
    }

    // Exit the system after Any Key is pressed
    StdIn.readLine()
  } finally {

    system.terminate()
  }

}