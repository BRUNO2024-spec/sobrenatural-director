package com.sobrenaturaldirector;

import org.junit.Test;
import static org.junit.Assert.*;
import com.sobrenaturaldirector.composition.ExternalBlockPolicy;
import com.sobrenaturaldirector.composition.model.ExternalBlockReference;

/** Architecture test: a second provider policy is a test-only object, not core code. */
public class DirectorGeneralizationTest {
    @Test public void genericBoundaryAcceptsAProviderSuppliedPolicy() {
        ExternalBlockPolicy fake = new ExternalBlockPolicy() {
            public boolean isProviderAvailable() { return true; }
            public boolean accepts(String provider, String version, ExternalBlockReference reference) {
                return "fake-provider".equals(provider) && "1.0".equals(version)
                        && reference != null && "fake-block".equals(reference.getRegistryName());
            }
        };
        assertTrue(fake.isProviderAvailable());
        assertTrue(fake.accepts("fake-provider", "1.0", new ExternalBlockReference("fake-provider", "fake-block", 0)));
        assertFalse(fake.accepts("unknown-provider", "1.0", new ExternalBlockReference("unknown-provider", "fake-block", 0)));
    }
}
