package ru.vachok.ostpst.usermenu;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.utils.FileSystemWorker;

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
        System.out.println("2. Show folders");
        System.out.println("3. Write folder names to disk");
        System.out.println("0. Exit");
        System.out.println("Choose: ");
    }
    
    private static void askUser(Scanner scanner, String fileName) {
        new MenuItemsConsoleImpl(fileName).showSecondStage();
        while (scanner.hasNextInt()) {
            int userAns = scanner.nextInt();
            
            if (userAns == 1) {
                new MenuItemsConsoleImpl(fileName).ansIsOne();
            }
            else if (userAns == 2) {
                MakeConvert makeConvert = new ConverterImpl(fileName);
                makeConvert.showListFolders();
            }
            else if (userAns == 3) {
                MakeConvert makeConvert = new ConverterImpl(fileName);
                makeConvert.getDequeFolderNamesAndWriteToDisk();
            }
            else if (userAns == 0) {
                userMenu.exitProgram(fileName);
            }
            else {
                userMenu.showMenu();
            }
        }
    }
}
