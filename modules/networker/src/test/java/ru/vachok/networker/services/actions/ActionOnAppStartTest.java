package ru.vachok.networker.services.actions;


import org.springframework.context.ConfigurableApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.TForms;

import java.awt.event.ActionEvent;


/**
 @see ActionOnAppStart
 @since 30.07.2019 (10:16) */
public class ActionOnAppStartTest {
    
    
    @Test
    public void testActionPerformed() {
        try {
            ActionEvent event = new ActionEvent(this, 42, "do");
            new ActionOnAppStart().actionPerformed(event);
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        boolean closeContext = false;
        try (ConfigurableApplicationContext context = IntoApplication.getConfigurableApplicationContext()) {
            context.stop();
            context.close();
            closeContext = IntoApplication.closeContext();
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertTrue(closeContext);
    }
}