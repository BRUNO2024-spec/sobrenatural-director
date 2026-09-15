package com.sobrenaturaldirector.observation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ObservationFrame {
    public static final int MAX_WORLDS = 64;
    public static final int MAX_PLAYERS = 256;
    private final String captureId;
    private final long serverTick, captureSequence;
    private final List<WorldSnapshot> worlds;
    private final List<PlayerSnapshot> players;
    private final ObservationDiagnostics diagnostics;

    public ObservationFrame(String captureId, long serverTick, long captureSequence, List<WorldSnapshot> worlds, List<PlayerSnapshot> players, ObservationDiagnostics diagnostics) {
        if (captureId == null || captureId.length() == 0 || captureId.length() > 128 || serverTick < 0 || captureSequence < 0 || worlds == null || players == null || worlds.size() > MAX_WORLDS || players.size() > MAX_PLAYERS || diagnostics == null) throw new IllegalArgumentException("invalid observation frame");
        this.captureId=captureId;this.serverTick=serverTick;this.captureSequence=captureSequence;
        List<WorldSnapshot> worldCopy=new ArrayList<WorldSnapshot>(worlds);Collections.sort(worldCopy);this.worlds=Collections.unmodifiableList(worldCopy);
        List<PlayerSnapshot> playerCopy=new ArrayList<PlayerSnapshot>(players);Collections.sort(playerCopy);this.players=Collections.unmodifiableList(playerCopy);this.diagnostics=diagnostics;
    }
    public String getCaptureId(){return captureId;} public long getServerTick(){return serverTick;} public long getCaptureSequence(){return captureSequence;} public List<WorldSnapshot> getWorlds(){return worlds;} public List<PlayerSnapshot> getPlayers(){return players;} public ObservationDiagnostics getDiagnostics(){return diagnostics;}
    @Override public boolean equals(Object o){if(!(o instanceof ObservationFrame))return false;ObservationFrame x=(ObservationFrame)o;return captureId.equals(x.captureId)&&serverTick==x.serverTick&&captureSequence==x.captureSequence&&worlds.equals(x.worlds)&&players.equals(x.players)&&diagnostics.equals(x.diagnostics);}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{captureId,serverTick,captureSequence,worlds,players,diagnostics});}
}
