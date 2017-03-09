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

                       1 A 16
                        / \                    
                       /   \                   
                      /     \                 
     *             2 B 7   8 C 15              
                    /         \                
                   /\         /\               
                  /  \       /  \              
                 /    \     /    \             
                /   5 E 6  9 F 10 \            
             3 D 4             11 G 14
                                   \
                                    \
                                  12 H 13
                                  
You can query for an entire tree branch of node ```C``` using a query similar to this:

```
 SELECT * FROM TREE_TABLE WHERE LEFT >=8 AND RIGHT <= 15
```

Using the traditional parant_id relationship would mean firing multiple queries for each child / parent relationship.

### Usage

Here is the example entity annotated with NestedJ - specific Annotations:

```

@Entity
@Table(name = "nested_nodes")
public class TestNodeImpl extends DummyObject implements NestedNode<TestNodeImpl> {

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

I ommited the getters and setters for shortage. As You see, there are 4 specific columns:
- Left
- Right
- Level
- Parent

Strictly speaking NestedSet doesn't need the parent mapping, but at the same time it's structure is so fragile, that this additional feature helps rebuild the tree if it becomes corrupted for some reason.

After creating schema You can use the special ```NestedNodeDao``` to perform a tree-specific opeeration, such as:

- ```insertAsFirstChildOf(node, parent)```
- ```insertAsLastChildOf(node, parent)```
- ```insertAsNextSibling(node, parent)```
- ```insertAsPrevSibling(node, parent)```
- ```removeSingle(node)```
- ```removeSubtree(node)```
- ```getTreeAsList(node)```
- ```getTree(node)```

and couple more.

The Entity inheritance is permitted. NestedJ will begin searching for it's annotation at the top class and move down the inheritance tree.

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
            <version>2.1.0</version>
</dependency>


```

### Initialization

In order to use NestedJ, You have to configure it. Here's the full code:


    NestedNodeRepositoryImpl<TestNodeImpl> repository = new NestedNodeRepositoryImpl<>();
        TreeDiscriminatorImpl treeDiscriminator = new TreeDiscriminatorImpl();
        NestedNodeInserter<TestNodeImpl> inserter = new NestedNodeInserterImpl<>(entityManager, treeDiscriminator);
        NestedNodeMover<TestNodeImpl> mover = new NestedNodeMoverImpl<>(entityManager, treeDiscriminator);
        NestedNodeRetriever<TestNodeImpl> retriever = new NestedNodeRetrieverImpl<>(entityManager, treeDiscriminator);
        NestedNodeRebuilder<TestNodeImpl> rebuilder = new NestedNodeRebuilderImpl<>(entityManager, treeDiscriminator, inserter, retriever);
        NestedNodeRemover<TestNodeImpl> remover = new NestedNodeRemoverImpl<>(entityManager, treeDiscriminator);

        repository.setInserter(inserter);
        repository.setMover(mover);
        repository.setRebuilder(rebuilder);
        repository.setRetriever(retriever);
        repository.setRemover(remover);
 

NestedNodeRepository is a default, provided implementation of ```NestedNodeDao```. If You need or want, You can implement your own inserter/mover/retriever/remover/rebuilder that fits to Your needs.


### BUGS

If You find any bugs, feel free to submit PR or create an issue on GitHub: https://github.com/eXsio/nestedj
