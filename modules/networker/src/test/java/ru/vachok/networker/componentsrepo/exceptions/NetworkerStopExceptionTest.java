package ru.vachok.networker.componentsrepo.exceptions;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;


public class NetworkerStopExceptionTest {
    
    
    @Test
    public void testGetMessage() {
        try {
            thrEx();
        }
        catch (NetworkerStopException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    private void thrEx() throws NetworkerStopException {
        throw new NetworkerStopException("test", "test", 1);
    }
}