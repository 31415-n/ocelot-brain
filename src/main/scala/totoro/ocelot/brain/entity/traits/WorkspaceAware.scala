package totoro.ocelot.brain.entity.traits

import totoro.ocelot.brain.util.Workspace

trait WorkspaceAware {
  /**
    * The workspace the entity is located in.
    */
  def workspace: Workspace = Workspace.Default
}
