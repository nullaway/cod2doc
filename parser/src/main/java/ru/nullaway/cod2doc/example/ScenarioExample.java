package ru.nullaway.cod2doc.example;


import ru.nullaway.cod2doc.api.annotation.Condition;
import ru.nullaway.cod2doc.api.annotation.Scenario;
import ru.nullaway.cod2doc.api.annotation.ScenarioParameter;

public class ScenarioExample {

    DomainExample domainExample = new DomainExample();
    PortExampleInterface portExample = new PortExample();

    class DtoExample {
        @ScenarioParameter("king")
        public String fieldExample;
    }

    @Scenario("Scenario name")
    public void scenarioName(@ScenarioParameter(value = "vlad", description = "dalv") DtoExample dtoExample) {
        portExample.portAction();
        if (isFieldExampleNull(dtoExample)) {
            domainExample.anotherDomainAction();
            if (isFieldExampleNull(dtoExample)) {
                domainExample.anotherDomainAction();
            } else {
                domainExample.domainAction();
            }
        } else {
            domainExample.domainAction();
        }
        portExample.portAction();
    }


    @Condition("Field is null")
    private static boolean isFieldExampleNull(DtoExample dtoExample) {
        return dtoExample.fieldExample != null;
    }

}
