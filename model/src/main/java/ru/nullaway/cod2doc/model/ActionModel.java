package ru.nullaway.cod2doc.model;

import java.util.ArrayList;
import java.util.List;

public  class ActionModel {
    private String description;
    private List<ActionModel> nestedActions;

    public ActionModel(String description) {
        this.description = description;
        this.nestedActions = new ArrayList<>();
    }

    public String getDescription() {
        return description;
    }

    public List<ActionModel> getNestedActions() {
        return nestedActions;
    }

    public void addNestedAction(ActionModel action) {
        nestedActions.add(action);
    }

    @Override
    public String toString() {
        return toString(0);
    }

    protected String toString(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
        sb.append(description).append("\n");
        for (ActionModel nestedAction : nestedActions) {
            sb.append(nestedAction.toString(level + 1));
        }
        return sb.toString();
    }
}
