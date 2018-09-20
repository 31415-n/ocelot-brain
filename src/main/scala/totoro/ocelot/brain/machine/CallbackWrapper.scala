package totoro.ocelot.brain.machine

import java.lang.reflect.Method

import org.objectweb.asm.{ClassWriter, Opcodes, Type}
import totoro.ocelot.brain.Ocelot

import scala.collection.mutable

object CallbackWrapper {
  private final val ObjectNameASM = classOf[AnyRef].getName.replace('.', '/')
  private final val CallbackCallDesc = Type.getMethodDescriptor(classOf[CallbackCall].getMethod("call", classOf[AnyRef], classOf[Context], classOf[Arguments]))
  private final val CallbackCallInterface = Array(classOf[CallbackCall].getName.replace('.', '/'))
  private final val MethodIdCache = mutable.Map.empty[Method, String]
  private final val CallbackWrapperCache = mutable.Map.empty[Method, CallbackCall]

  def createCallbackWrapper(method: Method): CallbackCall = this.synchronized {
    CallbackWrapperCache.getOrElseUpdate(method, createWrapper(method, CallbackCallInterface, emitCallbackCall).asInstanceOf[CallbackCall])
  }

  private def createWrapper(m: Method, interfaces: Array[String], emitCode: (Method, ClassWriter) => Unit): AnyRef = {
    val className = "generated.li.cil.oc.CallWrapper_" + generateId(m)
    if (!GeneratedClassLoader.containsClass(className)) {
      val cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
      cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, className.replace('.', '/'), null, ObjectNameASM, interfaces)
      emitConstructor(cw)
      emitCode(m, cw)
      cw.visitEnd()
      GeneratedClassLoader.addClass(className, cw.toByteArray)
    }

    GeneratedClassLoader.findClass(className).newInstance().asInstanceOf[AnyRef]
  }

  private def emitConstructor(cw: ClassWriter): Unit = {
    val mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
    mv.visitCode()
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ObjectNameASM, "<init>", "()V", false)
    mv.visitInsn(Opcodes.RETURN)
    mv.visitMaxs(1, 1)
    mv.visitEnd()
  }

  private def emitCallbackCall(m: Method, cw: ClassWriter): Unit = {
    val className = m.getDeclaringClass.getName.replace('.', '/')
    val mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "call", CallbackCallDesc, null, null)
    mv.visitCode()
    mv.visitVarInsn(Opcodes.ALOAD, 1)
    mv.visitTypeInsn(Opcodes.CHECKCAST, className)
    mv.visitVarInsn(Opcodes.ALOAD, 2)
    mv.visitVarInsn(Opcodes.ALOAD, 3)
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, m.getName, Type.getMethodDescriptor(m), false)
    mv.visitInsn(Opcodes.ARETURN)
    mv.visitMaxs(3, 3)
    mv.visitEnd()
  }

  private def generateId(m: Method): String = MethodIdCache.getOrElseUpdate(m, m.getDeclaringClass.getName.replace('.', '_') + "_" + m.getName)

  private object GeneratedClassLoader extends ClassLoader(Ocelot.getClass.getClassLoader) {
    private val GeneratedClasses = mutable.Map.empty[String, Class[_]]

    def containsClass(name: String): Boolean = GeneratedClasses.contains(name)

    def addClass(name: String, bytes: Array[Byte]): Unit = {
      GeneratedClasses += name -> defineClass(name, bytes, 0, bytes.length)
    }

    override def findClass(name: String): Class[_] = {
      GeneratedClasses.get(name) match {
        case Some(clazz) => clazz
        case _ => super.findClass(name)
      }
    }
  }

}
