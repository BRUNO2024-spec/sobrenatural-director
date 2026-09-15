package com.sobrenaturaldirector.composition.model;

import java.util.*;
import com.sobrenaturaldirector.domain.StableId;

public final class StructureBlueprint {
    private final String id,theme; private final StructureScale scale; private final List<StructureNode> nodes; private final List<StructureConnector> connectors; private final List<StructureAnchor> anchors; private final List<String> criticalPath;
    public StructureBlueprint(String id,String theme,StructureScale scale,Collection<StructureNode> nodes,Collection<StructureConnector> connectors,Collection<StructureAnchor> anchors,List<String> criticalPath){this.id=StableId.require(id,"structureId");this.theme=StableId.require(theme,"theme");if(scale==null)throw new IllegalArgumentException("scale");this.scale=scale;this.nodes=immutable(nodes);this.connectors=immutable(connectors);this.anchors=immutable(anchors);this.criticalPath=Collections.unmodifiableList(new ArrayList<String>(criticalPath==null?Collections.<String>emptyList():criticalPath));}
    private static <T> List<T> immutable(Collection<T> x){return Collections.unmodifiableList(new ArrayList<T>(x==null?Collections.<T>emptyList():x));}
    public String getId(){return id;}public String getTheme(){return theme;}public StructureScale getScale(){return scale;}public List<StructureNode> getNodes(){return nodes;}public List<StructureConnector> getConnectors(){return connectors;}public List<StructureAnchor> getAnchors(){return anchors;}public List<String> getCriticalPath(){return criticalPath;}
}
