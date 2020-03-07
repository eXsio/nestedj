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

package pl.exsio.nestedj.delegate.query.jdbc;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

public class JdbcKeyHolder extends GeneratedKeyHolder implements Serializable {

    @SuppressWarnings("unchecked")
    public <T> T getKeyValueAs(Class<T> keyClass) {
        if (this.getKeyList().isEmpty()) {
            return null;
        } else if (this.getKeyList().size() <= 1 && this.getKeyList().get(0).size() <= 1) {
            Iterator<Object> keyIter = ((Map)this.getKeyList().get(0)).values().iterator();
            if (keyIter.hasNext()) {
                Object key = keyIter.next();
                if (!(keyClass.isAssignableFrom(key.getClass()))) {
                    throw new DataRetrievalFailureException("The generated key is not of a supported type. Unable to cast [" + key.getClass().getName() + "] to [" + keyClass.getName() + "]");
                } else {
                    return keyClass.cast(key);
                }
            } else {
                throw new DataRetrievalFailureException("Unable to retrieve the generated key. Check that the table has an identity column enabled.");
            }
        } else {
            throw new InvalidDataAccessApiUsageException("The getKey method should only be used when a single key is returned.  The current key entry contains multiple keys: " + this.getKeyList());
        }
    }
}
