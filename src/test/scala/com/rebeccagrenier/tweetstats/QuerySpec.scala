package com.rebeccagrenier.tweetstats

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef, TestProbe }
import com.danielasfregola.twitter4s.entities.{ Tweet }
import scala.concurrent.duration._

import util.FileSupport

class QuerySpec(_system: ActorSystem)
  extends TestKit(_system)
  with Matchers
  with FlatSpecLike
  with FileSupport
  with BeforeAndAfterAll {
  /*
 TODO Learn how to test output
  def this() = this(ActorSystem("tweet-stats"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  val tweets = fromJsonFileAs[Seq[Tweet]]("src/test/resources/Tweets.json")

  "A Query Actor" should "not print report if all actors did not report" in {
    val probe = TestProbe()
    val queryActor = system.actorOf(Query.props())

    queryActor.tell(Query.RequestAllStats(), probe.ref)
    println(queryActor.messages)

  }
  */

}