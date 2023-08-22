package com.tailf.jnc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A simple SAX parser, for parsing NETCONF messages, into a simple
 * {@link Element} tree.
 *
 */
public class XMLParser {

    /** the parser implementation */
    protected XMLReader parser;

    /**
     * Constructor. Initializes the parser instance.
     */
    public XMLParser() throws JNCException {
        try {
            final String javaVersion = System.getProperty("java.version");
            if (javaVersion.startsWith("1.4")) {
                parser = XMLReaderFactory
                        .createXMLReader("org.apache.crimson.parser.XMLReaderImpl");
            } else {
                parser = XMLReaderFactory.createXMLReader();
            }
        } catch (final Exception e) {
            throw (JNCException) new JNCException(JNCException.PARSER_ERROR,
                    "failed to initialize parser").initCause(e);
        }

    }

    public void setCapabilities(Capabilities capabilities) {
    }

    /**
     * The handler with hooks for startElement etc. The SAX parser will build
     * up the parse tree, by calling these hooks.
     */
    private class ConfHandler extends DefaultHandler {

        // pointer to current element (node)
        public Element current;
        public Element top;
        public PrefixMap prefixes;

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            final Element child = new Element(uri, localName);
            child.prefixes = prefixes;
            prefixes = null;

            // add other attributes
            for (int i = 0; i < attributes.getLength(); i++) {
                final String attrName = attributes.getLocalName(i);
                // String attrType= attributes.getType(i);
                final String attrUri = attributes.getURI(i);
                final String attrValue = attributes.getValue(i);
                final Attribute attr = new Attribute(attrUri, attrName,
                        attrValue);
                // System.out.println("ATTRIBUTE: "+attributes.getQName(i)+
                // "  URI="+attributes.getURI(i));
                trace("add attr: {}", attr);
                child.addAttr(attr);
            }
            if (current == null) {
                trace("add to top: {}", child);
                top = child;
            } else {
                current.addChild(child);
                trace("add child: {}", child);
            }
            current = child; // step down
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            // check that we don't have mixed content
            if (current.hasChildren() && current.value != null) {
                // MIXED content not allowed
                current.value = null;
            }
            // step up
            current = current.getParent();
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (current.value == null) {
                current.value = "";
            }
            current.value = current.value + new String(ch, start, length);
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) {
            trace("startPrefixMapping: uri=\"{}\" prefix={}", uri, prefix);
            if (prefixes == null) {
                prefixes = new PrefixMap();
            }
            prefixes.add(new Prefix(prefix, uri));
            trace("added prefixmapping: {}", prefix);
        }
    }

    /**
     * Read in an XML file and parse it and return an element tree.
     */
    public Element readFile(String filename) throws JNCException {
        try {
            final ConfHandler handler = new ConfHandler();
            parser.setContentHandler(handler);
            parser.parse(filename);
            return handler.top;
        } catch (final Exception e) {
            throw (JNCException) new JNCException(JNCException.PARSER_ERROR, "parse file: "
                    + filename).initCause(e);
        }
    }

    /**
     * Parses an XML string returning an element tree from it.
     *
     * @param is Inputsource (byte stream) where the XML text is read from
     * @param yangNsPackages
     */
    public Element parse(InputSource is, YangNsPackage... yangNsPackages) throws JNCException {
        try {
            final ConfHandler handler = new ConfHandler();
            parser.setContentHandler(handler);
            parser.parse(is);
            return handler.top;
        } catch (final Exception e) {
            throw (JNCException) new JNCException(JNCException.PARSER_ERROR,
                "parse error").initCause(e);
        }
    }

    /**
     * Parses an XML String, returning a Element tree representing the XML
     * structure.
     *
     * @param str String containing the XML text to parse
     * @param yangNsPackages
     */
    public Element parse(String str, YangNsPackage... yangNsPackages) throws JNCException {
        final ByteArrayInputStream istream = new ByteArrayInputStream(
                str.getBytes(StandardCharsets.UTF_8));
        final InputSource is = new InputSource(istream);
        return parse(is);
    }

    public Document parse2Doc(String str) throws ParserConfigurationException, IOException, SAXException {
        final ByteArrayInputStream istream = new ByteArrayInputStream(
                str.getBytes(StandardCharsets.UTF_8));
        final InputSource is = new InputSource(istream);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        return dBuilder.parse(is);
    }

    /**
     * trace
     */
    protected void trace(String format, Object ... args) {
        if (Element.debugLevel >= Element.DEBUG_LEVEL_PARSER) {
            System.err.println(String.format("*XMLParser: " + format, args));
        }
    }
}
