package com.sobrenaturaldirector.composition.model;

import java.util.*;
import com.sobrenaturaldirector.domain.StableId;
import com.sobrenaturaldirector.planner.model.DirectorPlan;
import com.sobrenaturaldirector.content.query.ResolutionResult;

public final class CompositionRequest {private final DirectorPlan plan;private final String theme;private final StructureScale scale;private final long seed;private final Map<String,ResolutionResult> resolutions;public CompositionRequest(DirectorPlan plan,String theme,StructureScale scale,long seed,Map<String,ResolutionResult> resolutions){this.plan=plan;this.theme=StableId.require(theme==null?"generic":theme,"theme");this.scale=scale==null?StructureScale.SMALL:scale;this.seed=seed;this.resolutions=Collections.unmodifiableMap(new TreeMap<String,ResolutionResult>(resolutions==null?Collections.<String,ResolutionResult>emptyMap():resolutions));}public DirectorPlan getPlan(){return plan;}public String getTheme(){return theme;}public StructureScale getScale(){return scale;}public long getSeed(){return seed;}public Map<String,ResolutionResult> getResolutions(){return resolutions;}}
