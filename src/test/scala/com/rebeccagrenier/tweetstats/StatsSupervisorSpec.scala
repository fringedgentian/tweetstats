package com.rebeccagrenier.tweetstats

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef, TestProbe }
import com.danielasfregola.twitter4s.entities.Tweet
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import StatsSupervisor._
import Query._
import util.FileSupport

class StatsSupervisorSpec(_system: ActorSystem)
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

  "A StatsSupervisor Actor" should "increase count by one when a tweet is processed" in {
    val probe = TestProbe()
    val statSupervisor = system.actorOf(StatsSupervisor.props())
    val tweet = tweets.head

    statSupervisor.tell(ProcessTweet(tweet), probe.ref)
    statSupervisor.tell(RequestStats(), probe.ref)
    val response = probe.expectMsgType[SupervisorStats]
    response.count should ===(1)
  }

  "A StatsSupervisor Actor" should "increase picture count by one when a tweet with picture is processed" in {
    val probe = TestProbe()
    val statSupervisor = system.actorOf(StatsSupervisor.props())
    val tweet = tweets.head

    statSupervisor.tell(ProcessTweet(tweet), probe.ref)
    statSupervisor.tell(RequestStats(), probe.ref)
    val response = probe.expectMsgType[SupervisorStats]
    response.picsCount should ===(1)
  }

  "A StatsSupervisor Actor" should "not increase picture count by one when a tweet without picture is processed" in {
    val probe = TestProbe()
    val statSupervisor = system.actorOf(StatsSupervisor.props())
    //only the first tweet has media
    val tweet = tweets.tail.head

    statSupervisor.tell(ProcessTweet(tweet), probe.ref)
    statSupervisor.tell(RequestStats(), probe.ref)
    val response = probe.expectMsgType[SupervisorStats]
    response.picsCount should ===(0)
  }

  /*
	TODO Learn how to test Actors sending messages back and forth to each other
  "A StatsSupervisor Actor" should "forward tweets with emojis to the EmojiTracker child" in {
    val probe = TestProbe()
    val tweet = tweets.tail.head
    val statSupervisor = system.actorOf(StatsSupervisor.props())
    for (res <- system.actorSelection("/user/stat-supervisor/emojiTracker").resolveOne(500 millis)) {
      res.expectMsg(ProcessTweet(UTF32point(tweet.text)))
    }

    statSupervisor.tell(ProcessTweet(tweet), probe.ref)

  }
  */

}
