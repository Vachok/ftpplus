package ru.vachok.networker.mailserver;


import org.springframework.stereotype.Component;

/**
 @since 09.11.2018 (9:38) */
@Component("ruleset")
public class RuleSet {

    private String identity;

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
}
