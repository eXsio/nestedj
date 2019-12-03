package pl.exsio.nestedj.config.mem.discriminator;

import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

/**
 * Tree Discriminator for use with InMemory Repository implementation.
 * Allows to store multiple intependant Trees in one Repository/Collection.
 *
 * @param <ID> - Nested Node Identier Class
 * @param <N> - Nested Node Class
 */
public interface InMemoryTreeDiscriminator<ID extends Serializable, N extends NestedNode<ID>> {

    /**
     * Method that decides whether a target Node belongs to the Tree or not.
     *
     * @param node - target Node
     * @return true if Node belongs to the Tree, false if Node does not belong to the Tree
     */
    boolean applies(N node);
}
