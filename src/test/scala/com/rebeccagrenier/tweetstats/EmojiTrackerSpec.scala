package com.rebeccagrenier.tweetstats

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef, TestProbe }

class EmojiTrackerSpec(_system: ActorSystem)
  extends TestKit(_system)
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("tweet-stats"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "An EmojiTracker Actor" should "increase count by one when any ProcessRequest is sent to it" in {
    val probe = TestProbe()
    val emojiTracker = system.actorOf(EmojiTracker.props())
    val text = "This is text even without an emoji it doesn't matter because StatsSupervisor does that"
    val codePoints = StatsSupervisor.UTF32point(text)

    emojiTracker.tell(EmojiTracker.ProcessTweet(codePoints), probe.ref)
    emojiTracker.tell(Query.RequestStats(), probe.ref)
    val response = probe.expectMsgType[Query.EmojiStats]
    response.count should ===(1)
  }

  "An EmojiTracker Actor" should "Count an Emoji in the totals if included in a tweet" in {
    val probe = TestProbe()
    val emojiTracker = system.actorOf(EmojiTracker.props())
    val text = "Jungle shower @amberleighwest 💚🌴\n👉 Follow @bikinielegance \u2022\n#swims"
    val codePoints = StatsSupervisor.UTF32point(text)
    emojiTracker.tell(EmojiTracker.ProcessTweet(codePoints), probe.ref)
    emojiTracker.tell(Query.RequestStats(), probe.ref)
    val response = probe.expectMsgType[Query.EmojiStats]
    response.top.get("palm_tree") should ===(Some(1))

  }

  "An EmojiTracker Actor" should "Count an Emoji twice in totals if it is included twice in a tweet" in {
    val probe = TestProbe()
    val emojiTracker = system.actorOf(EmojiTracker.props())
    val text = "Jungle shower @amberleighwest 👉 \n👉 Follow @bikinielegance \u2022\n#swims"
    val codePoints = StatsSupervisor.UTF32point(text)
    emojiTracker.tell(EmojiTracker.ProcessTweet(codePoints), probe.ref)
    emojiTracker.tell(Query.RequestStats(), probe.ref)
    val response = probe.expectMsgType[Query.EmojiStats]
    response.top.get("point_right") should ===(Some(2))

  }

}