package com.rebeccagrenier.tweetstats

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef, TestProbe }
import com.danielasfregola.twitter4s.entities.{ HashTag, Tweet }
import scala.concurrent.duration._

import util.FileSupport

class HashtagTrackerSpec(_system: ActorSystem)
  extends TestKit(_system)
  with Matchers
  with FlatSpecLike
  with FileSupport
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("tweet-stats"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  val tweets = fromJsonFileAs[Seq[Tweet]]("src/test/resources/Tweets.json")

  "A HashtagTracker Actor" should "Count a hashtag in the totals if hashtag is included in a tweet" in {
    val probe = TestProbe()
    val hashtagTracker = system.actorOf(HashtagTracker.props())
    val hashtags = tweets.head.entities match {
      case Some(entities) => entities.hashtags
      case _ => Seq.empty[HashTag]
    }

    hashtagTracker.tell(HashtagTracker.ProcessTweet(hashtags), probe.ref)
    hashtagTracker.tell(Query.RequestStats(), probe.ref)
    val response = probe.expectMsgType[Query.HashtagStats]
    response.top.get("Scala") should ===(Some(1))

  }

}