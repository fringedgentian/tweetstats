package com.rebeccagrenier.tweetstats

import akka.actor.{ Actor, Props }

object Query {
  def props(): Props = Props(new Query())

  final case class RequestAllStats()
  final case class RequestStats()

  final case class SupervisorStats(count: Int, picsCount: Int, startTime: Long)
  final case class EmojiStats(count: Int, top: Map[String, Int])
  final case class HashtagStats(top: Map[String, Int])
  final case class URLStats(count: Int, top: Map[String, Int])
}

class Query() extends Actor {
  import Query._

  var gotEmojiStats = false
  var gotHashtagStats = false
  var gotSupervisorStats = false
  var gotURLStats = false

  var startTime: Long = 0
  var count = 0
  var picsCount = 0
  var emojiCount = 0
  var URLCount = 0
  var topDomains: Map[String, Int] = Map.empty
  var topEmojis: Map[String, Int] = Map.empty
  var topHashtags: Map[String, Int] = Map.empty

  def tweetsPer(miliseconds: Double): Double = {
    (count / (timeElapsed() / miliseconds))
  }

  def percentTweets(num: Int): Double = {
    (num / count.toDouble) * 100
  }

  def timeElapsed() = {
    System.currentTimeMillis - startTime
  }

  def sendReport() {
    if (gotEmojiStats && gotHashtagStats && gotSupervisorStats && gotURLStats) {
      println("**************Tweet Stream Report!*************\n" +
        f"Total Tweets Recieved: $count%.0f\n" +
        f"Average Tweets Per Second: ${tweetsPer(1000)}%2.0f, Minute: ${tweetsPer(60000)}%2.0f, Hour: ${tweetsPer(3600000)}%2.0f\n" +
        f"Percent of Tweets With Emoji: ${percentTweets(emojiCount)}%2.0f%%, URL ${percentTweets(URLCount)}%2.0f%%, Photo ${percentTweets(picsCount)}%2.0f%%\n" +
        "***Top 5 Emojis:***\n" +
        topEmojis.toSeq.sortWith(_._2 > _._2).take(5).map(x => s"${x._1}: ${x._2}\n").mkString +
        "***Top 5 Hashtags:***\n" +
        topHashtags.toSeq.sortWith(_._2 > _._2).take(5).map(x => s"${x._1}: ${x._2}\n").mkString +
        "***Top 5 Domains:***\n" +
        topDomains.toSeq.sortWith(_._2 > _._2).take(5).map(x => s"${x._1}: ${x._2}\n").mkString)
    }
  }

  override def receive: Receive = {
    case RequestAllStats() => {
      val supervisorActor = context.actorSelection("/user/stat-supervisor")
      supervisorActor ! RequestStats()
      val emojiActor = context.actorSelection("/user/stat-supervisor/emojiTracker")
      emojiActor ! RequestStats()
      val hashtagActor = context.actorSelection("/user/stat-supervisor/hashtagTracker")
      hashtagActor ! RequestStats()
      val URLActor = context.actorSelection("/user/stat-supervisor/urlTracker")
      URLActor ! RequestStats()
    }

    case SupervisorStats(count, picsCount, startTime) => {
      gotSupervisorStats = true
      this.count = count
      this.picsCount = picsCount
      this.startTime = startTime
      sendReport()
    }

    case EmojiStats(count, top) => {
      gotEmojiStats = true
      emojiCount = count
      topEmojis = top
      sendReport()
    }

    case HashtagStats(top) => {
      gotHashtagStats = true
      topHashtags = top
      sendReport()
    }

    case URLStats(count, top) => {
      gotURLStats = true
      URLCount = count
      topDomains = top
      sendReport()
    }

  }

}