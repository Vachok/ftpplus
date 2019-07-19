package ru.vachok.networker.componentsrepo;


import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.RejectedExecutionException;


/**
 @see ArgsReader
 @since 19.07.2019 (9:51) */
public class ArgsReaderTest {
    
    
    private String[] args;
    
    @Test
    public void testReadArgs() {
        this.args = new String[]{"-r test"};
        ArgsReader argsReader = new ArgsReader(new AnnotationConfigWebApplicationContext(), args);
        try {
            argsReader.run();
        }
        catch (RejectedExecutionException e) {
            System.out.println("argsReader.toString() = " + argsReader.toString());
        }
    }
    
    @Test
    public void testToString1() {
        this.args = new String[]{"test"};
        String toString = new ArgsReader(new AnnotationConfigWebApplicationContext(), args).toString();
        Assert.assertTrue(toString.contains("=test"), toString);
    }
}