package ru.vachok.networker.services;


import org.testng.annotations.Test;


public class PhotoConverterSRVTest {
    
    
    @Test
    public void convertFoto() {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        String psCommands = photoConverterSRV.psCommands();
        System.out.println(psCommands);
    }
    
}