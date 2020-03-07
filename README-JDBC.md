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
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
    <version>5.2.1.RELEASE</version>
</dependency>

```

## JDBC Usage

In order to use NestedJ, You have to configure it. In 9 our of 10 cases you will want to use the preconfigured builder methods available in the ```JdbcNestedNodeRepositoryFactory``` class:

```java

    //ROW MAPPER FOR CREATING INSTANCES OF THE NODE OBJECT FROM RESULT SET
    RowMapper<TestNode> mapper = (resultSet, i) -> YourNode.fromResultSet(resultSet);

    //TABLE NAME
    String tableName = "nested_nodes";

    // QUERY USED FOR INSERTING NEW NODES
    String insertQuery = "insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(next value for SEQ,?,?,?,?,?,?)";

    // INSERT QUERY VALUES PROVIDER, CONVERTS NODE OBJECT INTO AN OBJECT ARRAY
    Function<TestNode, Object[]> insertValuesProvider = n -> new Object[]{n.getTreeLeft(), n.getTreeLevel(), n.getTreeRight(), n.getName(), n.getParentId(), n.getDiscriminator()};
   
     // METHOD OF RETRIEVING GENERATED DATABASE PRIMARY KEYS
     Function<JdbcKeyHolder, Long> generatedKeyResolver = jdbcKeyHolder -> jdbcKeyHolder.getKeyValueAs(Long.class);
    
    //CONFIGURATION CLASS
    JdbcNestedNodeRepositoryConfiguration<Long, TestNode> configuration = new JdbcNestedNodeRepositoryConfiguration<>(
            new JdbcTemplate(dataSource), tableName, mapper, insertQuery, insertValuesProvider, generatedKeyResolver, new YourJdbcTreeDiscriminator()
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
