package com.sobrenaturaldirector.action;

import java.util.*;
import com.sobrenaturaldirector.control.DimensionRef;

public final class SemanticActionIntent {
    private final ActionType type; private final String source; private final DimensionRef dimension; private final List<String> roles;
    public SemanticActionIntent(ActionType type,String source,DimensionRef dimension,Collection<String> roles){if(type==null||source==null||dimension==null)throw new IllegalArgumentException("invalid action intent");this.type=type;this.source=source;this.dimension=dimension;TreeSet<String>s=new TreeSet<String>();if(roles!=null)s.addAll(roles);this.roles=Collections.unmodifiableList(new ArrayList<String>(s));}
    public ActionType getType(){return type;}public String getSource(){return source;}public DimensionRef getDimension(){return dimension;}public List<String> getRoles(){return roles;}
}
