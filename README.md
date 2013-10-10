spring-factory
==============

Tiny library adding simple way to inject "prototype" scoped beans that need run-time parameters.
Don't make everything singletons!

# Why?
For an object to be useful it generally needs _collaborators_ (usually injected through dependency injection)
and (optionally) _parameterization_ -- i.e. inputs that will affect the behavior.

How do we configure the _collaborators_?  Well we used to use the service locator but that was terrible.  Dependency Injection
pushes those _collaborators_ in to the object (via constructor args, setters, or fields).  So how does this help us
write and maintain complex software?  Well we can create _services_, decouple the interfaces from the implementations,
and then let a framework (Spring, Guice, Dagger, etc.) do the wiring.

But what about _parameterization_? Well when the parameters are static for the entire lifecycle of the application
then its simple: we create _singletons_! We parameterize them at construction time (usually through our dependency
injection framework's configuration layer).  Then our _collaborators_ and our _parameterizatoin_ live right next to
either other in fields in the object.

What happens if we need multiple instances with _different_ parameters?  Well in Spring we have a few options:

* Create a @Bean method that takes parameters and then let the service call the applicationContext to construct:

```java
@Configuration
public class MyBeans {
    @Bean @Scope("prototype")
    public HelloService helloService(String name) {
        return new HelloService(name);
    }
}

@Resource private ApplicationContext ctx;

public String greet(String name) {
    HelloService service = ctx.getBean(HelloService.class, name);
    return service.sayHello();
}

```

This is not ideal because:
1. it introduces coupling between your greet method and the ApplictaionContext class.  Really this code just wants a
method to "make an instance given a String" -- it doesn't need all of the other ApplicationContext methods.
1. Also adds a dependency on the spring framework in this class which makes it less easy to use this in non spring
environments

Another option is to make a framework