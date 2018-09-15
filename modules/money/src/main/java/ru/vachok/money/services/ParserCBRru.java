package ru.vachok.money.services;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.other.StaxStreamProcessor;
import ru.vachok.money.other.XmlNode;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;


/**
 <h1>Тянет курсы USD и EURO</h1>

 @since 12.08.2018 (16:12) */
@Service ("parsercb")
public class ParserCBRru {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ParserCBRru.class.getSimpleName();

    private static final Logger LOGGER = AppComponents.getLogger();

    private URL url = getUrl();

    private InputStream inputStream;

    private final String spec = "http://cbr.ru/currency_base/daily/";

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public static ParserCBRru getParser() {
        try{
            ParserCBRru parserCBRru = new ParserCBRru();
            parserCBRru.setInputStream(parserCBRru.getUrl().openStream());
            return parserCBRru;
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
            throw new RejectedExecutionException();
        }
    }

    private URL getUrl() {

        try{
            URL url = new URL(spec);
            return url;
        }
        catch(MalformedURLException e){
            LOGGER.info(e.getMessage(), e);
            throw new RejectedExecutionException();
        }
    }

    /*Instances*/
    private ParserCBRru() {
    }

    public String usdCur() {
        String usdToday = "USD Today";
        try{
            List<String> list = parseList();
        }
        catch(XMLStreamException e){
            LOGGER.error(e.getMessage(), e);
        }
        return usdToday;
    }

    private List<String> parseList() throws XMLStreamException {
        throw new IllegalAccessError();
    }

    public String parseTag(String table) {
        String retTag = "";
        try{
            StaxStreamProcessor staxStreamProcessor = new StaxStreamProcessor(getUrl().openStream());

            retTag = staxStreamProcessor.readAttribute(table);
        }
        catch(XMLStreamException | IOException e){
            LOGGER.error(e.getMessage(), e);
            return e.getMessage() + "\n" + new TForms().toStringFromArray(e);
        }
        return retTag;
    }

    public Map<Integer, XmlNode> getMap(String tagName) {
        try{
            StaxStreamProcessor staxStreamProcessor = new StaxStreamProcessor(getUrl().openStream());
            Map<Integer, XmlNode> integerXmlNodeMap = staxStreamProcessor.buildXmlElementsTree();
            if(integerXmlNodeMap.size() > 2){
                return integerXmlNodeMap;
            }
            else{
                throw new IllegalStateException();
            }
        }
        catch(XMLStreamException | IOException e){
            LOGGER.error(e.getMessage(), e);
            throw new RejectedExecutionException();
        }
    }

    public String euroCur() {
        return "No EURO";
    }

    private String parseCbr() {

        StringBuilder stringBuilder = new StringBuilder();
        try{
            url = new URL(spec);
            try(InputStream inputStream = url.openStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)){
                byte[] bytes = new byte[ConstantsFor.MEGABYTE];
                while(inputStream.available() > 0){
                    int read = bufferedInputStream.read(bytes);

                }
                stringBuilder.append(new String(bytes, StandardCharsets.UTF_8));
                return stringBuilder.toString();
            }
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        return "No Currency!";
    }
}