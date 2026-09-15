package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import com.sobrenaturaldirector.persistence.DirectorPersistenceMetadata;
import org.junit.Test;

public class FoundationMetadataTest {
    @Test public void persistenceSchemaIsVersioned() {
        assertEquals(1, DirectorPersistenceMetadata.CURRENT_SCHEMA_VERSION);
    }
}
