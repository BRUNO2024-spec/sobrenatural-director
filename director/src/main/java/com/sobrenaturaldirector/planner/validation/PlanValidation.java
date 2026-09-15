package com.sobrenaturaldirector.planner.validation;
import java.util.*;import com.sobrenaturaldirector.planner.model.*;import com.sobrenaturaldirector.planner.requirement.PlanRequirement;
public final class PlanValidation {private final List<String> errors;public PlanValidation(List<String>e){errors=Collections.unmodifiableList(new ArrayList<String>(e));}public boolean isValid(){return errors.isEmpty();}public List<String>getErrors(){return errors;}}
