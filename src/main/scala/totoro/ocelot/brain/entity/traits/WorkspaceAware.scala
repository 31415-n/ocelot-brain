package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.workspace.Workspace

/**
  * This trait provides some workspace context, to allow querying the time of day, for example.
  */
trait WorkspaceAware {
  var workspace: Workspace = _
}
