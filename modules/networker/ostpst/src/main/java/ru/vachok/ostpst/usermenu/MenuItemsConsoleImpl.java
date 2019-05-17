package ru.vachok.ostpst.usermenu;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.utils.FileSystemWorker;

import java.io.IOException;
import java.util.Scanner;


/**
 @since 16.05.2019 (12:06) */
class MenuItemsConsoleImpl implements MenuItems {
    
    
    private static UserMenu userMenu = new MenuConsoleLocal();
    
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
    
    @Override public void ansIsOne() {
        System.out.println("Enter name of csv, for contacts save:");
    
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String csvFileName = scanner.nextLine();
                MakeConvert makeConvert = new ConverterImpl(fileName);
                String saveContacts = makeConvert.saveContacts(csvFileName);
                messageToUser.warn(saveContacts);
                new MenuConsoleLocal(fileName).showMenu();
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    @Override public void showSecondStage() {
        System.out.println("What should I do with this file?");
        System.out.println("1. Save contacts to csv");
        System.out.println("2. Show contacts");
        System.out.println("3. Show folders");
        System.out.println("4. Write folder names to disk");
        System.out.println("5. Parse object");
        System.out.println("0. Exit");
        System.out.println("Choose: ");
    }
    
    private static void askUser(Scanner scanner, String fileName) throws IOException {
        while (scanner.hasNextInt()) {
            int userAns = scanner.nextInt();
            if (userAns == 1) {
                new MenuItemsConsoleImpl(fileName).ansIsOne();
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
                new MenuItemsConsoleImpl(fileName).ansIsFive();
            }
            else if (userAns == 0) {
                userMenu.exitProgram(fileName);
            }
            else {
                userMenu.showMenu();
            }
        }
    }
    
    private void ansIsFive() {
        System.out.println("Enter object ID: ");
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLong()) {
                long objID = scanner.nextLong();
                MakeConvert makeConvert = new ConverterImpl(fileName);
                String itemsByID = makeConvert.getObjectItemsByID(objID);
                System.out.println(itemsByID);
                new MenuConsoleLocal(fileName).showMenu();
            }
        }
    }
}
