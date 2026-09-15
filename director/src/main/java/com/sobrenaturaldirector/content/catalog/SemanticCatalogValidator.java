package com.sobrenaturaldirector.content.catalog;

public final class SemanticCatalogValidator { public void validate(SemanticCatalog catalog){if(catalog==null)throw new IllegalArgumentException("catalog unavailable");if(catalog.getEntries().size()>SemanticCatalog.MAX_ENTRIES||catalog.getProviders().size()>SemanticCatalog.MAX_PROVIDERS)throw new IllegalArgumentException("catalog bound exceeded");} }
