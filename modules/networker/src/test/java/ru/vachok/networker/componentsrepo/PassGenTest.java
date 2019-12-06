package ru.vachok.networker.componentsrepo;


import org.testng.Assert;
import org.testng.annotations.Test;


public class PassGenTest {
    
    
    @Test
    public void testGenPass() {
        PassGen passGen = new PassGen();
        String passwords = passGen.generatePasswords();
        System.out.println("passwords = " + passwords);
        Assert.assertTrue(passwords.contains("storeman4"));
    }
    
    @Test
    public void testToString() {
        String toString = new PassGen().toString();
        Assert.assertEquals(toString, "PassGen{userNames=[], numOfChars=8, chars=[1, 2, 3, 4, 5, 6, 7, 8, 9, 0, A, B, C, D, E]}");
    }
}