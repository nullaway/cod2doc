package ru.nullaway.cod2doc.model;

import java.util.ArrayList;
import java.util.List;

public class ConditionModel extends ActionModel {

    private List<ActionModel> thenActions;
    private List<ActionModel> elseActions;

    public ConditionModel(String description) {
        super(description);
        this.thenActions = new ArrayList<>();
        this.elseActions = new ArrayList<>();
    }

    public List<ActionModel> getThenActions() {
        return thenActions;
    }

    public void addThenAction(ActionModel action) {
        this.thenActions.add(action);
    }

    public List<ActionModel> getElseActions() {
        return elseActions;
    }

    public void addElseAction(ActionModel action) {
        this.elseActions.add(action);
    }

    @Override
    protected String toString(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
        sb.append("Condition: ").append(getDescription()).append("\n");

        for (int i = 0; i < level + 1; i++) {
            sb.append("  ");
        }
        sb.append("+ Then:\n");
        for (ActionModel thenAction : thenActions) {
            sb.append(thenAction.toString(level + 2));
        }

        for (int i = 0; i < level + 1; i++) {
            sb.append("  ");
        }
        sb.append("+ Else:\n");
        for (ActionModel elseAction : elseActions) {
            sb.append(elseAction.toString(level + 2));
        }

        return sb.toString();
    }


}
