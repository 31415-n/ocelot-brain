package totoro.ocelot.brain.user

/**
  * Represents a user of external app (ocelot.online, etc.).
  * It will play a role of Minecraft player in OpenComputers signals.
  *
  * @param nickname the nickname of a user, as it will appear in signals.
  */
case class User(nickname: String)
