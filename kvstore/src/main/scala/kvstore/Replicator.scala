package kvstore
import akka.actor.{ Cancellable, Props, Actor, ActorRef }
import scala.concurrent.duration._
object Replicator {
  case class Replicate(key: String, valueOption: Option[String], id: Long)
  case class Replicated(key: String, id: Long)

  case class Snapshot(key: String, valueOption: Option[String], seq: Long)
  case class SnapshotAck(key: String, seq: Long)
  def props(replica: ActorRef): Props = Props(new Replicator(replica))
}
class Replicator(val replica: ActorRef) extends Actor {
  import Replicator._
  import Replica._
  import context.{ dispatcher, system }

  /*
 * The contents of this actor is just a suggestion, you can implement it in any way you like.
 */
  // map from sequence number to pair of sender and request
  var acks = Map.empty[Long, (ActorRef, Replicate, Cancellable)]

  var _seqCounter = 0L
  def nextSeq = {
    val ret = _seqCounter
    _seqCounter += 1
    ret
  }
  def schedule(key: String, valueOption: Option[String], seq: Long): Cancellable =
    system.scheduler.schedule(0.millis, 100.millis) {
      replica ! Snapshot(key, valueOption, seq)
    }
  override def postStop(): Unit =
    acks foreach {
      case (_, (_, _, cancellable)) => {
        cancellable.cancel()
      }
    }
  /* TODO Behavior for the Replicator. */
  def receive: Receive = {
    case Replicate(key, valueOption, id) =>
      val seq = nextSeq
      acks += seq -> (sender, Replicate(key, valueOption, id), schedule(key, valueOption, seq))
    case SnapshotAck(key, seq) => acks.get(seq) match {
      case Some((primary, Replicate(key, _, id), cancellable)) =>
        cancellable.cancel()
        acks -= seq
        primary ! Replicated(key, id)
      case None =>
    }
  }
}