package ru.vachok.money.other;


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stax Stream Processor
 * Read all tag names and its text values if exists.
 * Parse xml files by level numbers or by elements names.
 *
 * @Author: D. Petrov
 * @Copyright: Motun Ltd. and Co.
 */
public class StaxStreamProcessor implements AutoCloseable {

    private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

    private final XMLStreamReader reader;

    public StaxStreamProcessor(InputStream is) throws XMLStreamException {
        reader = FACTORY.createXMLStreamReader(is);
    }

    /**
     * Do the parsing until we reached the ending event.
     *
     * @param stopEvent
     * @param value
     * @return
     * @throws XMLStreamException
     */
    public boolean doUntil(int stopEvent, String value) throws XMLStreamException {

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == stopEvent && value.equals(reader.getLocalName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Parse an xml content and build a map of nodes
     *
     * @return
     * @throws XMLStreamException
     */
    public Map<Integer, XmlNode> buildXmlElementsTree() throws XMLStreamException {

        Map<Integer, XmlNode> elementsTree = new TreeMap<>();
        int counter = 0;

        while (reader.hasNext()) {
            int event = reader.next();
            counter++;
            XmlNode node = new XmlNode();
            node.setEvent(counter);
            node.setLevel(event);
            String name = "NoName";
            String value = "";
            if (event == XMLEvent.START_ELEMENT) {
                name = reader.getLocalName();
                if (reader.hasText()) {
                    value = reader.getElementText();
                }
            }
            node.setName(name);
            node.setValue(value);
            elementsTree.put(counter, node);
        }

        return elementsTree;
    }

    /**
     * Read all tags as a list
     *
     * @return
     * @throws XMLStreamException
     */
    public List<String> readAllTagNames() throws XMLStreamException {

        List<String> tagNames = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLEvent.START_ELEMENT) {
                tagNames.add(reader.getLocalName());
            }
        }

        return tagNames;
    }

//    /**
//     * We can read the tags with the text values.
//     * @return
//     * @throws XMLStreamException
//     */
//    public Map<String, XmlNode> readAllTagValues() {
//
//        Map<String, XmlNode> tagsTree = new TreeMap<>();
//        int current = 0;
//        int parent = 0;
//
//        while (true) {
//            int event;
//            XmlNode node = new XmlNode();
//            current++;
//            if (current > 1) {
//                parent = current - 1;
//            }
//            String nameOfTag = current + "-" + parent;
//            String textValue = "";
//            try {
//                if (reader.hasNext()) {
//                    event = reader.next();
//                    if (event == XMLEvent.START_ELEMENT) {
//                        nameOfTag = readTagName();
//                        textValue = readText();
//                    } else if (event == XMLEvent.END_ELEMENT) {
//                        current = 0;
//                        parent = 0;
//                    }
//                } else {
//                    break;
//                }
//            } catch (XMLStreamException xmlEx) {
//                continue;
//            }
//            node.setCurrent(current);
//            node.setParent(parent);
//            node.setName(nameOfTag);
//            node.setValue(textValue);
//            tagsTree.put(nameOfTag, node);
//        }
//
//        return tagsTree;
//    }

    /**
     * Do the parsing until we reached the end of the parent tag.
     *
     * @param element
     * @param parent
     * @return
     * @throws XMLStreamException
     */
    public boolean doUntilEndReached(String element, String parent) throws XMLStreamException {

        while (reader.hasNext()) {
            int event = reader.next();
            if (parent != null && event == XMLEvent.END_ELEMENT
                && parent.equals(reader.getLocalName())) {
                return false;
            }
            if (event == XMLEvent.START_ELEMENT && element.equals(reader.getLocalName())) {
                return true;
            }
        }

        return false;
    }

    public String readTagName() {
        return reader.getLocalName();
    }

    /**
     * Get the attribute
     *
     * @param name
     * @return
     */
    public String readAttribute(String name) {
        return reader.getAttributeValue(null, name);
    }


    /**
     * Get the text of the element
     *
     * @return
     * @throws XMLStreamException
     */
    public String readText() throws XMLStreamException {
        return reader.getElementText();
    }

    @Override
    public void close() throws Exception {
        if (reader != null) {
            reader.close();
        }
    }

}