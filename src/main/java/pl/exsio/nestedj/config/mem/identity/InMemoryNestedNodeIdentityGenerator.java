package pl.exsio.nestedj.config.mem.identity;

import java.io.Serializable;

public interface InMemoryNestedNodeIdentityGenerator<ID extends Serializable> {

    ID generateIdentity();
}
