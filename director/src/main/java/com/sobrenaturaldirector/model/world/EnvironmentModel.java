package com.sobrenaturaldirector.model.world;
import com.sobrenaturaldirector.derivation.*;
public final class EnvironmentModel { private final DerivedSignal darkLikely,exposedToSky,undergroundLikely; public EnvironmentModel(DerivedSignal dark,DerivedSignal exposed,DerivedSignal underground){darkLikely=dark;exposedToSky=exposed;undergroundLikely=underground;} public DerivedSignal getDarkLikely(){return darkLikely;} public DerivedSignal getExposedToSky(){return exposedToSky;} public DerivedSignal getUndergroundLikely(){return undergroundLikely;} }
