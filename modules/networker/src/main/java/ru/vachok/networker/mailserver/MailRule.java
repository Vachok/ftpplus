package ru.vachok.networker.mailserver;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


/**
 @since 05.10.2018 (10:05) */
@Component("mailrule")
public class MailRule {

    private String runspaceId;

    private int ruleID;

    private List<String> otherFields;

    public int getRuleID() {
        return ruleID;
    }

    private String description;

    private String conditions;

    private String exceptions;

    private String actions;

    private String query;

    private String name;

    public void setRuleID(int ruleID) {
        this.ruleID = ruleID;
    }

    public List<String> getOtherFields() throws NullPointerException {
        return otherFields;
    }

    public void setOtherFields(List<String> otherFields) {
        this.otherFields = otherFields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private boolean state;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

    public String getRunspaceId() {
        return runspaceId;
    }

    public void setRunspaceId(String runspaceId) {
        this.runspaceId = runspaceId;
    }
    
    public static String fromArrayRules(Map<Integer, MailRule> mailRules, boolean br) {
        StringBuilder nStringBuilder = new StringBuilder();
        StringBuilder brStringBuilder = new StringBuilder();
        mailRules.forEach((x, y)->{
            nStringBuilder
                .append("\n")
                .append(x)
                .append(" MAP ID  RULE:")
                .append("\n")
                .append(y);
            brStringBuilder
                .append("<p><h4>")
                .append(x)
                .append(" MAP ID  RULE:</h4>")
                .append("<br>")
                .append(y)
                .append("</p>");
        });
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    
    @Override
    public String toString() {
        return new StringJoiner("\n", MailRule.class.getSimpleName() + "\n", "\n")
            .add("name='" + name + "'\n")
            .add("state=" + state)
            .add("actions='" + actions + "'\n")
            .add("conditions='" + conditions + "'\n")
            .add("description='" + description + "'\n")
            .add("exceptions='" + exceptions + "'\n")
            .toString();
    }
}
