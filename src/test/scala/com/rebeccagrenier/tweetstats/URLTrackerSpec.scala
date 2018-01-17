package com.rebeccagrenier.tweetstats

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef, TestProbe }
import com.danielasfregola.twitter4s.entities.{ Entities, Tweet }
import scala.concurrent.duration._

import util.FileSupport

class URLTrackerSpec(_system: ActorSystem)
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

  "A URLTracker Actor" should "increase count by one when any ProcessRequest is sent to it" in {
    val probe = TestProbe()
    val urlTracker = system.actorOf(URLTracker.props())
    val entities = tweets.head.entities

    urlTracker.tell(URLTracker.ProcessTweet(entities), probe.ref)
    urlTracker.tell(Query.RequestStats(), probe.ref)
    val response = probe.expectMsgType[Query.URLStats]
    response.count should ===(1)
  }

  "A URLTracker Actor" should "Count a domain in the totals if url is included in a tweet" in {
    val probe = TestProbe()
    val urlTracker = system.actorOf(URLTracker.props())
    val entities = tweets.head.entities

    urlTracker.tell(URLTracker.ProcessTweet(entities), probe.ref)
    urlTracker.tell(Query.RequestStats(), probe.ref)
    val response = probe.expectMsgType[Query.URLStats]
    response.top.get("buff.ly") should ===(Some(1))

  }

  "A URLTracker Actor" should "Count a domain twice in totals if it is included twice in a tweet" in {
    val probe = TestProbe()
    val urlTracker = system.actorOf(URLTracker.props())
    val entities = tweets.tail.head.entities

    urlTracker.tell(URLTracker.ProcessTweet(entities), probe.ref)
    urlTracker.tell(Query.RequestStats(), probe.ref)
    val response = probe.expectMsgType[Query.URLStats]
    response.top.get("manning.com") should ===(Some(2))

  }

}