package org.hdstar.util;

import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hdstar.model.RssChannel;
import org.hdstar.model.RssItem;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;

public class RssParser extends DefaultHandler {

    private String urlString;
    private RssChannel channel;
    private StringBuilder text;
    private RssItem item;
    private boolean imageStatus;

    /**
     * The constructor for the RSS parser; call {@link #parse()} to synchronously create an HTTP connection and parse
     * the RSS feed contents. The results can be retrieved with {@link #getChannel()}.
     * @param url
     */
    public RssParser(String url) {
            this.urlString = url;
            this.text = new StringBuilder();
    }

    /**
     * Returns the loaded RSS feed as channel which contains the individual {@link Item}s
     * @return A channel object that ocntains the feed details and individual items
     */
    public RssChannel getChannel() {
            return this.channel;
    }

    /**
     * Initialises an HTTP connection, retrieves the content and parses the RSS feed as standard XML.
     * @throws ParserConfigurationException Thrown if the SX parser is not working corectly
     * @throws SAXException Thrown if the SAX parser can encounters non-standard XML content
     * @throws IOException Thrown if the RSS feed content can not be retrieved, such as when no connection is available
     */
    public void parse() throws ParserConfigurationException, SAXException, IOException {

            HttpClient httpclient = HttpClientManager.getHttpClient();
            HttpResponse result = httpclient.execute(new HttpGet(urlString));
            SAXParserFactory spf = SAXParserFactory.newInstance();
            if (spf != null) {
                    SAXParser sp = spf.newSAXParser();
                    sp.parse(result.getEntity().getContent(), this);
            }

    }

    /**
     * By default creates a standard Item (with title, description and links), which may to overridden to add more data
     * (i.e. custom tags that a feed may supply).
     * @return A possibly decorated Item instance
     */
    protected RssItem createNewItem() {
            return new RssItem();
    }

    @Override
    public final void startElement(String uri, String localName, String qName, Attributes attributes) {

            /** First lets check for the channel */
            if (localName.equalsIgnoreCase("channel")) {
                    this.channel = new RssChannel();
            }

            /** Now lets check for an item */
            if (localName.equalsIgnoreCase("item") && (this.channel != null)) {
                    this.item = createNewItem();
                    this.channel.items.add(this.item);
            }

            /** Now lets check for an image */
            if (localName.equalsIgnoreCase("image") && (this.channel != null)) {
                    this.imageStatus = true;
            }

            /** Checking for a enclosure */
            if (localName.equalsIgnoreCase("enclosure")) {
                    /** Lets check we are in an item */
                    if (this.item != null && attributes != null && attributes.getLength() > 0) {
                            if (attributes.getValue("url") != null) {
                                    this.item.enclosureUrl = attributes.getValue("url").trim();
                            }
                            if (attributes.getValue("type") != null) {
                                    this.item.enclosureType = attributes.getValue("type");
                            }
                            if (attributes.getValue("length") != null) {
                                    this.item.enclosureLength = Long.parseLong(attributes.getValue("length"));
                            }
                    }
            }

    }

    /**
     * This is where we actually parse for the elements contents
     */
    @SuppressWarnings("deprecation")
    public final void endElement(String uri, String localName, String qName) {
            /** Check we have an RSS Feed */
            if (this.channel == null) {
                    return;
            }

            /** Check are at the end of an item */
            if (localName.equalsIgnoreCase("item")) {
                    this.item = null;
            }

            /** Check we are at the end of an image */
            if (localName.equalsIgnoreCase("image"))
                    this.imageStatus = false;

            /** Now we need to parse which title we are in */
            if (localName.equalsIgnoreCase("title")) {
                    /** We are an item, so we set the item title */
                    if (this.item != null) {
                            this.item.title = this.text.toString().trim();
                            /** We are in an image */
                    } else {
                            this.channel.title = this.text.toString().trim();
                    }
            }

            /** Now we are checking for a link */
            if (localName.equalsIgnoreCase("link")) {
                    /** Check we are in an item **/
                    if (this.item != null) {
                            this.item.link = this.text.toString().trim();
                            /** Check we are in an image */
                    } else if (this.imageStatus) {
                            this.channel.image = this.text.toString().trim();
                            /** Check we are in a channel */
                    } else {
                            this.channel.link = this.text.toString().trim();
                    }
            }

            /** Checking for a description */
            if (localName.equalsIgnoreCase("description")) {
                    /** Lets check we are in an item */
                    if (this.item != null) {
                            this.item.description = this.text.toString().trim();
                            /** Lets check we are in the channel */
                    } else {
                            this.channel.description = this.text.toString().trim();
                    }
            }

            /** Checking for a pubdate */
            if (localName.equalsIgnoreCase("pubDate")) {
                    /** Lets check we are in an item */
                    if (this.item != null) {
                            try {
                                    this.item.pubDate = new Date(Date.parse(this.text.toString().trim()));
                            } catch (Exception e) {
                                    // Date is malformed (not parsable by Date.parse)
                            }
                            /** Lets check we are in the channel */
                    } else {
                            try {
                                    this.channel.pubDate = new Date(Date.parse(this.text.toString().trim()));
                            } catch (Exception e) {
                                    // Date is malformed (not parsable by Date.parse)
                            }
                    }
            }

            /** Check for the category */
            if (localName.equalsIgnoreCase("category") && (this.item != null)) {
                    this.channel.categories.add(this.text.toString().trim());
            }

            addAdditionalData(localName, this.item, this.text.toString());

            this.text.setLength(0);
    }

    /**
     * May be overridden to add additional data from tags that are not standard in RSS. Not used by this default RSS
     * style parser. Usually used in conjunction with {@link #createNewItem()}.
     * @param localName The tag name
     * @param item The Item we are currently parsing
     * @param text The new text content
     */
    protected void addAdditionalData(String localName, RssItem item, String text) {
    }

    @Override
    public final void characters(char[] ch, int start, int length) {
            this.text.append(ch, start, length);
    }

}
