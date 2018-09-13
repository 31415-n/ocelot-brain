package totoro.ocelot.brain.machine

/**
  * Used to signal that the direct call limit for the current server tick has
  * been reached in [[Machine]].invoke(String, String, Object[])}.
  */
class LimitReachedException extends Exception
