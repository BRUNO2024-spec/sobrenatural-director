package com.sobrenaturaldirector.derivation;
import java.util.*;
public final class ObservationDelta { private final List<PlayerDelta> players; public ObservationDelta(List<PlayerDelta> players){this.players=Collections.unmodifiableList(new ArrayList<PlayerDelta>(players));} public List<PlayerDelta> getPlayers(){return players;} }
