package spray.revolver

import sbt.ProjectRef
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.collection.immutable.Queue

case class RevolverState(processes: Map[ProjectRef, AppProcess], colorPool: Queue[String]) {
  def addProcess(project: ProjectRef, process: AppProcess): RevolverState = copy(processes = processes + (project -> process))
  private[this] def removeProcess(project: ProjectRef): RevolverState = copy(processes = processes - project)
  def removeProcessAndColor(project: ProjectRef): RevolverState =
    getProcess(project) match {
      case Some(process) => removeProcess(project).offerColor(process.consoleColor)
      case None => this
    }

  def exists(project: ProjectRef): Boolean = processes.contains(project)
  def runningProjects: Seq[ProjectRef] = processes.keys.toSeq
  def getProcess(project: ProjectRef): Option[AppProcess] = processes.get(project)

  def takeColor: (RevolverState, String) =
    if (colorPool.nonEmpty) {
      val (color, nextPool) = colorPool.dequeue
      (copy(colorPool = nextPool), color)
    } else (this, "")

  def offerColor(color: String): RevolverState =
    if (color.nonEmpty) copy(colorPool = colorPool.enqueue(color))
    else this
}

object RevolverState {
  def initial = RevolverState(Map.empty, Queue.empty)
}

/**
 * Manages global state. This is not a full-blown STM so be cautious not to lose
 * state when doing several updates depending on another.
 */
object GlobalState {
  private[this] val state = new AtomicReference(RevolverState.initial)

  @tailrec def update(f: RevolverState => RevolverState): RevolverState = {
    val originalState = state.get()
    val newState = f(originalState)
    if (!state.compareAndSet(originalState, newState)) update(f)
    else newState
  }
  @tailrec def updateAndGet[T](f: RevolverState => (RevolverState, T)): T = {
    val originalState = state.get()
    val (newState, value) = f(originalState)
    if (!state.compareAndSet(originalState, newState)) updateAndGet(f)
    else value
  }

  def get(): RevolverState = state.get()
}
