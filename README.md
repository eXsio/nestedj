# NestedJ - Read-optimized sorted tree management for Java
[![Build Status](https://travis-ci.org/eXsio/nestedj.svg?branch=master)](https://travis-ci.org/eXsio/nestedj)
[![Coverity Status](https://scan.coverity.com/projects/8499/badge.svg?flat=1)](https://scan.coverity.com/projects/exsio-nestedj)
[![codecov](https://codecov.io/gh/eXsio/nestedj/branch/master/graph/badge.svg)](https://codecov.io/gh/eXsio/nestedj)
[![](https://jitpack.io/v/eXsio/nestedj.svg)](https://jitpack.io/#eXsio/nestedj)


## Overview
NestedJ is a Java library that provides **Spring Data type Repository** to manage read-optimized, **sorted** trees with the use of Nested Set Model.
**It provides an O(log2(n)) access to any tree traversal logic, including:**
- finding immediate children of any given node
- finding all children (regardless of depth) of any given node
- finding an immediate parent if any fiven node
- finding all parents of any given node from immediate to root

Like what you see? Let me know by leaving a Star! 

### What is Nested Set?

The [Nested Set Model](https://en.wikipedia.org/wiki/Nested_set_model) is a technique for representing nested sets (also known as trees or hierarchies) in relational databases.

### Practical Example

Let's say you have a e-commerce solution with a multi-level Product Catalog. One of the most basic requirements would be that 
if a Customer browses one of the Categories, he should be presented with Products from that Category and from its Subcategories (regardless of their depth in the hierarchy).

How can we do it? We could:
- Recursively traverse the Category and it's Subcategories, executing 1 SQL per Category and gatering all the Category IDs:

```sql


-- has an O(b^d) Time complexity where b == branching factor and d == depth of tree
-- due to the need of recursively repeating below query for each (sub)category in tree
select cat_id from categories where cat_parent_id = :parent_id

-- this solution causes N+1 select problem
select * from products where cat_id in (:gathered_cat_ids)

```

- use proprietary SQL features like Oracle's recursive queries:

```sql

-- has an O(b^d) complexity where b == branching factor and d == depth of tree
-- due to recursive logic hidden in database, but still present
select p.* 
from product p 
inner join categories c on p.cat_id = c.cat_id
connect by prior c.cat_id = c.cat_parent_id
where c.cat_parent_id = :parent_id
```

or Recursive Common Table Expressions in SQL Server: 

```sql

-- has an O(b^d) complexity where b == branching factor and d == depth of tree
-- due to recursive logic hidden in database, but still present
WITH Products as
(
  SELECT p.*
  FROM product p
  WHERE p.parent_id is null

  UNION ALL

  SELECT p1.*
  FROM product p1  
  INNER JOIN Products Ps
  ON Ps.parent_id = p1.parent_id
 )
SELECT * From Products

```

- use NestedJ:

```sql

-- has O(log2(n)) complexity (assuming that table is correctly indexed) 
-- or at worst O(n) for full table scan
select p.* 
from product p 
inner join categories c on p.cat_id = c.cat_id
where c.tree_left >= :parent_left and c.tree_right <= :parent_right

```

### Technical Example

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

```sql
 select * from tree_table where tree_left >= 8 and tree_right <= 17
```

You can query for a top treeLevel parent of a given (```H``` in this example) node using a query similar to this:

```sql
select * from tree_table where tree_left < 12 and tree_right > 13 and tree_level = 0
```

You can query for an entire path leading to a given (```D``` in this example) node using a query similar to this:

```sql
select * from tree_table where tree_left < 3 and tree_right > 4 order by tree_level asc
```

You can query for all leafs of a given parent/branch (```C``` in this example) node using a query similar to this:

```sql
select * from tree_table where tree_left > 8 and tree_right < 17 and tree_right - tree_left = 1 order by tree_level asc
```


Using the traditional ```parant_id``` relationship would require recursive execution of SQL query for each node in the tree.

##### Important: Nested Set is NOT a binary tree - the number of nodes on any treeLevel is unlimited. 

## Tradeoffs?

Of course there is no free lunch :) Nested Set Model offers unbeatable tree traversal performance at the cost of elevated (but reasonable - no recursion) complexity
of all tree modification operations. Any update of the tree structure triggers cascade update of left/right/level values in multiple nodes.
NestedJ should only be considered when the reads count highly outnumbers the modifications count. 

Going back to our e-commerce example: Product Catalog of a medium size store could be updated up to 100 - 500 times a day. 
At the same time the number of visitors could be 100k - 500k a day. Even with increased tree modification complexity we are still 
gaining a lot of performance. There is no recursiveness during the tree update. 

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
    <version>5.0.4</version>
</dependency>

```


## Advantages of NestedJ

   - **No Reflection!** - no custom annotations, no additional boostrap logic
   - **Best achievable performance** - all tree actions are performed using a minimal number of Bulk SQL Queries, without loading any parts of the tree structure to the memory and without node-by-node processing
   - **No JPA mappings enforced** - no ManyToOne/OneToMany mappings needed for the lib to work (if you're using JPA implementation)
   - **No ID/PK class enforced** - no hardwired requirements for any particular ID/Primary Key class
   - **Fully customizable** - you can repimplement the parts you need and leave the rest intact
   - **Fully tested** - integration tests created for all possible tree operations
   - **Minimal number of Project dependencies** - only JPA API or Spring JDBC - depending on which implementation you want to use 
   - **Multiple Implementations** - choose between JPA, JDBC or in-memory storage

## Storage implementations

 - [JPA](README-JPA.md) - uses Hibernate and Criteria Queries
 - [JDBC](README-JDBC.md) - uses Spring's JdbcTemplate
 - [In Memory](README-MEM.md) - uses java.util.Set and JDK8+ Streams  
 
 All of the implementations are interoperable - they use the same base abstractions. You can use the in-memory implementation to locally build, modify, traverse the tree 
 and once it's saved to the Database, you can pick up where you left off with JPA or JDBC implementation. 

## Multiple trees in one Table/Entity/Collection

You can have multiple independent trees in single Table/Entity/Collection. Just implement your own version of ```treeDiscriminator``` that will narrow all tree-related operations to your desired scope.

## Fixing / Initializing / Rebuilding the tree

Nested Set is a pretty fragile structure. One bad manual modification of the table can destroy it. Also inserting big number of records manually would be very hard if you'd have to insert them with the correct treeLeft/treeRight/treeLevel values. Fortunately NestedJ can rebuild the tree from scratch. Just use ```rebuild()``` method on the ```NestedNodeRepository<ID, N>```.

## Extending NestedJ

If you would need a custom implementation, or you want to enhance/customize one of the existing ones, you can easily do this by implementing / overriding one or more ```*QueryDelegate``` classes that are responsible for communicating with the actual database. NestedJ is structured as a decoupled set of classes and you are free to experiment and adjust anything you want.

## Concurrency

NestedJ supports Locking the ```NestedNodeRepository``` during any tree modification using the ```NestedNodeRepository.Lock``` interface.
There are 2 implementations available out of the box:
- ```NoLock``` - no-op lock that doesn't lock anything
- ```InMemoryLock``` - in-memory lock that locks trees based on provided Lock Handle

If you require more sophisticated locking (for example extarnal, distributed lock), feel free to implement and use your own ```NestedNodeRepository.Lock```

## Support

Although this is a project I'm working on in my spare time, I try to fix any issues as soon as I can. If you nave a feature request that could prove useful I will also consider adding it in the shortest possible time.

## BUGS

If You find any bugs, feel free to submit PR or create an issue on GitHub: https://github.com/eXsio/nestedj
