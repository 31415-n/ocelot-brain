package totoro.ocelot.brain.entity.traits

/**
 * Just keeps track of the last time the disk was accessed.
 */

trait DiskActivityAware {
  var lastDiskAccess: Long = -1L
}
