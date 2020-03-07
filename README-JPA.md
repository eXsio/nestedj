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
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>5.4.9.Final</version>
</dependency>

```


## JPA Usage

In order to use NestedJ, You have to configure it. In 9 our of 10 cases you will want to use the preconfigured builder methods available in the ```JpaNestedNodeRepositoryFactory``` class:
 
 ```java
    //ID and Node classes
    JpaNestedNodeRepositoryConfiguration<Long, YourNode> configuration = new JpaNestedNodeRepositoryConfiguration<>(
                    entityManager, YourNode.class, Long.class, new YourJpaTreeDiscriminator() //Discriminator is optional, allows to create multiple trees in one table
            );
    return JpaNestedNodeRepositoryFactory.create(configuration); 

```
Here is the example entity that implements the ```NestedNode``` interface:

```java

@Entity
@Table(name = "nested_nodes")
public class TestNode implements NestedNode<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Column(name = "tree_left", nullable = false)
    protected Long treeLeft;

    @Column(name = "tree_right", nullable = false)
    protected Long treeRight;

    @Column(name = "tree_level", nullable = false)
    protected Long treeLevel;

    @Column(name = "parent_id")
    protected Long parentId;
    
   //getters, setters
}
```
