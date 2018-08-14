# NestedJ - a JPA Nested Set implementation
[![Build Status](https://travis-ci.org/eXsio/nestedj.svg?branch=master)](https://travis-ci.org/eXsio/nestedj)
[![Coverity Status](https://scan.coverity.com/projects/8499/badge.svg?flat=1)](https://scan.coverity.com/projects/exsio-nestedj)
[![codecov](https://codecov.io/gh/eXsio/nestedj/branch/master/graph/badge.svg)](https://codecov.io/gh/eXsio/nestedj)

NestedJ is a Java library that implements the Nested Set pattern with the help of JPA.

### Overview
NestedJ can automate the insertion/moving/removing of tree nodes. It can also retrieve persisted Nodes as a flat or recursive Java structures / collections.

### What is Nested Set?

Nested Set is a RDBMS Tree implmentation. It allows to query for whole tree branches and finding ancestors and descendants using one simple query. As usual, there is no freee lunch, so the price to pay in this case is a slightly more complex logic for modifying the tree (inserting / moving / removing nodes and branches). Fortunately NestedJ makes it as easy as using a standard JPA EntityManager.

### Example

Given the below structure:

                       1 A 18
                        / \                    
                       /   \                   
                      /     \                 
     *             2 B 7   8 C 17              
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

You can query for a top level parent of a given (```H``` in this example) node using a query similar to this:

```
SELECT * FROM TREE_TABLE WHERE LEFT < 12 AND RIGHT > 13 AND LEVEL = 0
```

You can also query for an entire path leading to a given (```D``` in this example) node using a query similar to this:

```
SELECT * FROM TREE_TABLE WHERE LEFT < 3 AND RIGHT > 4 ORDER BY LEVEL ASC
```


Using the traditional ```parant_id``` relationship would mean firing multiple queries for each child / parent relationship.

##### Important: Nested Set is NOT a binary tree - the number of nodes on any level is unlimited. 

### Installation

```

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.eXsio</groupId>
    <artifactId>nestedj</artifactId>
    <version>2.2.0</version>
</dependency>

```

### First Steps

In order to use NestedJ, You have to configure it. In 9 our of 10 cases you will want to use the preconfigured builder methods available in the ```NestedNodeRepository``` interface:
 
 
 Here's the full code:


    NestedNodeRepository.createDefault(entityManager)
    NestedNodeRepository.createDiscriminated(entityManager, new CustomTreeDiscriminator());
 

```DelegatingNestedNodeRepository``` is a default, provided implementation of ```NestedNodeRepository```. If You need or want, You can implement your own inserter/mover/retriever/remover/rebuilder that fit your needs and configure the whole thing by hand:

```
    CustomNestedNodeInserter<ID, N> inserter = new CustomNestedNodeInserter<>(entityManager, discriminator);
    CustomNestedNodeRetriever<ID, N> retriever = new CustomNestedNodeRetriever<>(entityManager, discriminator);
    return new DelegatingNestedNodeRepository<>(inserter,
            new CustomNestedNodeMover<>(entityManager, discriminator),
            new CustomNestedNodeRemover<>(entityManager, discriminator),
            retriever,
            new CustomNestedNodeRebuilder<>(entityManager, discriminator, inserter, retriever)
    );

```

### Entiy mapping

Here is the example entity annotated with NestedJ - specific Annotations:

```

@Entity
@Table(name = "nested_nodes")
public class TestNodeImpl extends DummyObject implements NestedNode<Long, TestNodeImpl> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Column(name = "node_name", nullable = false)
    protected String name;

    @LeftColumn
    @Column(name = "tree_left", nullable = false)
    protected Long lft;

    @RightColumn
    @Column(name = "tree_right", nullable = false)
    protected Long rgt;

    @LevelColumn
    @Column(name = "tree_level", nullable = false)
    protected Long lvl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id", nullable = true)
    @ParentColumn
    protected TestNodeImpl parent;

    @Column(name = "discriminator", nullable = false)
    protected String discriminator;
}
```

I have ommited the getters and setters for shortage. As You see, there are 4 specific columns:
- Left
- Right
- Level
- Parent

Strictly speaking NestedSet doesn't need the parent mapping, but at the same time it's structure is so fragile, that this additional feature helps rebuild the tree if it becomes corrupted for some reason.

It is recommended that ```LeftColumn```, ```RightColumn``` and ```LevelColumn``` be non nullable. This will ensure better stability of the Nested Set structure.

The ID of the entity interface is a parametrized type, which allows for using Long as well as UUID or any other ```Serializable``` class.

### Usage

After creating schema You can use the special ```NestedNodeRepository``` to perform a tree-specific opeeration, such as:

```
    void insertAsFirstChildOf(N node, N parent);

    void insertAsLastChildOf(N node, N parent);

    void insertAsNextSiblingOf(N node, N parent);

    void insertAsPrevSiblingOf(N node, N parent);

    void removeSingle(N node);

    void removeSubtree(N node);

    Iterable<N> getChildren(N node);

    Optional<N> getParent(N node);

    Iterable<N> getParents(N node);

    Iterable<N> getTreeAsList(N node);

    Tree<ID, N> getTree(N node);

    void rebuildTree(Class<N> nodeClass);
    
    void destroyTree(Class<N> nodeClass);
```

The Entity inheritance is permitted. NestedJ will begin searching for it's annotation at the top class and move down the inheritance tree.

### Multiple Trees in one Table/Entity

You can have multiple independant trees in single Table/Entity. Just implement your own version of ```TreeDiscriminator``` that will apply additional selectors on all JPA Queries.

### Fixing / Initializing / Rebuilding the Tree

Nested Set is a pretty fragile structure. One bad manual modification of the table can destroy it. Also inserting big number of records manually would be very hard if you'd have to insert them with the correct left/right/level values. Fortunately NestedJ can rebuild the Tree from scratch. Just use ```rebuild(Class<N> nodeClass)``` method on the ```NestedNodeRepository<ID, N>```.


### BUGS

If You find any bugs, feel free to submit PR or create an issue on GitHub: https://github.com/eXsio/nestedj
