/* 
 * The MIT License
 *
 * Copyright 2014 exsio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pl.exsio.nestedj.config;

/**
 *
 * @author exsio
 */
public class NestedNodeConfigImpl implements NestedNodeConfig {

    private String entityName;

    private String rightFieldName;

    private String leftFieldName;

    private String levelFieldName;

    private String parentFieldName;

    private String idFieldName;

    @Override
    public String getEntityName() {
        return entityName;
    }

    @Override
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public String getRightFieldName() {
        return rightFieldName;
    }

    @Override
    public void setRightFieldName(String rightFieldName) {
        this.rightFieldName = rightFieldName;
    }

    @Override
    public String getLeftFieldName() {
        return leftFieldName;
    }

    @Override
    public void setLeftFieldName(String leftFieldName) {
        this.leftFieldName = leftFieldName;
    }

    @Override
    public String getLevelFieldName() {
        return levelFieldName;
    }

    @Override
    public void setLevelFieldName(String levelFieldName) {
        this.levelFieldName = levelFieldName;
    }

    @Override
    public String getParentFieldName() {
        return parentFieldName;
    }

    @Override
    public void setParentFieldName(String parentFieldName) {
        this.parentFieldName = parentFieldName;
    }

    public String getIdFieldName() {
        return idFieldName;
    }

    public void setIdFieldName(String idFieldName) {
        this.idFieldName = idFieldName;
    }

    @Override
    public String toString() {
        return "[leftFieldName: " + this.leftFieldName + ", rightFieldName:" + this.rightFieldName
                + ", levelFieldName: " + this.levelFieldName + ", parentFieldName:" + this.parentFieldName + "]";
    }

}
