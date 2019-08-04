package ru.vachok.ostpst.utils;


import ru.vachok.ostpst.ConstantsOst;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.SortedMap;


/**
 @since 15.05.2019 (9:06) */
public class CharsetEncoding {
    
    
    private String charsetOnInput;
    
    private String charsetOnOutput = ConstantsOst.DEFAULT;
    
    public CharsetEncoding(String charsetOnInput, String charsetOnOutput) {
        this.charsetOnInput = charsetOnInput;
        this.charsetOnOutput = charsetOnOutput;
    }
    
    public CharsetEncoding(String charsetOnInput) {
        this.charsetOnInput = charsetOnInput;
    }
    
    public CharsetEncoding() {
        this.charsetOnInput = "UTF-8";
    }
    
    
    public String getStrInAnotherCharset(String strToConvert) throws RuntimeException {
        Charset inputChars;
        if (charsetOnInput.equals(ConstantsOst.DEFAULT)) {
            inputChars = Charset.defaultCharset();
        }
        else {
            inputChars = Charset.forName(charsetOnInput);
        }
        
        Charset outputChars;
    
        if (charsetOnOutput.equals(ConstantsOst.DEFAULT)) {
            outputChars = Charset.defaultCharset();
        }
        else {
            outputChars = Charset.forName(charsetOnOutput);
        }
        
        ByteBuffer buffer = inputChars.encode(strToConvert);
        CharBuffer charBuffer = outputChars.decode(buffer);
        
        strToConvert = new String(charBuffer.array()).trim();
        
        return strToConvert;
    }
    
    public String getCharsetNames() {
        SortedMap<String, Charset> charsetSortedMap = Charset.availableCharsets();
        return new TFormsOST().fromArray(charsetSortedMap.keySet());
    }
    
    public byte[] getInUnicode(String strToConvert) {
        Charset in = Charset.defaultCharset();
        Charset out = Charset.forName("unicode");
        ByteBuffer buffer = in.encode(strToConvert);
        CharBuffer charBuffer = out.decode(buffer);
        strToConvert = new String(charBuffer.array()).trim();
        return strToConvert.getBytes();
    }
    
}
