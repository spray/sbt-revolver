package spray.revolver

import sbt.ProjectRef
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec

case class RevolverState(processes: Map[ProjectRef, AppProcess]) {
  def addProcess(project: ProjectRef, process: AppProcess): RevolverState = copy(processes = processes + (project -> process))
  def removeProcess(project: ProjectRef): RevolverState = copy(processes = processes - project)

  def exists(project: ProjectRef): Boolean = processes.contains(project)
  def runningProjects: Seq[ProjectRef] = processes.keys.toSeq
  def getProcess(project: ProjectRef): Option[AppProcess] = processes.get(project)
}

object RevolverState {
  def initial = RevolverState(Map.empty)
}

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
