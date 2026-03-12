package site.gutschi.quarkustestextension.deployment.log

import io.quarkus.arc.deployment.AdditionalBeanBuildItem
import io.quarkus.arc.deployment.ExcludedTypeBuildItem
import io.quarkus.arc.deployment.GeneratedBeanBuildItem
import io.quarkus.arc.deployment.GeneratedBeanGizmo2Adaptor
import io.quarkus.deployment.annotations.BuildProducer
import io.quarkus.deployment.annotations.BuildStep
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem
import org.jboss.jandex.AnnotationInstance
import org.jboss.jandex.DotName
import site.gutschi.quarkustestextension.runtime.log.Logged
import site.gutschi.quarkustestextension.runtime.log.LoggingService

class LogProcessor {
    private val logged = DotName.createSimple(Logged::class.java.getName())

    @BuildStep
    fun addLoggingServiceBean(): AdditionalBeanBuildItem {
        return AdditionalBeanBuildItem(LoggingService::class.java.name)
    }

    @BuildStep
    fun removeLoggedBeans(index: ApplicationIndexBuildItem, producer: BuildProducer<ExcludedTypeBuildItem>) {
        index.index.getAnnotations(logged).stream()
            .map { t: AnnotationInstance -> t.target().asClass() }
            .forEach { producer.produce(ExcludedTypeBuildItem(it.name().toString())) }
    }

    @BuildStep
    fun extendLoggingClasses(index: ApplicationIndexBuildItem, producer: BuildProducer<GeneratedBeanBuildItem>) {
        val classOutput = GeneratedBeanGizmo2Adaptor(producer)
        index.index.getAnnotations(logged).stream()
            .map { t: AnnotationInstance -> t.target().asClass() }
            .forEach { LoggerImplCreator(it, classOutput).generate()}
    }
}