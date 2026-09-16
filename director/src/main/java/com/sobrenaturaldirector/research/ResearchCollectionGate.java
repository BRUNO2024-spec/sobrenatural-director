package com.sobrenaturaldirector.research;

/** Central consent predicate used before any research snapshot is enqueued. */
public interface ResearchCollectionGate { boolean canCollect(); }
