// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.ostpst.ConstantsFor;
import ru.vachok.ostpst.utils.CharsetEncoding;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;


public class MenuConsoleLocalTest {
    
    
    @Test(enabled = false)
    public void checkCons() {
        UserMenu userMenu = new MenuConsoleLocal(new CharsetEncoding(ConstantsFor.CP_WINDOWS_1251)
            .getStrInAnotherCharset("tmp_t.p.magdich.pst"));
        try (InputStream inputStream = new FileInputStream("scanner");
             Scanner scanner = new Scanner(inputStream);
             OutputStream outputStream = System.out
        ) {
            while (scanner.hasNextInt()) {
                outputStream.write(scanner.nextInt());
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
        
    }
    
    @Test(enabled = false)
    public void exitProg() {
        UserMenu userMenu = new MenuConsoleLocal();
        try (Scanner scanner = new Scanner(new FileInputStream("exitProg"))) {
            while (scanner.hasNextLine()) {
                userMenu.exitProgram(scanner.nextLine());
            }
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
}