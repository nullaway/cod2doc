package ru.nullaway.cod2doc;

import ru.nullaway.cod2doc.api.annotation.*;
import ru.nullaway.cod2doc.model.ActionModel;
import ru.nullaway.cod2doc.model.ConditionModel;
import ru.nullaway.cod2doc.model.ParameterModel;
import ru.nullaway.cod2doc.model.ScenarioModel;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class ScenarioParser {

    public static List<ScenarioModel> parse(String path){
        Launcher launcher = new Launcher();
        launcher.addInputResource(path);
        launcher.buildModel();

        CtModel model = launcher.getModel();

        Map<String, String> actionServices = new HashMap<>();
        Map<String, String> conditionDescriptions = new HashMap<>();
        List<ScenarioModel> scenarios = new ArrayList<>();

        for (CtType<?> ctType : model.getAllTypes()) {
            String serviceName = "";
            if (ctType instanceof CtClass && ctType.hasAnnotation(Service.class)) {
                Service service = ctType.getAnnotation(Service.class);
                serviceName = service.value();
            }

            for (CtMethod<?> method : ctType.getMethods()) {
                if (method.hasAnnotation(Action.class)) {
                    Action action = method.getAnnotation(Action.class);
                    String actionName = action.value();
                    String fullActionName = serviceName.isEmpty() ? actionName : serviceName + ": " + actionName;
                    actionServices.put(method.getSignature(), fullActionName);
                }

                if (method.hasAnnotation(Condition.class)) {
                    Condition condition = method.getAnnotation(Condition.class);
                    String conditionDescription = condition.value();
                    conditionDescriptions.put(method.getSignature(), conditionDescription);
                }
            }
        }

        for (CtType<?> ctType : model.getAllTypes()) {
            for (CtMethod<?> method : ctType.getMethods()) {
                if (method.hasAnnotation(Scenario.class)) {
                    Scenario scenario = method.getAnnotation(Scenario.class);
                    String scenarioName = scenario.value();
                    ScenarioModel scenarioModel = new ScenarioModel(scenarioName);
                    scenarios.add(scenarioModel);

                    // Обработка параметров
                    processParameters(method, scenarioModel);

                    List<CtStatement> statements = method.getBody().getStatements();
                    processStatements(statements, scenarioModel, actionServices, conditionDescriptions);
                }
            }
            // Обработка полей модели классов для ScenarioParameter
            for (CtField<?> field : ctType.getFields()) {
                if (field.hasAnnotation(ScenarioParameter.class)) {
                    ScenarioParameter scenarioParameter = field.getAnnotation(ScenarioParameter.class);
                    ParameterModel parameterModel = new ParameterModel();
                    parameterModel.setName(scenarioParameter.value());
                    parameterModel.setDescription(scenarioParameter.description());

                    // Предполагается, что параметр относится к последнему сценарию в списке
                    if (!scenarios.isEmpty()) {
                        scenarios.get(scenarios.size() - 1).getParameters().add(parameterModel);
                    }
                }
            }
        }

        return scenarios;
    }

    public static void main(String[] args) {
        List<ScenarioModel> scenarios = parse("F:\\projects\\petProject\\cod2doc\\parser\\src");
        for (ScenarioModel scenario : scenarios) {
            System.out.println(scenario);
        }
    }

    private static void processParameters(CtMethod<?> method, ScenarioModel scenarioModel) {
        for (CtParameter<?> parameter : method.getParameters()) {
            // Обработка аннотации ScenarioParameter на параметрах метода
            if (parameter.hasAnnotation(ScenarioParameter.class)) {
                ScenarioParameter annotation = parameter.getAnnotation(ScenarioParameter.class);
                addParameterToModel(annotation, scenarioModel);
            }

            // Обработка аннотации ScenarioParameter на полях объектов, передаваемых как параметры метода
            CtType<?> parameterType = parameter.getType().getTypeDeclaration();
            if (parameterType != null) {
                for (CtField<?> field : parameterType.getFields()) {
                    if (field.hasAnnotation(ScenarioParameter.class)) {
                        ScenarioParameter annotation = field.getAnnotation(ScenarioParameter.class);
                        addParameterToModel(annotation, scenarioModel);
                    }
                }
            }
        }
    }

    private static void addParameterToModel(ScenarioParameter parameter, ScenarioModel scenarioModel) {
        ScenarioParameter scenarioParameter = parameter;
        ParameterModel parameterModel = new ParameterModel();
        parameterModel.setName(scenarioParameter.value());
        parameterModel.setDescription(scenarioParameter.description());

        scenarioModel.getParameters().add(parameterModel);
    }


    private static void processStatements(List<CtStatement> statements, ScenarioModel scenarioModel, Map<String, String> actionServices, Map<String, String> conditionDescriptions) {
        for (CtStatement statement : statements) {
            if (statement instanceof CtInvocation<?>) {
                CtInvocation<?> invocation = (CtInvocation<?>) statement;
                CtExecutable<?> invokedMethod = invocation.getExecutable().getDeclaration();
                if (invokedMethod != null && invokedMethod.hasAnnotation(Action.class)) {
                    String actionSignature = invokedMethod.getSignature();
                    String fullActionName = actionServices.get(actionSignature);
                    if (fullActionName != null) {
                        scenarioModel.addAction(new ActionModel(fullActionName));
                    }
                }
            }

            if (statement instanceof CtIf) {
                CtIf ifStatement = (CtIf) statement;
                List<CtInvocation<?>> conditionInvocations = ifStatement.getCondition().getElements(new TypeFilter<>(CtInvocation.class));
                for (CtInvocation<?> conditionInvocation : conditionInvocations) {
                    CtExecutable<?> invokedMethod = conditionInvocation.getExecutable().getDeclaration();
                    if (invokedMethod != null && invokedMethod.hasAnnotation(Condition.class)) {
                        String conditionSignature = invokedMethod.getSignature();
                        String conditionDescription = conditionDescriptions.get(conditionSignature);
                        if (conditionDescription != null) {
                            ConditionModel conditionAction = new ConditionModel("Condition: " + conditionDescription);
                            scenarioModel.addAction(conditionAction);
                            if (ifStatement.getThenStatement() != null) {
                                processBlock(ifStatement.getThenStatement(), conditionAction.getThenActions(), actionServices, conditionDescriptions);
                            }
                            if (ifStatement.getElseStatement() != null) {
                                processBlock(ifStatement.getElseStatement(), conditionAction.getElseActions(), actionServices, conditionDescriptions);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void processBlock(CtStatement block, List<ActionModel> actions, Map<String, String> actionServices, Map<String, String> conditionDescriptions) {
        if (block instanceof CtBlock<?>) {
            List<CtStatement> statements = ((CtBlock<?>) block).getStatements();
            processStatements(statements, actions, actionServices, conditionDescriptions);
        } else {
            processStatements(List.of(block), actions, actionServices, conditionDescriptions);
        }
    }

    private static void processStatements(List<CtStatement> statements, List<ActionModel> actions, Map<String, String> actionServices, Map<String, String> conditionDescriptions) {
        for (CtStatement statement : statements) {
            if (statement instanceof CtInvocation<?>) {
                CtInvocation<?> invocation = (CtInvocation<?>) statement;
                CtExecutable<?> invokedMethod = invocation.getExecutable().getDeclaration();
                if (invokedMethod != null && invokedMethod.hasAnnotation(Action.class)) {
                    String actionSignature = invokedMethod.getSignature();
                    String fullActionName = actionServices.get(actionSignature);
                    if (fullActionName != null) {
                        actions.add(new ActionModel(fullActionName));
                    }
                }
            }

            if (statement instanceof CtIf) {
                CtIf ifStatement = (CtIf) statement;
                List<CtInvocation<?>> conditionInvocations = ifStatement.getCondition().getElements(new TypeFilter<>(CtInvocation.class));
                for (CtInvocation<?> conditionInvocation : conditionInvocations) {
                    CtExecutable<?> invokedMethod = conditionInvocation.getExecutable().getDeclaration();
                    if (invokedMethod != null && invokedMethod.hasAnnotation(Condition.class)) {
                        String conditionSignature = invokedMethod.getSignature();
                        String conditionDescription = conditionDescriptions.get(conditionSignature);
                        if (conditionDescription != null) {
                            ConditionModel conditionAction = new ConditionModel("Condition: " + conditionDescription);
                            actions.add(conditionAction);
                            if (ifStatement.getThenStatement() != null) {
                                processBlock(ifStatement.getThenStatement(), conditionAction.getThenActions(), actionServices, conditionDescriptions);
                            }
                            if (ifStatement.getElseStatement() != null) {
                                processBlock(ifStatement.getElseStatement(), conditionAction.getElseActions(), actionServices, conditionDescriptions);
                            }
                        }
                    }
                }
            }
        }
    }

}