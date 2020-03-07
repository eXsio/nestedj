## Installation

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.eXsio</groupId>
    <artifactId>nestedj</artifactId>
    <version>5.0.2</version>
</dependency>

```

## In Memory Usage

In order to use NestedJ, You have to configure it. In 9 our of 10 cases you will want to use the preconfigured builder methods available in the ```InMemoryNestedNodeRepositoryFactory``` class:

```java
    AtomicLong id = new AtomicLong();
    List<YourNode> nodes = Lists.newArrayList();
    InMemoryNestedNodeRepositoryConfiguration<Long, YourNode> configuration = new InMemoryNestedNodeRepositoryConfiguration<>(id::incrementAndGet, nodes, new TestInMemoryTreeDiscriminator());
    return InMemoryNestedNodeRepositoryFactory.create(configuration, new InMemoryLock<>(YourNode::getDiscriminator));
    
```

or if you don't need multithreaded access to the Repository and Tree Discriminator:

```java
    AtomicLong id = new AtomicLong();
    InMemoryNestedNodeRepositoryConfiguration<Long, YourNode> configuration = new InMemoryNestedNodeRepositoryConfiguration<>(id::incrementAndGet);
    return InMemoryNestedNodeRepositoryFactory.create(configuration);
    
```

The Node Class has to implement the ```NestedNode``` interface so that the logic can operage on Nested Node specific columns.

## Using in-memory implementation with noSQL storage

You can use the in-memory implementation to modify and traverse the Tree. anytime you need to persist it, you can just get your Nodes
by calling InMemoryNestedNodeRepositoryConfiguration::getNodes and store the flat List of the Tree Nodes wherever you want! 
You can serialize them to JSON or XML and store in noSQL database or even in flat file. 