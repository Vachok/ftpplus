package ru.vachok.ostpst.fileworks;


import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;


public class ParserFoldersWithAttachmentsTest {
    
    
    @Test
    public void testFolders() {
        Charset cp1251 = Charset.forName("windows-1251");
        Charset defaultCharset = Charset.defaultCharset();
        String fileName = "c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost";
        ByteBuffer byteBuffer = cp1251.encode(fileName);
        CharBuffer charBuffer = defaultCharset.decode(byteBuffer);
        fileName = new String(charBuffer.array()).trim();
        ParserFoldersWithAttachments parserFoldersWithAttachments = new ParserFoldersWithAttachments(fileName);
        parserFoldersWithAttachments.getContents();
    }
    
    
}