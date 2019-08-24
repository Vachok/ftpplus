// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mail;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.MailRulesAttributes;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 @see ExSRV
 @since 22.06.2019 (16:51) */
@SuppressWarnings("ALL")
public class ExSRVTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private List<MailRule> mailRules = new ArrayList<>();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    /**
     @see ExSRV#fileAsStrings()
     */
    @Test()
    public void testFileAsStrings() {
        ExSRV exSRV = new ExSRV();
        Queue<String> rulesQ = new LinkedList<>();
        try (InputStream inputStream = getClass().getResourceAsStream("/rules.txt");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "x-UTF-16LE-BOM");
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            bufferedReader.lines().forEach(rulesQ::add);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        try {
            Assert.assertTrue(rulesQ.size() > 10);
    
            makeRules(rulesQ, new MailRule());
            Assert.assertTrue(mailRules.size() > 50);
            String fromArray = new TForms().fromArray(mailRules);
            Assert.assertTrue(fromArray.contains("ruleID=33"));
        }
        catch (NullPointerException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Contract("_ -> fail")
    private void tryWithFile(@NotNull ExSRV exSRV) {
        throw new TODOException("30.07.2019 (13:45)");
    }
    
    private void makeRules(@NotNull final Queue<String> rulesQ, MailRule mailRule) {
        MailRulesAttributes[] mailRulesAttr = MailRulesAttributes.values();
        List<String> otherFields = mailRule.getOtherFields();
        while (rulesQ.size() > 0) {
            String ruleAttr = rulesQ.poll();
            try {
                if (ruleAttr.contains(MailRulesAttributes.Name.name())) {
                    mailRule.setName(ruleAttr.split("\\Q: \\E")[1]);
                }
                else if (ruleAttr.contains(MailRulesAttributes.Actions.name())) {
                    mailRule.setActions(ruleAttr.split("\\Q : \\E")[1]);
                }
                else if (ruleAttr.contains(MailRulesAttributes.Priority.name())) {
                    mailRule.setRuleID(Integer.parseInt(ruleAttr.split("\\Q : \\E")[1]));
                }
                else if (ruleAttr.contains(MailRulesAttributes.Conditions.name())) {
                    mailRule.setConditions(ruleAttr.split("\\Q : \\E")[1]);
                }
                else if (ruleAttr.contains(MailRulesAttributes.Exceptions.name())) {
                    mailRule.setExceptions(ruleAttr.split("\\Q : \\E")[1]);
                }
                else if (ruleAttr.contains(MailRulesAttributes.Description.name())) {
                    mailRule.setDescription(ruleAttr.split("\\Q : \\E")[1]);
                }
                else if (ruleAttr.contains(MailRulesAttributes.State.name())) {
                    mailRule.setState(Boolean.parseBoolean(ruleAttr.split("\\Q : \\E")[1]));
                }
                else if (ruleAttr.contains(MailRulesAttributes.ExchangeVersion.name())) {
                    mailRules.add(mailRule);
                    makeRules(rulesQ, new MailRule());
                }
                else {
                    otherFields.add(ruleAttr);
                }
            }
            catch (ArrayIndexOutOfBoundsException ignore) {
                //
            }
        }
    }
}