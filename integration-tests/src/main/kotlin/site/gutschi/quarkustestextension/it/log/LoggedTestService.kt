package site.gutschi.quarkustestextension.it.log

import jakarta.enterprise.context.ApplicationScoped
import site.gutschi.quarkustestextension.runtime.log.Logged

@ApplicationScoped
@Logged
class LoggedTestService(private val dependency: TestDependency) {
    fun doSomething() {
        println("Doing something")
    }

    fun doSomethingWithDependency() {
        println("Doing something with ${dependency.data}")
    }

    fun doSomethingWithInput(input: String, input2: Int) {
        println("Doing something with input: $input and $input2")
    }

    fun doSomethingWithException() {
        println("Doing something with exception")
        throw RuntimeException("Something went wrong")
    }

    fun doSomethingWithReturnValue(): String {
        println("Doing something with return value")
        return "Hello, World!"
    }
}