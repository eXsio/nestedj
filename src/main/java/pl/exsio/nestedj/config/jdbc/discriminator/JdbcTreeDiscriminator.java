package pl.exsio.nestedj.config.jdbc.discriminator;

import java.util.List;

/**
 * Tree Discriminator for use with JDBC Repository implementation.
 * Allows to store multiple intependant Trees in one Repository/Collection.
 */
public interface JdbcTreeDiscriminator {

    /**
     * Returns String with SQL query part that narrows the scope of all SQL Queries used by JDBC implementation
     *
     * @return - String with SQL query part that narrows the scope of all SQL Queries used by JDBC implementation.
     */
    String getQueryPart();

    /**
     * Returns List of values that should be set in the Prepared Statement and that correspond to the parameters used in the getQueryPart() method.
     *
     * @return - List of values that should be set in the Prepared Statement and that correspond to the parameters used in the getQueryPart() method.
     */
    List<Object> getParameters();
}
