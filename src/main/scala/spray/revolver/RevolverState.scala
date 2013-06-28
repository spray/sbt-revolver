package spray.revolver

import sbt.ProjectRef

case class RevolverState(processes: Map[ProjectRef, AppProcess]) {
  def grabColor: (String, RevolverState) = {
    val (color, wheel) = colorWheel.grabColor
    (color, copy(colorWheel = wheel))
  }

  def addProcess(project: ProjectRef, process: AppProcess): RevolverState = copy(processes = processes + (project -> process))
  def removeProcess(project: ProjectRef): RevolverState = copy(processes = processes - project)

  def exists(project: ProjectRef): Boolean = processes.contains(project)
  def runningProjects: Seq[ProjectRef] = processes.keys.toSeq
  def getProcess(project: ProjectRef): Option[AppProcess] = processes.get(project)

}

object RevolverState {
  def initial = RevolverState(Map.empty, ColorWheel.simple)
}
