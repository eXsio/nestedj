/*
 *  The MIT License
 *
 *  Copyright (c) 2019 eXsio.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 *  BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

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
