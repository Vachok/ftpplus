package ru.vachok.ostpst.fileworks;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.utils.FileSystemWorker;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.Callable;


/**
 @since 06.05.2019 (12:19) */
class ParserContacts implements Callable<String> {
    
    
    private final MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private String fileName;
    
    private PSTFile pstFile;
    
    private int strCounter;
    
    private String fileContactsName;
    
    ParserContacts(PSTFile pstFile) {
        this.pstFile = pstFile;
    }
    
    ParserContacts(String fileName) {
        this.fileName = fileName;
        this.fileContactsName = "showContacts";
    }
    
    ParserContacts(String fileName, String fileContactsName) {
        try {
            new File(fileName).setWritable(true);
            this.pstFile = new PSTFile(fileName);
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
        }
        this.fileContactsName = fileContactsName;
    }
    
    @Override public String call() {
        if (fileContactsName.isEmpty()) {
            this.fileContactsName = "contacts.csv";
            return getPathAndWriteHeaderToDisk();
        }
        else if (fileContactsName.equals("showContacts")) {
            return showContacts();
        }
        else {
            return getPathAndWriteHeaderToDisk();
        }
    }
    
    private String showContacts() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            this.pstFile = new PSTFile(fileName);
            ParserFoldersWithAttachments rootFolder = new ParserFoldersWithAttachments(pstFile);
            Deque<String> folderNamesAndWriteToDisk = rootFolder.getDeqFolderNamesWithIDAndWriteToDisk();
            for (String s : folderNamesAndWriteToDisk) {
                if (s.toLowerCase().contains(new String("контак".getBytes(), Charset.forName("windows-1251")))) {
                    s = s.split("id ")[1];
                    long parseLong = Long.parseLong(s);
                    PSTFolder pstFolder = (PSTFolder) PSTObject.detectAndLoadPSTObject(pstFile, parseLong);
                    stringBuilder.append(folderRead(pstFolder));
                }
            }
        }
        catch (PSTException | IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".showContacts", e));
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    private String folderRead(PSTFolder folder) throws PSTException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Vector<PSTObject> folderChildren = folder.getChildren(folder.getContentCount());
        
        return stringBuilder.toString();
    }
    
    private void foldersRead(PSTFolder pstFolder, PrintStream printStream) throws IOException {
        Vector<PSTFolder> folders = null;
        try {
            folders = pstFolder.getSubFolders();
        }
        catch (PSTException e) {
            messageToUser.error(e.getMessage());
        }
        
        Iterator<PSTFolder> pstFolderIterator = folders.iterator();
        
        while (pstFolderIterator.hasNext()) {
            PSTFolder folder = pstFolderIterator.next();
            boolean nameContacts = folder.getDisplayName().toLowerCase().contains("ontacts")
                || folder.getFolderType() == 2
                || folder.getDisplayName().toLowerCase().contains(new String("онтакт".getBytes(), Charset.forName("windows-1251")));
            boolean hasSubs = folder.hasSubfolders();
    
            System.out.println("folder = " + folder.getDisplayName() + " has no contacts.");
            if (hasSubs && !nameContacts) {
                foldersRead(folder, printStream);
            }
            if (nameContacts) {
                try {
                    writeContactsToFile(folder, printStream);
                }
                catch (PSTException e) {
                    messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".foldersRead", e));
                }
            }
        }
    }
    
    private void writeContactsToFile(PSTFolder folder, PrintStream printStream) throws IOException, PSTException {
        Vector<PSTObject> folderChildren = new Vector<>();
        long objID = 0;
        try {
            int folderContentCount = folder.getContentCount();
            folderChildren = folder.getChildren(folderContentCount);
        }
        catch (PSTException e) {
            messageToUser.error(e.getMessage());
            objID = folder.getDescriptorNodeId();
        }
        
        Iterator<PSTObject> pstObjectIterator = folderChildren.iterator();
        
        if (objID != 0) {
            PSTObject object = PSTObject.detectAndLoadPSTObject(pstFile, objID);
            ParserObjects parserObjects = new ParserObjects(object);
        }
        else {
            while (pstObjectIterator.hasNext()) {
                writeContact(pstObjectIterator, printStream);
            }
        }
    }
    
    private void writeContact(Iterator<PSTObject> pstObjectIterator, PrintStream printStream) {
        PSTContact pstContact = (PSTContact) pstObjectIterator.next();
        printStream.print("\"\",\"");
        printStream.print(pstContact
            .getDisplayName() + "\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",,,\"\",\"\",\"\",\"\",\"\",,,\"\",\"\",\"\",\"\",\"\",,,\"\",\"\",\"\",\"\",\"\",,\"\",\"\",\"\",\"\",\"\",,\"\",\"\",\"\",\"\",,\"\",\"\",\"\",\"\",\"\",\"\",\"Обычная\",\"\",\"0.0.00\",\"0.0.00\",,,\"\",\"\",,,,,,\"Не определен\",,,,,\"Обычная\",,,,,,\"\",,\"\",,,,,,,\"Ложь\",\"" + pstContact
            .getEmail1EmailAddress() + "\",");
        printStream.print("\"SMTP\",");
        printStream.print("\"" + pstContact.getEmail1EmailAddress() + "\"");
        printStream.println();
    }
    
    private void showContact(PSTContact pstContact) {
        strCounter++;
        System.out.println(strCounter + ") " + pstContact.getDisplayName() + " is " + pstContact.getEmail1EmailAddress());
    }
    
    private String getPathAndWriteHeaderToDisk() {
        PSTFolder rootFolder = null;
        try {
            rootFolder = pstFile.getRootFolder();
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
        }
        try (OutputStream outputStream = new FileOutputStream(fileContactsName);
             PrintStream printStream = new PrintStream(outputStream, true, "Windows-1251")
        ) {
            printStream
                .println("Обращение,\"Имя\",\"Отчество\",\"Фамилия\",\"Суффикс\",\"Организация\",\"Отдел\",\"Должность\",\"Улица (раб. адрес)\",\"Улица 2 (раб. адрес)\",\"Улица 3 (раб. адрес)\",\"Город (раб. адрес)\",\"Область (раб. адрес)\",\"Индекс (раб. адрес)\",\"Страна или регион (раб. адрес)\",\"Улица (дом. адрес)\",\"Улица 2 (дом. адрес)\",\"Улица 3 (дом. адрес)\",\"Город (дом. адрес)\",\"Область (дом. адрес)\",\"Почтовый код (дом.)\",\"Страна или регион (дом. адрес)\",\"Улица (другой адрес)\",\"Улица  2 (другой адрес)\",\"Улица  3 (другой адрес)\",\"Город  (другой адрес)\",\"Область  (другой адрес)\",\"Индекс  (другой адрес)\",\"Страна или регион  (другой адрес)\",\"Телефон помощника\",\"Рабочий факс\",\"Рабочий телефон\",\"Телефон раб. 2\",\"Обратный вызов\",\"Телефон в машине\",\"Основной телефон организации\",\"Домашний факс\",\"Домашний телефон\",\"Телефон дом. 2\",\"ISDN\",\"Телефон переносной\",\"Другой факс\",\"Другой телефон\",\"Пейджер\",\"Основной телефон\",\"Радиотелефон\",\"Телетайп/телефон с титрами\",\"Телекс\",\"Важность\",\"Веб-страница\",\"Годовщина\",\"День рождения\",\"Дети\",\"Заметки\",\"Имя помощника\",\"Инициалы\",\"Категории\",\"Ключевые слова\",\"Код организации\",\"Личный код\",\"Отложено\",\"Пол\",\"Пользователь 1\",\"Пользователь 2\",\"Пользователь 3\",\"Пользователь 4\",\"Пометка\",\"Почтовый ящик (дом. адрес)\",\"Почтовый ящик (другой адрес)\",\"Почтовый ящик (раб. адрес)\",\"Профессия\",\"Расположение\",\"Расположение комнаты\",\"Расстояние\",\"Руководитель\",\"Сведения о доступности в Интернете\",\"Сервер каталогов\",\"Супруг(а)\",\"Счет\",\"Счета\",\"Хобби\",\"Частное\",\"Адрес эл. почты\",\"Тип эл. почты\",\"Краткое имя эл. почты\",\"Адрес 2 эл. почты\",\"Тип 2 эл. почты\",\"Краткое 2 имя эл. почты\",\"Адрес 3 эл. почты\",\"Тип 3 эл. почты\",\"Краткое 3 имя эл. почты\",\"Язык\"");
            foldersRead(rootFolder, printStream);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        
        return Paths.get(fileContactsName).toAbsolutePath().toString();
    }
}
