package site.gutschi.quarkustestextension.it.log

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TestDependency {
    val data= "Test Dependency Data"
}