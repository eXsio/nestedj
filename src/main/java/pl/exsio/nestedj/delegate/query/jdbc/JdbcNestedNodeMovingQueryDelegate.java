package pl.exsio.nestedj.delegate.query.jdbc;

import com.google.common.base.Preconditions;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeMovingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.sql.Types;

public class JdbcNestedNodeMovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JdbcNestedNodeQueryDelegate<ID, N>
        implements NestedNodeMovingQueryDelegate<ID, N> {

    private final static Long MARKING_MODIFIER = 1000L;

    private enum Mode {
        UP, DOWN
    }

    public JdbcNestedNodeMovingQueryDelegate(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }

    @Override
    public Integer markNodeIds(NestedNodeInfo<ID> node) {
        return jdbcTemplate.update(
                getDiscriminatedQuery(
                        new Query("update :tableName set :right = (-:right - ?) where :left >= ? and :right <= ?")
                            .set("tableName", tableName)
                            .set("right", right)
                            .set("left", left)
                            .build()
                ),
                preparedStatement -> {
                    preparedStatement.setLong(1, MARKING_MODIFIER);
                    preparedStatement.setLong(2, node.getLeft());
                    preparedStatement.setLong(3, node.getRight());
                    setDiscriminatorParams(preparedStatement, 4);
                }
        );
    }

    @Override
    public void updateSideFieldsUp(Long delta, Long start, Long stop, String field) {
        updateFields(Mode.UP, delta, start, stop, field);
    }

    @Override
    public void updateSideFieldsDown(Long delta, Long start, Long stop, String field) {
        updateFields(Mode.DOWN, delta, start, stop, field);
    }

    @Override
    public void performMoveUp(Long nodeDelta, Long levelModificator) {
        performMove(Mode.UP, nodeDelta, levelModificator);
    }

    @Override
    public void performMoveDown(Long nodeDelta, Long levelModificator) {
        performMove(Mode.DOWN, nodeDelta, levelModificator);
    }

    @Override
    public void updateParentField(ID newParentId, NestedNodeInfo<ID> node) {
        Preconditions.checkNotNull(newParentId);
        doUpdateParentField(newParentId, node);
    }

    @Override
    public void clearParentField(NestedNodeInfo<ID> node) {
        doUpdateParentField(null, node);
    }

    private void updateFields(Mode mode, Long delta, Long start, Long stop, String field) {
        String columnName = treeColumnNames.get(field);
        String sign = Mode.UP.equals(mode) ? "+" : "-";
        jdbcTemplate.update(
                getDiscriminatedQuery(
                        new Query("update :tableName set :columnName = :columnName :sign ? where :columnName > ? and :columnName < ?")
                                .set("tableName", tableName)
                                .set("columnName", columnName)
                                .set("sign", sign)
                        .build()
                ),
                preparedStatement -> {
                    preparedStatement.setLong(1, delta);
                    preparedStatement.setLong(2, start);
                    preparedStatement.setLong(3, stop);
                    setDiscriminatorParams(preparedStatement, 4);
                }
        );
    }

    private void performMove(Mode mode, Long nodeDelta, Long levelModificator) {
        String sign = Mode.UP.equals(mode) ? "+" : "-";
        jdbcTemplate.update(
                getDiscriminatedQuery(
                        new Query("update :tableName set :level = (:level + ?), :right = (-(:right + ?) :sign ?), :left = :left :sign ? where :right < 0")
                                .set("tableName", tableName)
                                .set("level", level)
                                .set("left", left)
                                .set("right", right)
                                .set("sign", sign)
                                .build()
                ),
                preparedStatement -> {
                    preparedStatement.setLong(1, levelModificator);
                    preparedStatement.setLong(2, MARKING_MODIFIER);
                    preparedStatement.setLong(3, nodeDelta);
                    preparedStatement.setLong(4, nodeDelta);
                    setDiscriminatorParams(preparedStatement, 5);
                }
        );
    }

    private void doUpdateParentField(ID newParentId, NestedNodeInfo<ID> node) {
        jdbcTemplate.update(
                getDiscriminatedQuery(
                        new Query("update :tableName set :parentId = ? where :id = ?")
                                .set("tableName", tableName)
                                .set("parentId", parentId)
                                .set("id", id)
                                .build()
                ),
                preparedStatement -> {
                    if(newParentId == null) {
                        preparedStatement.setNull(1, Types.OTHER);
                    } else {
                        preparedStatement.setObject(1, newParentId);
                    }
                    preparedStatement.setObject(2, node.getId());
                    setDiscriminatorParams(preparedStatement, 3);
                }
        );
    }
}
