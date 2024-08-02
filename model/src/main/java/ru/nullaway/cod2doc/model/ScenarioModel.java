package ru.nullaway.cod2doc.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScenarioModel {
    private String name;
    private List<ActionModel> actions;
    private Set<ParameterModel> parameters = new HashSet<>();

    public ScenarioModel(String name) {
        this.name = name;
        this.actions = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<ActionModel> getActions() {
        return actions;
    }

    public void addAction(ActionModel action) {
        actions.add(action);
    }

    public Set<ParameterModel> getParameters() {
        return parameters;
    }

    public void setParameters(Set<ParameterModel> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Scenario: ").append(name).append("\n");

        if (!parameters.isEmpty()) {
            sb.append("  Parameters:\n");
            for (ParameterModel parameter : parameters) {
                sb.append("    ").append(parameter.getName()).append(": ").append(parameter.getDescription()).append("\n");
            }
        }

        for (ActionModel action : actions) {
            sb.append(action.toString(1));
        }
        return sb.toString();
    }
}
