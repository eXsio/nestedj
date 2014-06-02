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

    @Override
    public String toString() {
        return "[leftFieldName: " + this.leftFieldName + ", rightFieldName:" + this.rightFieldName
                + ", levelFieldName: " + this.levelFieldName + ", parentFieldName:" + this.parentFieldName + "]";
    }

}
