package ru.vachok.networker.fileworks;


/**
 Операции копирования
 <p>

 @since 19.12.2018 (10:27) */
class FilesCP extends FileSystemWorker implements Runnable {

    @Override
    public void run() {
        LOGGER.info("FilesCP.run");
        cpConstTxt("G:\\My_Proj\\FtpClientPlus\\modules\\networker\\src\\main\\resources\\static\\texts\\const.txt");
    }
}
