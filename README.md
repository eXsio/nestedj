# NestedJ
[![Build Status](https://travis-ci.org/eXsio/nestedj.svg?branch=master)](https://travis-ci.org/eXsio/nestedj)
[![Coverity Status](https://scan.coverity.com/projects/8499/badge.svg?flat=1)](https://scan.coverity.com/projects/exsio-nestedj)
[![codecov](https://codecov.io/gh/eXsio/nestedj/branch/master/graph/badge.svg)](https://codecov.io/gh/eXsio/nestedj)

NestedJ is a Java library that implements the NestedSet pattern with the help of JPA.

### Overview
NestedJ can automate the insertion/moving/removing of tree nodes. It can also retrieve persisted Nodes as a flat or recursive Java structures / collections.

### Usage

Here is the example entity annotated with NestedJ - specific Annotations:


    @Entity
    @Table(name = "nested_nodes")
    @Inheritance(strategy = InheritanceType.JOINED)
    public class TestNodeImpl implements NestedNode {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        protected Long id;

        @Column(name = "node_name", nullable = false)
        protected String name;

        @LeftColumn
        @Column(name = "tree_left", nullable = true)
        protected Long lft;

        @RightColumn
        @Column(name = "tree_right", nullable = true)
        protected Long rgt;
    
        @LevelColumn
        @Column(name = "tree_level", nullable = true)
        protected Long lvl;
 
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "parent_id", nullable = true)
        @ParentColumn
        protected TestNodeImpl parent;
    }


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

### Installation / Initialization

In order to use NestedJ, You have to configure it. Here's the full code:


    NestedNodeRepository repository = new NestedNodeRepository();
    repository.setInserter(new NestedNodeInserterImpl());
    repository.setMover(new NestedNodeMoverImpl());
    repository.setRebuilder(new NestedNodeRebuilderImpl(new NestedNodeInserterImpl()));
    repository.setRemover(new NestedNodeRemoverImpl());
    repository.setRetriever(new NestedNodeRetrieverImpl());
 

NestedNodeRepository is a default, provided implementation of ```NestedNodeDao```. If You need or want, You can implement your own inserter/mover/retriever/remover/rebuilder that fits to Your needs.


### BUGS

If You find any bugs, feel free to submit PR or create an issue on GitHub: https://github.com/eXsio/nestedj
