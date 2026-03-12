package site.gutschi.quarkustestextension.deployment.log

import io.quarkus.builder.BuildException
import io.quarkus.gizmo2.ClassOutput
import io.quarkus.gizmo2.Gizmo
import io.quarkus.gizmo2.ParamVar
import io.quarkus.gizmo2.This
import io.quarkus.gizmo2.creator.*
import io.quarkus.gizmo2.desc.FieldDesc
import io.quarkus.gizmo2.desc.MethodDesc
import io.quarkus.gizmo2.impl.constant.ConstImpl
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jboss.jandex.ClassInfo
import org.jboss.jandex.MethodInfo
import org.jboss.jandex.MethodParameterInfo
import org.jboss.jandex.gizmo2.Jandex2Gizmo
import site.gutschi.quarkustestextension.runtime.log.LoggingService
import java.lang.reflect.Modifier
import kotlin.reflect.jvm.javaMethod

class LoggerImplCreator(private val originalClass: ClassInfo, private val classOutput: ClassOutput) {
    private lateinit var thisExpr: This
    private lateinit var loggerField: FieldDesc
    private val startLog = MethodDesc.of(LoggingService::startInvocation.javaMethod)
    private val exceptionLog = MethodDesc.of(LoggingService.RunningInvocation::finishWithException.javaMethod)
    private val returnLog = MethodDesc.of(LoggingService.RunningInvocation::finishWithResult.javaMethod)
    private val noReturnLog = MethodDesc.of(LoggingService.RunningInvocation::finishWithoutResult.javaMethod)

    fun generate(){
        checkInput()
        val name = originalClass.toString() + "Impl"
        val superClass = Jandex2Gizmo.classDescOf(originalClass)
        val constructors = originalClass.constructors()
        val methods = originalClass.methods().stream().filter { isBusinessMethod(it) }
        Gizmo.create(classOutput).class_(name) {
            thisExpr = it.this_()
            it.extends_(superClass)
            it.public_()
            it.packagePrivate()
            it.addAnnotation(Singleton::class.java)
            createLoggerField(it)
            constructors.forEach { mi -> createConstructor(it, mi)}
            methods.forEach { mi -> createMethod( it, mi) }
        }
    }

    private fun checkInput(){
        if(originalClass.isAbstract) throw BuildException("Class ${originalClass.name()} is abstract. This is not allowed on a @Logged class")
        if(originalClass.isFinal) throw BuildException("Class ${originalClass.name()} is final. This is not allowed on a @Logged class")
        if(originalClass.isSealed) throw BuildException("Class ${originalClass.name()} is sealed. This is not allowed on a @Logged class")
        if(originalClass.isInterface) throw BuildException("${originalClass.name()} is an interface. This is not allowed on a @Logged class")
        originalClass.methods().stream()
            .filter { isBusinessMethod(it) }
            .filter { Modifier.isFinal(it.flags().toInt()) }
            .forEach { throw BuildException("Method ${it.name()} is final. This is not allowed on a @Logged class") }
    }

    private fun createLoggerField(cc: ClassCreator) {
        loggerField = cc.field("logger") {
            it.packagePrivate()
            it.setType(LoggingService::class.java)
            it.addAnnotation(Inject::class.java)
        }
    }

    private fun createConstructor(cc: ClassCreator, mi: MethodInfo){
        val superCtor = Jandex2Gizmo.constructorDescOf(mi)
        cc.constructor(superCtor) {
            it.packagePrivate()
            val args = mi.parameters().map{pi -> createParameter(it, pi)}
            it.body { bc->
                bc.invokeSpecial(superCtor, thisExpr, args)
                bc.return_()
            }
        }
    }

    private fun createMethod(cc: ClassCreator, mi: MethodInfo) {
        val superExpr = Jandex2Gizmo.methodDescOf(mi)
        val methodName = ConstImpl.of(superExpr.name())
        val className = ConstImpl.of(originalClass.name().toString())
        val returnType = Jandex2Gizmo.classDescOf(mi.returnType())

        cc.method(mi.name()) {
            val args = mi.parameters().map{pi -> createParameter(it, pi)}
            it.public_()
            it.addAnnotation(Override::class.java)
            it.returning(returnType)
            it.body { bc->
                val params = arrayOf(className,methodName, bc.newArray(Any::class.java, args))
                val invocation = bc.localVar("invocation", bc.invokeVirtual(startLog, thisExpr.field(loggerField), *params ))
                bc.try_ { tc ->
                    tc.body { bc ->
                        if (superExpr.isVoidReturn) {
                            bc.invokeSpecial(superExpr, thisExpr, args)
                            bc.invokeVirtual(noReturnLog, invocation)
                            bc.return_()
                        }
                        else {
                            val result = bc.localVar("result",bc.invokeSpecial(superExpr, thisExpr, args))
                            bc.invokeVirtual(returnLog, invocation, result)
                            bc.return_(result)
                        }
                    }
                    tc.catch_(Exception::class.java, "result") { bc, exception ->
                        bc.invokeVirtual(exceptionLog, invocation, exception)
                        bc.throw_(exception)
                    }
                }
            }
        }
    }

    private fun createParameter(ec: ExecutableCreator, pi: MethodParameterInfo): ParamVar {
        val name = pi.name()
        val type = Jandex2Gizmo.classDescOf(pi.type())
        return ec.parameter(name, type)
    }

    private fun isBusinessMethod(method: MethodInfo): Boolean {
        return !method.isConstructor && !method.isStaticInitializer
    }
}
