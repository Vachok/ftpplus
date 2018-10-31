package ru.vachok.networker.mailserver;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 @since 05.10.2018 (10:05) */
@Component("rulesbean")
public class RulesBean {

    private String runspaceId;

    private String description;

    private String conditions;

    private String exceptions;

    private String actions;

    private String query;

    private boolean state;

    private String otherFields;

    private List<RulesBean> allRules = new ArrayList<>();

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<RulesBean> getAllRules() {
        return allRules;
    }

    public void setAllRules(List<RulesBean> allRules) {
        this.allRules = allRules;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getExceptions() {
        return exceptions;
    }

    public void setExceptions(String exceptions) {
        this.exceptions = exceptions;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getOtherFields() {
        return otherFields;
    }

    public void setOtherFields(String otherFields) {
        this.otherFields = otherFields;
    }

    public String getRunspaceId() {
        return runspaceId;
    }

    public void setRunspaceId(String runspaceId) {
        this.runspaceId = runspaceId;
    }
}
