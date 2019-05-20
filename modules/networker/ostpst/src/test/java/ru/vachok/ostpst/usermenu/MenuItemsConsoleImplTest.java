// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvertOrCopy;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.FileInputStream;
import java.util.Scanner;


@SuppressWarnings("ALL") public class MenuItemsConsoleImplTest {
    
    
    @Test
    public void eigthTest() {
        String fileName = "tmp_t.p.magdich.pst";
        MakeConvertOrCopy makeConvertOrCopy = new ConverterImpl(fileName);
        System.out.println("Enter folder ID:");
        try (Scanner scanner = new Scanner(new FileInputStream("MenuItemsConsoleImplTest.8"))) {
            long folderID = scanner.nextLong();
            while (scanner.hasNextLine()) {
                System.out.println("...and message ID or Subject:");
                scanner.reset();
                if (scanner.hasNextLong()) {
                    long messageID = scanner.nextLong();
                    System.out.println(makeConvertOrCopy.searchMessages(folderID, messageID));
                    System.out.println("MENU LOCAL SHOWN");
                    
                }
                else if (scanner.hasNextLine()) {
                    String subj = scanner.nextLine().replaceFirst(" ", "");
                    Assert.assertNotNull(subj);
                    Assert.assertFalse(subj.isEmpty());
                    System.out.println(makeConvertOrCopy.searchMessages(folderID, subj));
                    System.out.println("\n\n\nMENU LOCAL SHOWN");
                }
                else {
                    System.out.println("\n\n\nMENU LOCAL SHOWN");
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println(new TFormsOST().fromArray(e));
        }
    }
    
}