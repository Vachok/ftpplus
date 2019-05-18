// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.utils.TForms;

import java.io.FileInputStream;
import java.util.Scanner;


@SuppressWarnings("ALL") public class MenuItemsConsoleImplTest {
    
    
    @Test
    public void eigthTest() {
        String fileName = "tmp_t.p.magdich.pst";
        MakeConvert makeConvert = new ConverterImpl(fileName);
        System.out.println("Enter folder ID:");
        try (Scanner scanner = new Scanner(new FileInputStream("scanner"))) {
            long folderID = scanner.nextLong();
            while (scanner.hasNextLine()) {
                System.out.println("...and message ID or Subject:");
                scanner.reset();
                if (scanner.hasNextLong()) {
                    long messageID = scanner.nextLong();
                    System.out.println(makeConvert.searchMessages(folderID, messageID));
                    System.out.println("MENU LOCAL SHOWN");
                    
                }
                else if (scanner.hasNextLine()) {
                    String subj = scanner.nextLine().replaceFirst(" ", "");
                    Assert.assertNotNull(subj);
                    Assert.assertFalse(subj.isEmpty());
                    System.out.println(makeConvert.searchMessages(folderID, subj));
                    System.out.println("\n\n\nMENU LOCAL SHOWN");
                }
                else {
                    System.out.println("\n\n\nMENU LOCAL SHOWN");
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println(new TForms().fromArray(e));
        }
    }
    
}