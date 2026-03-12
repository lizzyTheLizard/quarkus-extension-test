package site.gutschi.quarkustestextension.it.log
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import site.gutschi.quarkustestextension.runtime.log.LoggingService
import kotlin.test.assertFails
import kotlin.test.expect

@QuarkusTest
class LoggedTestServiceTest {
    @Inject
    lateinit var loggedTestService: LoggedTestService
    @Inject
    lateinit var loggingService: LoggingService

    @Test
    fun doSomething() {
        loggedTestService.doSomething()
        val invocations = loggingService.getInvocations(LoggedTestService::class.java, LoggedTestService::doSomething.name)
        expect(1){ invocations.size }
        expect(LoggedTestService::class.qualifiedName) {invocations.first().className}
        expect(LoggedTestService::doSomething.name) {invocations.first().methodName}
        expect(0) {invocations.first().args.size}
        expect(null) {invocations.first().result}
        expect(null) {invocations.first().throwable}
    }

    @Test
    fun doSomethingWithDependency() {
        loggedTestService.doSomethingWithDependency()
        val invocations = loggingService.getInvocations(LoggedTestService::class.java, LoggedTestService::doSomethingWithDependency.name)
        expect(1){ invocations.size }
        expect(LoggedTestService::class.qualifiedName) {invocations.first().className}
        expect(LoggedTestService::doSomethingWithDependency.name) {invocations.first().methodName}
        expect(0) {invocations.first().args.size}
        expect(null) {invocations.first().result}
        expect(null) {invocations.first().throwable}
    }

    @Test
    fun doSomethingWithInput() {
        loggedTestService.doSomethingWithInput("test", 42)
        val invocations = loggingService.getInvocations(LoggedTestService::class.java, LoggedTestService::doSomethingWithInput.name)
        expect(1){ invocations.size }
        expect(LoggedTestService::class.qualifiedName) {invocations.first().className}
        expect(LoggedTestService::doSomethingWithInput.name) {invocations.first().methodName}
        expect(2) {invocations.first().args.size}
        expect("test") {invocations.first().args[0]}
        expect(42) {invocations.first().args[1]}
        expect(null) {invocations.first().result}
        expect(null) {invocations.first().throwable}
    }

    @Test
    fun doSomethingWithException() {
        val exception = assertFails { loggedTestService.doSomethingWithException() }
        val invocations = loggingService.getInvocations(LoggedTestService::class.java, LoggedTestService::doSomethingWithException.name)
        expect(1){ invocations.size }
        expect(LoggedTestService::class.qualifiedName) {invocations.first().className}
        expect(LoggedTestService::doSomethingWithException.name) {invocations.first().methodName}
        expect(0) {invocations.first().args.size}
        expect(null) {invocations.first().result}
        expect(exception) {invocations.first().throwable}
    }

    @Test
    fun doSomethingWithReturnValue() {
        loggedTestService.doSomethingWithReturnValue()
        val invocations = loggingService.getInvocations(LoggedTestService::class.java, LoggedTestService::doSomethingWithReturnValue.name)
        expect(1){ invocations.size }
        expect(LoggedTestService::class.qualifiedName) {invocations.first().className}
        expect(LoggedTestService::doSomethingWithReturnValue.name) {invocations.first().methodName}
        expect(0) {invocations.first().args.size}
        expect("Hello, World!") {invocations.first().result}
        expect(null) {invocations.first().throwable}
    }


}
