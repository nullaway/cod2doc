package ru.nullaway.cod2doc.panel;

import ru.nullaway.cod2doc.model.ActionModel;
import ru.nullaway.cod2doc.model.ConditionModel;
import ru.nullaway.cod2doc.model.ParameterModel;
import ru.nullaway.cod2doc.model.ScenarioModel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.net.URL;

public class ScenarioPanel extends JPanel {
    private JTree tree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;

    public ScenarioPanel(String title) {
        setLayout(new BorderLayout());
        root = new DefaultMutableTreeNode("Scenarios");
        treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.setCellRenderer(new ScenarioTreeCellRenderer());
        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);

        if (title != null && !title.isEmpty()) {
            setBorder(BorderFactory.createTitledBorder(title));
        }
    }

    public void displayScenario(ScenarioModel scenarioModel) {
        if (scenarioModel != null) {
            DefaultMutableTreeNode scenarioNode = new DefaultMutableTreeNode(scenarioModel);

            // Add parameters
            if (!scenarioModel.getParameters().isEmpty()) {
                DefaultMutableTreeNode paramsNode = new DefaultMutableTreeNode("Parameters");
                for (ParameterModel parameter : scenarioModel.getParameters()) {
                    paramsNode.add(new DefaultMutableTreeNode(parameter));
                }
                scenarioNode.add(paramsNode);
            }

            // Add actions
            for (ActionModel action : scenarioModel.getActions()) {
                scenarioNode.add(createActionNode(action));
            }

            root.add(scenarioNode);
            treeModel.reload();
        } else {
            root.removeAllChildren();
            treeModel.reload();
            JOptionPane.showMessageDialog(this, "No scenario to display.");
        }
    }

    private DefaultMutableTreeNode createActionNode(ActionModel action) {
        DefaultMutableTreeNode actionNode = new DefaultMutableTreeNode(action);

        if (action instanceof ConditionModel) {
            ConditionModel condition = (ConditionModel) action;

            DefaultMutableTreeNode thenNode = new DefaultMutableTreeNode("Then");
            for (ActionModel thenAction : condition.getThenActions()) {
                thenNode.add(createActionNode(thenAction));
            }

            DefaultMutableTreeNode elseNode = new DefaultMutableTreeNode("Else");
            for (ActionModel elseAction : condition.getElseActions()) {
                elseNode.add(createActionNode(elseAction));
            }

            actionNode.add(thenNode);
            actionNode.add(elseNode);
        } else {
            for (ActionModel nestedAction : action.getNestedActions()) {
                actionNode.add(createActionNode(nestedAction));
            }
        }

        return actionNode;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Scenario Display");
        ScenarioPanel scenarioPanel = new ScenarioPanel("Scenario Panel");

        // Example ScenarioModel
        ScenarioModel exampleScenario = new ScenarioModel("Scenario name");
        ParameterModel param1 = new ParameterModel();
        param1.setName("vlad");
        param1.setDescription("dalv");
        ParameterModel param2 = new ParameterModel();
        param2.setName("king");
        param2.setDescription("");
        exampleScenario.getParameters().add(param1);
        exampleScenario.getParameters().add(param2);

        ActionModel action1 = new ActionModel("Port action");
        exampleScenario.addAction(action1);

        ConditionModel condition1 = new ConditionModel("Condition: Field is null");
        ActionModel thenAction1 = new ActionModel("DOMAIN SERVICE: Another domain action");
        ConditionModel nestedCondition = new ConditionModel("Condition: Field is null");
        ActionModel nestedThenAction = new ActionModel("DOMAIN SERVICE: Another domain action");
        ActionModel nestedElseAction = new ActionModel("DOMAIN SERVICE: Domain action");
        nestedCondition.addThenAction(nestedThenAction);
        nestedCondition.addElseAction(nestedElseAction);
        condition1.addThenAction(thenAction1);
        condition1.addThenAction(nestedCondition);
        ActionModel elseAction1 = new ActionModel("DOMAIN SERVICE: Domain action");
        condition1.addElseAction(elseAction1);

        exampleScenario.addAction(condition1);
        exampleScenario.addAction(action1);

        scenarioPanel.displayScenario(exampleScenario);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(scenarioPanel);
        frame.setSize(600, 400);
        frame.setVisible(true);
    }

    private static class ScenarioTreeCellRenderer extends DefaultTreeCellRenderer {
        private Icon scenarioIcon;
        private Icon actionIcon;
        private Icon conditionIcon;
        private Icon parameterIcon;

        public ScenarioTreeCellRenderer() {
            URL scenarioUrl = getClass().getResource("/icons/scenario.png");
            URL actionUrl = getClass().getResource("/icons/action.png");
            URL conditionUrl = getClass().getResource("/icons/condition.png");
            URL parameterUrl = getClass().getResource("/icons/parameter.png");

            if (scenarioUrl != null) {
                scenarioIcon = new ImageIcon(scenarioUrl);
            }
            if (actionUrl != null) {
                actionIcon = new ImageIcon(actionUrl);
            }
            if (conditionUrl != null) {
                conditionIcon = new ImageIcon(conditionUrl);
            }
            if (parameterUrl != null) {
                parameterIcon = new ImageIcon(parameterUrl);
            }
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof ScenarioModel) {
                setIcon(scenarioIcon);
            } else if (userObject instanceof ConditionModel) {
                setIcon(conditionIcon);
            } else if (userObject instanceof ActionModel) {
                setIcon(actionIcon);
            } else if (userObject instanceof ParameterModel) {
                setIcon(parameterIcon);
            } else {
                setIcon(getDefaultLeafIcon());
            }

            return this;
        }
    }
}