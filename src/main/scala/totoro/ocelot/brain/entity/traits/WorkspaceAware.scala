package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.workspace.Workspace

/**
  * This trait provides some workspace context, to allow querying the time of day, for example.
  */
trait WorkspaceAware {
  private var _workspace: Workspace = _

  def workspace: Workspace = _workspace
  def workspace_=(value: Workspace): Unit = {
    onWorkspaceChange(value)
    _workspace = value
  }

  def onWorkspaceChange(newWorkspace: Workspace): Unit = {}
}
