package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.util.Workspace

trait WorkspaceAware {
  /**
    * The workspace the entity is located in.
    */
  var workspace: Workspace = Workspace.Default
}
