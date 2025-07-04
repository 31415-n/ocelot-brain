package totoro.ocelot.brain.util

import totoro.ocelot.brain.{Ocelot, Settings}

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable

object ThreadPoolFactory {
  val priority: Int = {
    val custom = Settings.get.threadPriority
    if (custom < 1) Thread.MIN_PRIORITY + (Thread.NORM_PRIORITY - Thread.MIN_PRIORITY) / 2
    else custom max Thread.MIN_PRIORITY min Thread.MAX_PRIORITY
  }

  def create(name: String, threads: Int): ScheduledExecutorService = Executors.newScheduledThreadPool(threads,
    new ThreadFactory() {
      private val baseName = "Ocelot-" + name + "-"

      private val threadNumber = new AtomicInteger(1)

      private val group = System.getSecurityManager match {
        case null => Thread.currentThread().getThreadGroup
        case s => s.getThreadGroup
      }

      def newThread(r: Runnable): Thread = {
        val thread = new Thread(group, r, baseName + threadNumber.getAndIncrement)
        if (!thread.isDaemon) {
          thread.setDaemon(true)
        }
        if (thread.getPriority != priority) {
          thread.setPriority(priority)
        }
        thread
      }
    })

  val safePools: mutable.ArrayBuffer[SafeThreadPool] = mutable.ArrayBuffer.empty[SafeThreadPool]

  def createSafePool(name: String, threads: Int): SafeThreadPool = {
    val handler = new SafeThreadPool(name, threads)
    safePools += handler
    handler
  }
}

class SafeThreadPool(val name: String, val threads: Int) {
  private var _threadPool: ScheduledExecutorService = _

  def withPool(f: ScheduledExecutorService => Future[_], requiresPool: Boolean = true): Option[Future[_]] = {
    if (_threadPool == null) {
      Ocelot.log.warn("Error handling file saving: Did the server never start?")
      if (requiresPool) {
        Ocelot.log.warn("Creating new thread pool.")
        newThreadPool()
      } else {
        return None
      }
    } else if (_threadPool.isShutdown || _threadPool.isTerminated) {
      Ocelot.log.warn("Error handling file saving: Thread pool shut down!")
      if (requiresPool) {
        Ocelot.log.warn("Creating new thread pool.")
        newThreadPool()
      } else {
        return None
      }
    }
    Option(f(_threadPool))
  }

  def newThreadPool(): Unit = {
    if (_threadPool != null && !_threadPool.isTerminated) {
      _threadPool.shutdownNow()
    }
    _threadPool = ThreadPoolFactory.create(name, threads)
  }

  def waitForCompletion(): Unit = withPool(threadPool => {
    try {
      threadPool.shutdown()
      var terminated = threadPool.awaitTermination(15, TimeUnit.SECONDS)
      if (!terminated) {
        Ocelot.log.warn("Warning: Completing all tasks has already taken 15 seconds!")
        terminated = threadPool.awaitTermination(105, TimeUnit.SECONDS)
        if (!terminated) {
          Ocelot.log.error("Warning: Completing all tasks has already taken two minutes! Aborting")
          threadPool.shutdownNow()
        }
      }
    } catch {
      case e: InterruptedException => e.printStackTrace()
    }
    null
  }, requiresPool = false)
}
