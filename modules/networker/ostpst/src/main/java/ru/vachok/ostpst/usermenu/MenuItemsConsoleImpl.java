// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import com.pff.PSTException;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.MakeConvertOrCopy;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.utils.FileSystemWorkerOST;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;


/**
 @since 16.05.2019 (12:06) */
public class MenuItemsConsoleImpl implements MenuItems {
    
    
    private static UserMenu userMenu = new MenuConsoleLocal();
    
    private static MakeConvertOrCopy makeConvertOrCopy;
    
    private static long folderID;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private String fileName;
    
    public MenuItemsConsoleImpl(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public void askUser() {
        try (Scanner scanner = new Scanner(System.in)) {
            showSecondStage();
            askUser(scanner, fileName);
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorkerOST.error(getClass().getSimpleName() + ".askUser", e));
            new MenuConsoleLocal(null).showMenu();
        }
    }
    
    @Override public void showSecondStage() {
        System.out.println("What should I do with this file?");
        System.out.println("1. Save contacts to csv");
        System.out.println("2. Show contacts");
        System.out.println("3. Show folders");
        System.out.println("4. Write folder names to disk");
        System.out.println("5. Parse object");
        System.out.println("6. Show message subjects");
        System.out.println("7. Copy file");
        System.out.println("8. Search message");
        System.out.println("0. Exit");
        System.out.println("Choose: ");
    }
    
    private void ansIsOneSaveContToCSV() {
        System.out.println("Enter name of csv, for contacts save:");
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String csvFileName = scanner.nextLine();
                MakeConvertOrCopy converter = new ConverterImpl(fileName);
                String saveContacts = converter.saveContacts(csvFileName);
                messageToUser.warn(saveContacts);
                new MenuConsoleLocal(fileName).showMenu();
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    private static void askUser(Scanner scanner, String fileName) throws IOException {
        makeConvertOrCopy = new ConverterImpl(fileName);
        while (scanner.hasNextInt()) {
            int userAns = scanner.nextInt();
            if (userAns == 1) {
                new MenuItemsConsoleImpl(fileName).ansIsOneSaveContToCSV();
            }
            else if (userAns == 2) {
                new MenuItemsConsoleImpl(fileName).ansTwo();
            }
            else if (userAns == 3) {
                new MenuItemsConsoleImpl(fileName).ansThree();
            }
            else if (userAns == 4) {
                new MenuItemsConsoleImpl(fileName).ansIsFour();
            }
            else if (userAns == 5) {
                new MenuItemsConsoleImpl(fileName).ansIsFiveParseByID();
            }
            else if (userAns == 6) {
                new MenuItemsConsoleImpl(fileName).ansIsSixGetListMSGSubj(folderID);
            }
            else if (userAns == 7) {
                new MenuItemsConsoleImpl(fileName).ansSevenCopy();
            }
            else if (userAns == 8) {
                new MenuItemsConsoleImpl(fileName).ansEightSearch();
            }
            else if (userAns == 0) {
                userMenu.exitProgram(fileName);
            }
            else if (userAns == 10) {
                userMenu.showMenu();
            }
            else {
                System.out.println("Incorrect choice!");
                new MenuItemsConsoleImpl(fileName).askUser();
            }
        }
        userMenu.showMenu();
    }
    
    private void ansTwo() {
        makeConvertOrCopy.showContacts();
        askUser();
    }
    
    private void ansThree() {
        System.out.println(makeConvertOrCopy.showListFolders());
        askUser();
    }
    
    private void ansIsFour() throws IOException {
        makeConvertOrCopy.getDequeFolderNamesAndWriteToDisk();
        askUser();
    }
    
    private void ansEightSearch() {
        ansEightSearch(0);
    }
    
    private void ansEightSearchSecondStage(Scanner scanner, long folderID) {
        System.out.println("Another message? (0 for back, 6 - show subjects)");
        scanner.reset();
        if (scanner.hasNextLong()) {
            long messageID = scanner.nextLong();
            if (messageID == 0) {
                new MenuConsoleLocal(fileName).showMenu();
            }
            if (messageID == 6) {
                new MenuItemsConsoleImpl(fileName).ansIsSixGetListMSGSubj(folderID);
            }
            System.out.println(makeConvertOrCopy.searchMessages(folderID, messageID));
            this.ansEightSearchSecondStage(scanner, folderID);
        }
        else if (scanner.hasNextLine()) {
            String subj = scanner.nextLine();
            try {
                System.out.println(makeConvertOrCopy.searchMessages(subj));
            }
            catch (PSTException | IOException e) {
                messageToUser.error(FileSystemWorkerOST.error(getClass().getSimpleName() + ".ansEightSearchSecondStage", e));
            }
            this.ansEightSearchSecondStage(scanner, folderID);
        }
        else {
            new MenuConsoleLocal(fileName).showMenu();
        }
    }
    
    private void ansIsSixGetListMSGSubj(long folderID) {
        if (folderID == 0) {
            System.out.println("Enter folder id: ");
            try (Scanner scanner = new Scanner(System.in)) {
                folderID = scanner.nextLong();
                List<String> subjectWithID = makeConvertOrCopy.getListMessagesSubjectWithID(folderID);
                System.out.println(new TFormsOST().fromArray(subjectWithID));
                new MenuItemsConsoleImpl(fileName).askUser();
            }
            catch (Exception e) {
                System.out.println(e.getMessage() + "\n\n" + new TFormsOST().fromArray(e));
                new MenuItemsConsoleImpl(fileName).askUser();
            }
        }
        List<String> subjectWithID = makeConvertOrCopy.getListMessagesSubjectWithID(folderID);
        System.out.println(new TFormsOST().fromArray(subjectWithID));
        this.ansEightSearch(folderID);
    }
    
    private void ansEightSearch(long id) {
        System.out.println("Enter folder ID, 10 to search EVERYWHERE or 0 to return :");
        try (Scanner scanner = new Scanner(System.in)) {
            if (id > 0) {
                ansEightSearchSecondStage(scanner, id);
            }
            id = -1;
            try {
                id = Long.parseLong(scanner.nextLine());
            }
            catch (NumberFormatException e) {
                System.out.println("NumberFormat incorrect:\n");
                System.out.println(e.getMessage());
                new MenuConsoleLocal(fileName).showMenu();
            }
            if (id == 0) {
                new MenuConsoleLocal(fileName).showMenu();
            }
            else if (id == 10) {
                ansEightSearchEverywhere();
            }
            else {
                System.out.println("...and message ID or Subject:");
                while (scanner.hasNextLine()) {
                    ansEightSearchSecondStage(scanner, id);
                }
            }
        }
        catch (NumberFormatException e) {
            System.out.println(e);
            System.out.println(new TFormsOST().fromArray(e));
            new MenuConsoleLocal(fileName).showMenu();
        }
        askUser();
    }
    
    private void ansEightSearchEverywhere() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter what are U wanna find:");
            String whatFind = scanner.nextLine();
            System.out.println("Started Search. It takes about 8-10 minutes. After all, see results in " + Paths.get(".").toAbsolutePath().normalize());
            Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(()->makeConvertOrCopy.searchMessages(whatFind));
        }
    }
    
    private void ansIsFiveParseByID() {
        System.out.println("Enter object ID: ");
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLong()) {
                long objID = scanner.nextLong();
                MakeConvertOrCopy converter = new ConverterImpl(fileName);
                String itemsByID = converter.getObjectItemsByID(objID);
                System.out.println(itemsByID);
                new MenuConsoleLocal(fileName).showMenu();
            }
        }
    }
    
    private void ansSevenCopy() {
        System.out.println("New copy? (y/n) (e - exit)");
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(ConstantsOst.FILENAME_PROPERTIES));
            this.fileName = properties.getProperty(ConstantsOst.PR_TMPFILE);
            System.out.println("Your last copy: " + fileName);
            System.out.println("c - continue last");
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
            new MenuItemsConsoleImpl(fileName).askUser();
        }
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String newCP = scanner.nextLine();
                System.out.println(makeConvertOrCopy.copyierWithSave(newCP));
                this.fileName = Paths.get(".").normalize().toAbsolutePath() + ConstantsOst.SYSTEM_SEPARATOR + "tmp_" + fileName;
                new MenuItemsConsoleImpl(fileName).askUser();
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + new TFormsOST().fromArray(e));
            new MenuConsoleLocal(fileName).showMenu();
        }
    }
}
