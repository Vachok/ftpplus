package ru.vachok.networker.componentsrepo.exceptions;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;

import java.text.MessageFormat;
import java.util.Properties;


/**
 @see PropertiesAppNotFoundException
 @since 17.07.2019 (11:00) */
public class PropertiesAppNotFoundExceptionTest {
    
    
    private static Properties properties = AppComponents.getProps();
    
    @Test
    public void testThrow() {
        try {
            getPropsForTesting();
        }
        catch (PropertiesAppNotFoundException e) {
            Assert.assertNotNull(e);
            System.out.println(MessageFormat.format("{0}\nStack:\n{1}", e.getMessage(), properties.getProperty("")));
        }
    }
    
    private void getPropsForTesting() {
        throw new PropertiesAppNotFoundException(properties.size());
    }
}