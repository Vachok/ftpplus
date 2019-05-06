package ru.vachok.ostpst;


import com.pff.*;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Vector;


/**
 @since 06.05.2019 (12:19) */
public class ParserContacts implements Runnable {
    
    
    private final MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private PSTFile pstFile;
    
    public ParserContacts(String fileName) {
        try {
            this.pstFile = new PSTFile(fileName);
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    @Override public void run() {
        Path startPath = Paths.get("");
        String pathFldrString = startPath.toAbsolutePath() + "\\obj\\";
        try (OutputStream outputStream = new FileOutputStream(pathFldrString + "contacts.csv");
        PrintStream printStream = new PrintStream(outputStream)) {
            printStream
                .println(new String("Обращение,\"Имя\",\"Отчество\",\"Фамилия\",\"Суффикс\",\"Организация\",\"Отдел\",\"Должность\",\"Улица (раб. адрес)\",\"Улица 2 (раб. адрес)\",\"Улица 3 (раб. адрес)\",\"Город (раб. адрес)\",\"Область (раб. адрес)\",\"Индекс (раб. адрес)\",\"Страна или регион (раб. адрес)\",\"Улица (дом. адрес)\",\"Улица 2 (дом. адрес)\",\"Улица 3 (дом. адрес)\",\"Город (дом. адрес)\",\"Область (дом. адрес)\",\"Почтовый код (дом.)\",\"Страна или регион (дом. адрес)\",\"Улица (другой адрес)\",\"Улица  2 (другой адрес)\",\"Улица  3 (другой адрес)\",\"Город  (другой адрес)\",\"Область  (другой адрес)\",\"Индекс  (другой адрес)\",\"Страна или регион  (другой адрес)\",\"Телефон помощника\",\"Рабочий факс\",\"Рабочий телефон\",\"Телефон раб. 2\",\"Обратный вызов\",\"Телефон в машине\",\"Основной телефон организации\",\"Домашний факс\",\"Домашний телефон\",\"Телефон дом. 2\",\"ISDN\",\"Телефон переносной\",\"Другой факс\",\"Другой телефон\",\"Пейджер\",\"Основной телефон\",\"Радиотелефон\",\"Телетайп/телефон с титрами\",\"Телекс\",\"Важность\",\"Веб-страница\",\"Годовщина\",\"День рождения\",\"Дети\",\"Заметки\",\"Имя помощника\",\"Инициалы\",\"Категории\",\"Ключевые слова\",\"Код организации\",\"Личный код\",\"Отложено\",\"Пол\",\"Пользователь 1\",\"Пользователь 2\",\"Пользователь 3\",\"Пользователь 4\",\"Пометка\",\"Почтовый ящик (дом. адрес)\",\"Почтовый ящик (другой адрес)\",\"Почтовый ящик (раб. адрес)\",\"Профессия\",\"Расположение\",\"Расположение комнаты\",\"Расстояние\",\"Руководитель\",\"Сведения о доступности в Интернете\",\"Сервер каталогов\",\"Супруг(а)\",\"Счет\",\"Счета\",\"Хобби\",\"Частное\",\"Адрес эл. почты\",\"Тип эл. почты\",\"Краткое имя эл. почты\",\"Адрес 2 эл. почты\",\"Тип 2 эл. почты\",\"Краткое 2 имя эл. почты\",\"Адрес 3 эл. почты\",\"Тип 3 эл. почты\",\"Краткое 3 имя эл. почты\",\"Язык\"".getBytes(), Charset
                .forName("UTF-8")));
            PSTFolder rootFolder = pstFile.getRootFolder();
            contactsParse(rootFolder, printStream);
        }
        catch (PSTException | IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    void contactsParse(PSTFolder pstFolder, final PrintStream printStream) throws PSTException, IOException {
        Vector<PSTFolder> folders = pstFolder.getSubFolders();
        Iterator<PSTFolder> pstFolderIterator = folders.iterator();
        while (pstFolderIterator.hasNext()) {
            PSTFolder folder = pstFolderIterator.next();
            if (folder.hasSubfolders()) {
                this.contactsParse(folder, printStream);
            }
            if(folder.getDisplayName().toLowerCase().contains("контакт")) {
                Vector<PSTObject> folderChildren = folder.getChildren(folder.getContentCount());
                Iterator<PSTObject> pstObjectIterator = folderChildren.iterator();
                while (pstObjectIterator.hasNext()){
                    PSTContact pstContact = (PSTContact) pstObjectIterator.next();
                    printStream.print("\"\",\"");
                    printStream.print(pstContact.getDisplayName()+"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",,,\"\",\"\",\"\",\"\",\"\",,,\"\",\"\",\"\",\"\",\"\",,,\"\",\"\",\"\",\"\",\"\",,\"\",\"\",\"\",\"\",\"\",,\"\",\"\",\"\",\"\",,\"\",\"\",\"\",\"\",\"\",\"\",\"Обычная\",\"\",\"0.0.00\",\"0.0.00\",,,\"\",\"\",,,,,,\"Не определен\",,,,,\"Обычная\",,,,,,\"\",,\"\",,,,,,,\"Ложь\",\""+pstContact.getEmail1EmailAddress()+"\",");
                    printStream.print("\"SMTP\",");
                    printStream.print("\""+pstContact.getEmail1EmailAddress()+"\"");
                    printStream.println();
                }
            }
        }
        ;
    }
}
