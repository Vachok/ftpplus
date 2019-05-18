// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.utils.FileSystemWorker;
import ru.vachok.ostpst.utils.TForms;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;


/**
 @since 16.05.2019 (12:06) */
class MenuItemsConsoleImpl implements MenuItems {
    
    
    private static UserMenu userMenu = new MenuConsoleLocal();
    
    private static MakeConvert makeConvert = null;
    
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
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".askUser", e));
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
                MakeConvert converter = new ConverterImpl(fileName);
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
        makeConvert = new ConverterImpl(fileName);
        while (scanner.hasNextInt()) {
            int userAns = scanner.nextInt();
            if (userAns == 1) {
                new MenuItemsConsoleImpl(fileName).ansIsOneSaveContToCSV();
            }
            else if (userAns == 2) {
                MakeConvert makeConvert = new ConverterImpl(fileName);
                makeConvert.showContacts();
                new MenuConsoleLocal(fileName).showMenu();
            }
            else if (userAns == 3) {
                MakeConvert makeConvert = new ConverterImpl(fileName);
                System.out.println(makeConvert.showListFolders());
                new MenuConsoleLocal(fileName).showMenu();
            }
            else if (userAns == 4) {
                MakeConvert makeConvert = new ConverterImpl(fileName);
                makeConvert.getDequeFolderNamesAndWriteToDisk();
                new MenuConsoleLocal(fileName).showMenu();
            }
            else if (userAns == 5) {
                new MenuItemsConsoleImpl(fileName).ansIsFiveParseByID();
            }
            else if (userAns == 6) {
                new MenuItemsConsoleImpl(fileName).ansIsSixGetListMSGSubj();
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
                new MenuConsoleLocal(fileName).showMenu();
            }
        }
        userMenu.showMenu();
    }
    
    private void ansEightSearch() {
        System.out.println("Enter folder ID :");
        try (Scanner scanner = new Scanner(System.in)) {
            long folderID = Long.parseLong(scanner.nextLine());
            System.out.println("...and message ID or Subject:");
            while (scanner.hasNextLine()) {
                scanner.reset();
                if (scanner.hasNextLong()) {
                    long messageID = scanner.nextLong();
                    System.out.println(makeConvert.searchMessages(folderID, messageID));
                    new MenuConsoleLocal(fileName).showMenu();
                }
                else if (scanner.hasNextLine()) {
                    String subj = scanner.nextLine();
                    System.out.println(makeConvert.searchMessages(folderID, subj));
                    new MenuConsoleLocal(fileName).showMenu();
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println(new TForms().fromArray(e));
            userMenu.showMenu();
        }
    }
    
    private void ansIsSixGetListMSGSubj() {
        System.out.println("Enter folder id: ");
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLong()) {
                long objID = scanner.nextLong();
                List<String> subjectWithID = makeConvert.getListMessagesSubjectWithID(objID);
                System.out.println(new TForms().fromArray(subjectWithID));
                new MenuConsoleLocal(fileName).showMenu();
            }
        }
        catch (Exception e) {
            userMenu.showMenu();
        }
    }
    
    private void ansIsFiveParseByID() {
        System.out.println("Enter object ID: ");
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLong()) {
                long objID = scanner.nextLong();
                MakeConvert converter = new ConverterImpl(fileName);
                String itemsByID = converter.getObjectItemsByID(objID);
                System.out.println(itemsByID);
                new MenuConsoleLocal(fileName).showMenu();
            }
        }
    }
    
    private void ansSevenCopy() {
        System.out.println("New copy? (y/n)");
        System.out.println();
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String newCP = scanner.nextLine();
                System.out.println(makeConvert.copyierWithSave(newCP));
                new MenuConsoleLocal("tmp_" + new File(fileName).getName()).showMenu();
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}
