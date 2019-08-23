package ru.vachok.networker.mail;


import org.springframework.stereotype.Component;

import java.util.*;


/**
 @since 05.10.2018 (10:05)
 */
@Component("mailrule")
public class MailRule {

    private String runspaceId;

    private int ruleID;
    
    private List<String> otherFields = new ArrayList<>();

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
        final StringBuilder sb = new StringBuilder("MailRule{");
        sb.append(", ruleID=").append(ruleID);
        sb.append(", description='").append(description).append('\'');
        sb.append(", conditions='").append(conditions).append('\'');
        sb.append(", exceptions='").append(exceptions).append('\'');
        sb.append(", actions='").append(actions).append('\'');
        sb.append(", query='").append(query).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", state=").append(state);
        sb.append(", otherFields size=").append(otherFields.size());
        sb.append('}');
        return sb.toString();
    }
    
    
}
