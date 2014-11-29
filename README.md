spring-factory
==============
EDIT: **In Spring 4.1 we finally get a reasonable way to do method injection + java @Configuration. Thus, I recommend taking a look at [@Lookup methods](http://docs.spring.io/spring-framework/docs/4.1.0.RELEASE/javadoc-api/org/springframework/beans/factory/annotation/Lookup.html) instead of reading further**

~~Tiny Spring library to try and ease the pain of using prototype scoped beans inside of singleton scoped services.
Don't make everything singletons!~~


WiringFactory Usage
====================

WiringFactory is a little Spring extension which tries to help reduce boilerplate when working with protoype scope 
beans.  There is a longer discussion below about the why and what, but here's just a quick snippet that shows 
what its for:

```java
// we have to build a factory type to take runtime parameters to construct instances
public class ExportCsvParserFactory extends WiringFactorySupport<ExportCsvParser> {

  public ExportCsvParser makeFor(File csvFile) {
    // we construct the prototype instance, not spring; the wire method 
    // provided by WiringFactorySupport allows spring to do @Resource injection, etc.
    return wire(new ExportCsvParser(csvFile));
  }
}
```
Then somewhere in a `@Configuration` class:
```java
// in the spring @Configuration file you then just register the factory; no need to register 
// the prototype definition-- spring-factory will do that for us! 
// (ExportCsvParserFactory class could've also been discovered by component scanning)
@Bean
public ExportCsvParserFactory exportCsvParserFactory() {
  return new ExportCsvParserFactory();
}
```

That's it! The spring-factory extension detects all beans that extend `WiringFactory` (which `WiringFactorySupport`
does) and then automatically creates the prototype definition for the types it creates.  Then inside the factory
that you make, you construct the bean instance however you want (passing whatever constructor args you need) and then
just pass that instance to the wire method that `WiringFactorySupport` provides.  The `wire` method takes a `T` and
returns a `T` - thus, idiomatically, just use it inline in the return statement as above.

Now you can do `@Resource` injects and `@PostConstruct` and whatever else you want:

```java
@PrototypeComponent
public class ExportCsvParser implements ICsvParser, AutoCloseable {

  private final File csvFile;
  private final InputStream is;

  // this gets injected during the call to wire() in the factory just like a
  // normal singleton bean
  @Resource private Database db;  

  public ExportCsvParser(File csvFile) {
    this.csvFile = csvFile;
    this.is = CharStreams.newInputStream(csvFile, Charsets.UTF8);
  }
  
  @PostConstruct
  public void afterCreate() {
    // these work too!
  }

  // other methods

  @Override
  public void close() throws Exception {
    this.is.close();
  }
}
```

Note that `PrototypeComponent` annotation is not required.  It is just there for documentation reasons.  Since
`@Resource` injection in non-singletons is not as common in our code base its probably best to remind the reader
that this is a bean and a prototype bean at that.

*Important*: to enable this fancy support you have to `@Import(SpringFactoryBeans.class)` somewhere in your spring
configuration.

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

What happens if we need multiple instances with _different_ parameters?  Well we have a few options and trade-offs of:
boilerplate, binding to the spring framework, and thread safety.

An Example
===========

We want an `IngestService` that reads CSV files and does something with them.  We are going to use `IngestService` in
different applications and in some applications we want the csv parser to load things in to a Database and in some
cases we want the csv parser to just emit them to another file. So who are our players:

* `Database` a singleton service that allows you to get db connections, etc.
* `ICsvParser` an interface for parsing csv files
* `ExportingCsvParser` an implementation of `ICsvParser` that just does some transformation of the file and writes it to another file
* `DbLoadingCsvParser` an implementation of `ICsvParser` that writes to the `Database`
* `IngestService` a singleton instance that needs to call `ICsvParser`

The problem is that we want `ICsvParser` to be stateful – we want to call `close()` on it when we were done
(for example).  That means that an instance of a csvParser works for a particular Csv file so we have to pass that in
to a constructor or a method or something.  Since we like immutable objects, we'll pass it in to the constructor.
But here's the problem, we want spring to own and inject `ICsvParser` with whatever particular implementation that
we have configured for this particular application.  In some applications this will be `ExportingCsvParser` and in
others `DbLoadingCsvParser`.  Hmm... we can't put a `@Resource` field in `IngestService` because `IngestService`
is a singleton and let's say it can be called from many threads – we need a different instance of `ICsvParser` for
each client thats using `IngestService`....

In Spring you can create singleton scoped beans or prototype scoped beans.  Singleton beans are alive for the
container whereas each time you inject a prototype bean you get a new instance.  Prototype beans are intended to
be "stateful" for the particular instance of the activity you're doing.  Prototype scoped beans can be injected with
other prototype scoped beans and other singletons, but singletons can only be injected with other singletons.
Why is that?  Well think about it – a singleton is injected only once when it is first constructed.  So if satisfy
the prototype dependencies, it will construct new prototype instances to inject them in to the new singleton
instance... but that's the only time new instances of the prototype beans will be created.  Those references are set
that singleton will always refer to those particular prototype instances!  This is called the scope inversion problem.
Spring has come up with a few solutions to this problem through magical proxying... but we don't use this
(pretty big violation of the principle of least surprise since normal java references don't behave as you expect).

So barring magic, what do we generally do?  Create a factory class that is a singleton and inject that instead!
So if `ICsvParser` didn't have any constructor arguments you could do
`@Resource private ObjectFactory<ICsvParser> csvParserFactory`  – this `ObjectFactory` type is a built in spring
factory that it generates on the fly whenever you want.  Then at runtime in the `IngestService` you can call
`csvParserFactory.getObject()` and it will return an instance of the bean.  If that bean is prototype scoped then you
get a new instance of the bean for every `getObject()` call.  If its a singleton you get the single instance
regardless of how many times you call `getObject()`.  So its a nice way for consumers to not have to care about the
scope of their dependencies.  That's a great solution and we didn't have to make a new `CsvParserFactory` interface,
`ExportingCsvParserFactory` class, and `DbLoadingCsvParserLoading` class.  But crap, we can't pass the filename in
to `ObjectFactory.getObject()` it takes no arguments...

One option here is to use the `ApplicationContext` directly.  you can call `getBean("beanName", arg0, arg1, ...)`
and then cast it.  This works but you have to inject a whole application context (Eek!).  We generally don't like that.
`ApplicationContexts` do "too much", too easy to get into nasty coupling scenarios making it really hard to test and
mock.  Maybe I'll work on some abstractions to ease this... but until then we need types :/  It's a strongly typed
language without function types...

So we need a `CsvParserFactory` and implementations for each of our kinds of `CsvParserFactories` that we might
inject.  The factory will just have one method like `ICsvParser makeFor(File csvFile)` to capture that runtime
parameter that parameterizes the parser instance.  Ok so we can do that and all is well.  The `CsvParserFactory`
classes can both be singletons (as factories generally are) and they can be `@Resource` injected with whatever is
needed.  The `ExportingCsvParser` and `DbLoadingCsvParser` classes are no longer Spring beans – they are just normal
java objects.  The factory is a bean so whatever bean dependencies the parser instances need would just need to be
`@Resource` injected in to the factory and then the factory can pass them in to the csv parser constructor (just like
we used to do in the "good old days" before fancy DI frameworks).

This could be the end of our story, and probably usually will be.  However there are some disadvantages here:

1. Since `DbLoadingCsvParser` and `ExportCsvParser` are no longer spring beans they can't be `@Resource` injected.
Any dependencies have to be passed in.  So every time I need a new dependency I have to propagate it through the
constructor, update the factory to inject it there, etc.
2. These classes don't get spring events, callbacks, or any other normal spring features.

We want the best of both worlds.  We want a prototype that we can construct – passing whatever runtime parameters we
need – but that can also be `@Resource` injected (and other spring features like lifecycle callbacks, etc.)

See the above WiringFactory sample usage to see how the spring-factory helps this case.

Final note
============
I think CDI (i.e. the Weld framework) solves this whole problem (and many others related to dependency injection and
contextual information) in a much more elegant way with dependency bijection and loosely coupling dependencies in
scopes to a context that can be tied to a thread.  One day I'll work on trying to implement these concepts in
spring... but outjection is hard.
