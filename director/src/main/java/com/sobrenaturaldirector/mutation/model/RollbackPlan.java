package com.sobrenaturaldirector.mutation.model;
import java.util.*;
public final class RollbackPlan {private final boolean snapshotRequired;private final List<String> actions;public RollbackPlan(boolean snapshotRequired,Collection<String> actions){this.snapshotRequired=snapshotRequired;this.actions=Collections.unmodifiableList(new ArrayList<String>(actions==null?Collections.<String>emptyList():actions));}public boolean isSnapshotRequired(){return snapshotRequired;}public List<String> getActions(){return actions;}}
