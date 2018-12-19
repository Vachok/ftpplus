package ru.vachok.networker.config.fileworks;


/**
 Операции копирования
 <p>

 @since 19.12.2018 (10:27) */
class FilesCP extends FileSystemWorker implements Runnable {

    @Override
    public void run() {
        LOGGER.info("FilesCP.run");
        cpConstTxt();
    }
}
