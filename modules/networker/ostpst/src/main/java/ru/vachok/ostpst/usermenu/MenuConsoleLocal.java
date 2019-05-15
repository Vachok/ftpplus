package ru.vachok.ostpst.usermenu;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.utils.CharsetEncoding;

import java.awt.*;
import java.nio.charset.Charset;
import java.util.Scanner;


/**
 @since 14.05.2019 (9:20) */
public class MenuConsoleLocal implements UserMenu {
    
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private String fileName;
    
    public MenuConsoleLocal(String fileName) {
        this.fileName = fileName;
    }
    
    public MenuConsoleLocal() {
        this.fileName = null;
    }
    
    @Override public void showMenu() {
        System.out.println("Please, enter a filename: ");
        if (fileName == null) {
            try (Scanner scanner = new Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    String nextLine = scanner.nextLine();
                    this.fileName = new String(nextLine.getBytes(), Charset.forName("UTF-8"));
                    chkFile(scanner);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            showMenu();
        }
        else {
            callFromConstructor();
        }
    }
    
    private void callFromConstructor() {
        this.fileName = new CharsetEncoding("windows-1251").getStrInAnotherCharset(fileName);
        chkFile();
    }
    
    private void chkFile() {
        if (checkFileSize(fileName) != 666) {
            askUser();
        }
        else {
            System.err.println("Error. Can't convert " + fileName);
        }
    }
    
    private void chkFile(Scanner scanner) {
        double fileSize = checkFileSize(fileName);
        boolean isOst = fileName.toLowerCase().contains(".ost") || fileName.toLowerCase().contains(".pst");
        if (fileSize == 666 || !isOst) {
            System.err.println(getClass().getSimpleName() + "ERROR :\n" + "No file, or file is corrupted");
            System.out.println("Enter another file, or type exit for exit");
        }
        else {
            System.out.println("Checking file size... Filename is: " + fileName);
            messageToUser.info(getClass().getSimpleName() + ".showMenu", fileName, " = " + fileSize + " GB");
            askUser();
        }
    }
    
    private void askUser() {
        System.out.println("fileName = " + fileName);
        MakeConvert makeConvert = new ConverterImpl(fileName);
        System.out.println("saveContacts = " + makeConvert.saveContacts(null));
        makeConvert.saveFolders();
    }
    
    private void askUser(Scanner scanner) {
        showSecondStage();
        while (scanner.hasNext()) {
            int userAns = scanner.nextInt();
            messageToUser.info(getClass().getSimpleName() + ".askUser", "userAns", " = " + userAns);
            if (userAns == 3) {
                System.exit(222);
            }
            if (userAns == 2) {
                throw new IllegalComponentStateException("14.05.2019 (12:22)");
            }
            if (userAns == 1) {
                System.out.println("Enter name of csv, for contacts save:");
                String csvFileName = scanner.nextLine();
                MakeConvert makeConvert = new ConverterImpl(fileName);
                makeConvert.saveContacts(csvFileName);
            }
        }
    }
    
    private void showSecondStage() {
        System.out.println("What should I do with this file?");
        System.out.println("1. Save contacts to csv");
        System.out.println("2. Convert to another format");
        System.out.println("3. Exit");
        System.out.println("Choose: ");
    }
}
