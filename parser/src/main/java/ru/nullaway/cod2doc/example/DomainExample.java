package ru.nullaway.cod2doc.example;


import ru.nullaway.cod2doc.api.annotation.Action;
import ru.nullaway.cod2doc.api.annotation.Service;

@Service("DOMAIN SERVICE")
public class DomainExample {

    @Action("Domain action")
    public void domainAction(){

    }

    @Action("Another domain action")
    public void anotherDomainAction(){

    }

}
