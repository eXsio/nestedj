package pl.exsio.nestedj.delegate.query.mem;

import com.google.common.collect.Maps;
import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.config.mem.discriminator.InMemoryTreeDiscriminator;
import pl.exsio.nestedj.config.mem.identity.InMemoryNestedNodeIdentityGenerator;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static pl.exsio.nestedj.model.NestedNode.*;

@SuppressWarnings("unchecked")
public abstract class InMemoryNestedNodeQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    private final InMemoryTreeDiscriminator<ID, N> treeDiscriminator;

    private final InMemoryNestedNodeIdentityGenerator<ID> identityGenerator;

    protected final Set<N> nodes;

    protected final static Map<String, InMemoryNestedNodeInsertingQueryDelegate.Setter> SETTERS = Maps.newHashMap();

    protected final static Map<String, InMemoryNestedNodeInsertingQueryDelegate.Getter> GETTERS = Maps.newHashMap();

    static {
        SETTERS.put(RIGHT, (node, value) -> node.setTreeRight((Long) value));
        SETTERS.put(LEFT, (node, value) -> node.setTreeLeft((Long) value));
        SETTERS.put(LEVEL, (node, value) -> node.setTreeLevel((Long) value));
        SETTERS.put(ID, (node, value) -> node.setId((Serializable) value));
        SETTERS.put(PARENT_ID, (node, value) -> node.setParentId((Serializable) value));

        GETTERS.put(RIGHT, NestedNode::getTreeRight);
        GETTERS.put(LEFT, NestedNode::getTreeLeft);
        GETTERS.put(LEVEL, NestedNode::getTreeLevel);
        GETTERS.put(ID, NestedNode::getId);
        GETTERS.put(PARENT_ID, NestedNode::getParentId);
    }

    public InMemoryNestedNodeQueryDelegate(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        this.treeDiscriminator = configuration.getTreeDiscriminator();
        this.identityGenerator = configuration.getIdentityGenerator();
        this.nodes = configuration.getNodes();
    }

    protected Stream<N> nodesStream() {
        return treeDiscriminator != null ? nodes.stream().filter(treeDiscriminator::applies) : nodes.stream();
    }

    protected ID generateIdentity() {
        return identityGenerator.generateIdentity();
    }


    protected interface Setter<ID extends Serializable, N extends NestedNode<ID>> {
        void set(N node, Object value);
    }

    protected interface Getter<ID extends Serializable, N extends NestedNode<ID>> {
        Object get(N node);
    }

    protected Long getLong(String fieldname, N node) {
        return (Long) GETTERS.get(fieldname).get(node);
    }

    protected Serializable getSerializable(String fieldname, N node) {
        return (Serializable) GETTERS.get(fieldname).get(node);
    }

    protected void setLong(String fieldname, N node, Long value) {
        SETTERS.get(fieldname).set(node, value);
    }

    protected void setSerializable(String fieldname, N node, Serializable value) {
        SETTERS.get(fieldname).set(node, value);
    }
}
