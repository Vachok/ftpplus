// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mail;


import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.controller.ExCTRL;


/**<b>Exchange Rules Changer</b>
 @since 09.11.2018 (9:38) */
@Component(ModelAttributeNames.AT_NAME_RULESET)
public class RuleSet {

    /**
     Get-TransportRule -identity
     */
    private String identity;

    /**
     Get-TransportRule -fromAddressMatchesPatterns
     */
    private String fromAddressMatchesPatterns;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getFromAddressMatchesPatterns() {
        return fromAddressMatchesPatterns;
    }

    public void setFromAddressMatchesPatterns(String fromAddressMatchesPatterns) {
        this.fromAddressMatchesPatterns = fromAddressMatchesPatterns;
    }

    /**
     <b>Разбор ввода в форму</b>
     {@link ExCTRL#ruleSetPost(RuleSet, Model)}

     @return Set-TransportRule -Identity "{@link #identity}" -FromAddressMatchesPattern {@link #fromAddressMatchesPatterns} -ExceptIfHasClassification "Default\ExCompanyConfidential"
     */
    public String getCopyToRuleSetter() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append("Set-TransportRule -Identity \"")
            .append(identity)
            .append("\" -FromAddressMatchesPatterns ");
        for (String s : fromAddressMatchesPatterns.split(" ")) {
            s = s.trim() + ", ";
            if (s.matches("^\\w.*")) stringBuilder.append(s);
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(" -ExceptIfHasClassification \"Default\\ExCompanyConfidential\"");
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RuleSet{");
        sb.append("identity='").append(identity).append('\'');
        sb.append(", fromAddressMatchesPatterns='").append(fromAddressMatchesPatterns).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
