# NestedJ - a JPA / JDBC Nested Set implementation
[![Build Status](https://travis-ci.org/eXsio/nestedj.svg?branch=master)](https://travis-ci.org/eXsio/nestedj)
[![Coverity Status](https://scan.coverity.com/projects/8499/badge.svg?flat=1)](https://scan.coverity.com/projects/exsio-nestedj)
[![codecov](https://codecov.io/gh/eXsio/nestedj/branch/master/graph/badge.svg)](https://codecov.io/gh/eXsio/nestedj)

NestedJ is a Java library that implements the Nested Set pattern for Java.

## Overview
NestedJ can automate the insertion/moving/removing of tree nodes. It can also retrieve persisted Nodes as a flat or recursive Java structures / collections.

### What is Nested Set?

Nested Set is a RDBMS Tree implementation. It allows to fetch whole tree branches and find ancestors and descendants using single SQL statement. As usual, there is no freee lunch, so the price to pay in this case is a slightly more complex logic for modifying the tree (inserting / moving / removing nodes and branches). Fortunately NestedJ makes it very easy.

### Example

Given the below structure:

                       1 A 18
                        / \                    
                       /   \                   
                      /     \                 
                   2 B 7   8 C 17              
                    /         \ _______               
                   /\         /\       \          
                  /  \       /  \   15 I 16            
                 /    \     /    \             
                /   5 E 6  9 F 10 \            
             3 D 4             11 G 14
                                   \
                                    \
                                  12 H 13
                                  
You can query for an entire tree branch of node ```C``` using a query similar to this:

```
 SELECT * FROM TREE_TABLE WHERE LEFT >=8 AND RIGHT <= 17
```

You can query for a top treeLevel parent of a given (```H``` in this example) node using a query similar to this:

```
SELECT * FROM TREE_TABLE WHERE LEFT < 12 AND RIGHT > 13 AND LEVEL = 0
```

You can also query for an entire path leading to a given (```D``` in this example) node using a query similar to this:

```
SELECT * FROM TREE_TABLE WHERE LEFT < 3 AND RIGHT > 4 ORDER BY LEVEL ASC
```


Using the traditional ```parant_id``` relationship would mean firing multiple queries for each child / parent relationship.

##### Important: Nested Set is NOT a binary tree - the number of nodes on any treeLevel is unlimited. 

## Advantages of NestedJ

   - **No Reflection!** - no custom annotations, no additional boostrap logic
   - **Best achievable performance** - all tree actions are performed using a minimal number of Bulk SQL Queries, without loading any parts of the tree structure to the memory and without node-by-node processing
   - **No JPA mappings enforced** - no ManyToOne/OneToMany mappings needed for the lib to work (if you're using JPA implementation)
   - **No ID/PK class enforced** - no hardwired requirements for any particular ID/Primary Key class
   - **Fully customizable** - you can repimplement the parts you need and leave the rest intact
   - **Fully tested** - integration tests created for all possible tree operations
   - **Minimal number of Project dependencies** - only Guava and (JPA API or Spring JDBC) - depending on which implementation you want to use 

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
    <version>4.1.1</version>
</dependency>

```


## JPA Usage

In order to use NestedJ, You have to configure it. In 9 our of 10 cases you will want to use the preconfigured builder methods available in the ```JpaNestedNodeRepositoryFactory``` interface:
 
 ```java
    //ID and Node classes
    JpaNestedNodeRepositoryConfiguration<Long, TestNode> configuration = new JpaNestedNodeRepositoryConfiguration<>(
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
    
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getTreeLeft() {
        return treeLeft;
    }

    @Override
    public Long getTreeRight() {
        return treeRight;
    }

    @Override
    public Long getTreeLevel() {
        return treeLevel;
    }

    @Override
    public Long getParentId() {
        return parentId;
    }

    @Override
    public void setTreeLeft(Long treeLeft) {
        this.treeLeft = treeLeft;
    }

    @Override
    public void setTreeRight(Long treeRight) {
        this.treeRight = treeRight;
    }

    @Override
    public void setTreeLevel(Long treeLevel) {
        this.treeLevel = treeLevel;
    }

    @Override
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
```

## JDBC Usage

In order to use NestedJ, You have to configure it. In 9 our of 10 cases you will want to use the preconfigured builder methods available in the ```JdbcNestedNodeRepositoryFactory``` interface:

```java

    //ROW MAPPER FOR CREATING INSTANCES OF THE NODE OBJECT FROM RESULT SET
    RowMapper<TestNode> mapper = (resultSet, i) -> YourNode.fromResultSet(resultSet);

    //TABLE NAME
    String tableName = "nested_nodes";

    // QUERY USED FOR INSERTING NEW NODES
    String insertQuery = "insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(next value for SEQ,?,?,?,?,?,?)";

    // INSERT QUERY VALUES PROVIDER, CONVERTS NODE OBJECT INTO AN OBJECT ARRAY
    Function<TestNode, Object[]> insertValuesProvider = n -> new Object[]{n.getTreeLeft(), n.getTreeLevel(), n.getTreeRight(), n.getName(), n.getParentId(), n.getDiscriminator()};

    //CONFIGURATION CLASS
    JdbcNestedNodeRepositoryConfiguration<Long, TestNode> configuration = new JdbcNestedNodeRepositoryConfiguration<>(
            new JdbcTemplate(dataSource), tableName, mapper, insertQuery, insertValuesProvider, new YourJdbcTreeDiscriminator() //Discriminator is optional, allows to create multiple trees in one table
    );

    //CUSTOM COLUMN NAMES
    configuration.setIdColumnName("id");
    configuration.setParentIdColumnName("parent_id");
    configuration.setLeftColumnName("tree_left");
    configuration.setRighColumnName("tree_right");
    configuration.setLevelColumnName("tree_level");

    return JdbcNestedNodeRepositoryFactory.create(configuration);
    
```

The Node Class has to implement the ```NestedNode``` interface so that the logic can operage on Nested Node specific columns.

## General Usage

After creating schema You can use the special ```NestedNodeRepository``` to perform a tree-specific opeeration, such as:

```java

    void insertAsFirstChildOf(N node, N parent);
    
    void insertAsLastChildOf(N node, N parent);

    void insertAsNextSiblingOf(N node, N parent);

    void insertAsPrevSiblingOf(N node, N parent);

    void removeSingle(N node);

    void removeSubtree(N node);

    Iterable<N> getChildren(N node);

    Optional<N> getParent(N node);

    Optional<N> getPrevSibling(N node);

    Optional<N> getNextSibling(N node);

    Iterable<N> getParents(N node);

    Iterable<N> getTreeAsList(N node);

    Tree<ID, N> getTree(N node);

    void rebuildTree();

    void destroyTree();

    void insertAsFirstRoot(N node);

    void insertAsLastRoot(N node);
```

As You see, the ```NestedNode``` interface contains 5 specific columns:
- ID
- Left
- Right
- Level
- Parent ID

Strictly speaking NestedSet doesn't need the parent id column, but at the same time it's structure is so fragile, that this additional feature helps rebuild the tree if it becomes corrupted for some reason.

It is recommended that ```Left```, ```Right``` and ```Level``` be non nullable. This will ensure better stability of the Nested Set structure.

The ID of the interface is a parametrized type, which allows for using Long as well as UUID or any other ```Serializable``` class.

##### The ```NestedNode``` interface expects the implementing class to adhere to the JavaBean standard (field names should match getters and setters)

## Multiple Trees in one Table/Entity

You can have multiple independant trees in single Table/Entity. Just implement your own version of ```JpaTreeDiscriminator``` or ```JdbcTreeDiscriminator``` that will apply additional selectors on all Queries.

## Fixing / Initializing / Rebuilding the Tree

Nested Set is a pretty fragile structure. One bad manual modification of the table can destroy it. Also inserting big number of records manually would be very hard if you'd have to insert them with the correct treeLeft/treeRight/treeLevel values. Fortunately NestedJ can rebuild the Tree from scratch. Just use ```rebuild()``` method on the ```NestedNodeRepository<ID, N>```.

## Extending NestedJ

NestedJ comes with 2 implementations:
- JPA
- JDBC

If however you would need a custom one, or you want to enhance/customize one of the implementations, you can easily do this by implementing / overriding one or more ```*QueryDelegate``` classes that are responsible for communicating with the actual database. NestedJ is structured as a decoupled set of classes and you are free to experiment and adjust anything you want.

## Concurrency
NestedJ is not designed to handle concurrent tree modification. One single tree operation (insert/move/delete) can trigger an update on many tree nodes (which is the whole point of Nested Set), so it would be best if you ensure there are no concurrent updates going on. Having that said NestedJ has no Concurrency control mechanism built in. It is up to the user to decide whether any such logic is needed and how it should work. The simples one would be to lock entire table during a tree operation either via setting a proper transaction isolation or using any kind of Distributed Lock (for example from Spring Integration)

## BUGS

If You find any bugs, feel free to submit PR or create an issue on GitHub: https://github.com/eXsio/nestedj
