// Copyright (c) all rights. http://networker.vachok.ru 2019.

import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.MakeConvertOrCopy;
import ru.vachok.ostpst.OstToPstStart;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;


@SuppressWarnings("ALL") public class TestMain {


    @Test(enabled = true)
    public void launchProg() {
        File file = new File(new String("c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost".getBytes(), Charset.forName("UTF-8")));
        OstToPstStart ostToPstStart = new OstToPstStart();
        try {
            OstToPstStart.main(new String[]{"-t"});
        }
        catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test(enabled = false)
    public void complexTest() {
        String fileName = getFileName();
        MakeConvertOrCopy makeConvertOrCopy = new ConverterImpl(fileName);
        String listFolders = makeConvertOrCopy.showListFolders();
        Assert.assertNotNull(listFolders);
        boolean totalC = listFolders.contains("totalCounter =");
        Assert.assertTrue(totalC, "List folders incorrect");

        String csvFileName = Paths.get(fileName).getParent().toAbsolutePath() + "\\cnt.csv";
        String saveContacts = makeConvertOrCopy.saveContacts(csvFileName);
        Assert.assertNotNull(saveContacts);
        Assert.assertTrue(new File(csvFileName).exists());

        String saveFolders = null;
        try {
            saveFolders = makeConvertOrCopy.saveFolders();
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TFormsOST().fromArray(e));
        }
        boolean foldersTxt = saveFolders.contains(ConstantsOst.FILENAME_FOLDERSTXT);
        Assert.assertTrue(foldersTxt);

        String showContacts = makeConvertOrCopy.showContacts();
        Assert.assertNotNull(showContacts);

        String cleanPreviousCopy = makeConvertOrCopy.cleanPreviousCopy();
        Assert.assertNotNull(cleanPreviousCopy);

        String parseObject = makeConvertOrCopy.getObjectItemsByID(35906);
        Assert.assertFalse(parseObject.contains("null (null)"));

        List<String> subjList = makeConvertOrCopy.getListMessagesSubjectWithID(8578);

        System.out.println(listFolders);
        System.out.println(saveContacts);
        System.out.println(showContacts);
        System.out.println(cleanPreviousCopy);
        System.out.println(parseObject);
        System.out.println(new TFormsOST().fromArray(subjList));
    }

    private void showBytes() {
        try {
            String str8 = new String('И', "UTF-8");
            str8 = new CharsetEncoding("UTF-8").getStrInAnotherCharset(str8);
            byte[] bytes = str8.getBytes();
            for (int i = 0; i < bytes.length; i++) {
                System.out.println("bytes = " + bytes[i]);
            }
            System.out.println(str8);

            String str1251 = new String('И'.getBytes(), ConstantsOst.CP_WINDOWS_1251);
            str1251 = new CharsetEncoding(ConstantsOst.CP_WINDOWS_1251).getStrInAnotherCharset(str1251);
            byte[] str1251Bytes = str1251.getBytes();
            for (int i = 0; i < str1251Bytes.length; i++) {
                System.out.println("bytes1251 = " + str1251Bytes[i]);
            }
            System.out.println(str1251);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] inUnicode = new CharsetEncoding().getInUnicode('И');
        for (int i = 0; i < inUnicode.length; i++) {
            System.out.println("inUnicode = " + inUnicode[i]);
        }
        try {
            System.out.println("UNICODE = " + new String(inUnicode, "unicode"));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String getFileName() {
        if (new File("c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost")
            .exists()) {
            return "c:\\Users\\ikudryashov\\OneDrive\\Документы\\Файлы Outlook\\ksamarchenko@velkomfood.ru.ost";
        }
        else {
            return "\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\v.v.palgova.pst";
        }
    }
}
