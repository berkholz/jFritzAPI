/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.berkholz.jFritzAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Init a connection to a FRITZ!BOX.
 *
 * @author Marcel Berkholz
 */
public class FritzConnection {

    /**
     * Username to establish the connectin with.
     */
    String username;
    /**
     * Password of the username, which is required to establish a connectin to
     * the FritzBox.
     */
    String password;
    String challenge;
    String sid;
    String baseURLofFritzBox;
    HttpURLConnection urlConnection;

    /*
     * CONSTRUCTORS
     */
    /**
     * Create a FritzConnection and authenticate with the given username and
     * password.
     *
     * @param username Username of the Fritz!Box user which has rights to manage
     * settings of the Fritz!Box.
     * @param password Password of the Fritz!Box user.
     */
    public FritzConnection(String username, String password) {
        this.username = username;
        this.password = password;
        this.baseURLofFritzBox = "http://fritz.box/";
    }

    /**
     * Create a FritzConnection and authenticate with the given username and
     * password.
     *
     * @param username Username of the Fritz!Box user which has rights to manage
     * settings of the Fritz!Box.
     * @param password Password of the Fritz!Box user.
     * @param targetURL Base URL of the FritzBox, e.g. http://fritz.box/.
     */
    public FritzConnection(String username, String password, String targetURL) {
        this.username = username;
        this.password = password;
        this.baseURLofFritzBox = targetURL;
    }

    /*
     * GETTER AND SETTER
     */
    /**
     * Get the username of the FritzConnection.
     *
     * @return The username of the FritzConnection.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username which shall be used for the FritzConnection.
     *
     * @param username Username for the FritzConnection.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the password of the username of the FritzConnection.
     *
     * @return The password of the username of the FritzConnection.
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the challenge of the FritzConnection.
     *
     * @return The challenge of the FritzConnection.
     */
    public String getChallenge() {
        return challenge;
    }

    /**
     *
     * @param challenge
     */
    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    /**
     *
     * @return
     */
    public String getSid() {
        return sid;
    }

    /**
     *
     * @param sid
     */
    public void setSid(String sid) {
        this.sid = sid;
    }

    /**
     *
     * @return
     */
    public String getBaseURLofFritzBox() {
        return baseURLofFritzBox;
    }

    /**
     *
     * @param internetsite
     */
    public void setBaseURLofFritzBox(String internetsite) {
        this.baseURLofFritzBox = internetsite;
    }

    /* 
     * METHODS
     */
    /**
     * Initialize the HTTP connection to the FritzBox.
     */
    public void initConnection() {
        try {
            urlConnection = (HttpURLConnection) new URL(baseURLofFritzBox + "login_sid.lua").openConnection();
        } catch (IOException ex) {
            Logger.getLogger(FritzConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initialize the sid
     */
    public void initSid() {
        Document doc = null;

        // XPATH-element to parse over the xml document.
        XPath xpath = XPathFactory.newInstance().newXPath();
        Logger.getLogger(FritzConnection.class.getName()).log(Level.INFO, "Getting XPath instance to parse XML.");

        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(urlConnection.getInputStream());
            Logger.getLogger(FritzConnection.class.getName()).log(Level.INFO, "Saving XML content of URL in a Document Object: {0}", FritzHelper.documentToString(doc));
        } catch (ParserConfigurationException | IOException | SAXException ex) {
            Logger.getLogger(FritzConnection.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        try {
            // extract the sid from the xml document
            this.sid = (String) xpath.evaluate("//SID", doc, XPathConstants.STRING);
            Logger.getLogger(FritzConnection.class.getName()).log(Level.INFO, "Getting SID ({0}) via XPath", sid);

            // get the challange response
            this.challenge = (String) xpath.evaluate("//Challenge", doc, XPathConstants.STRING);
            Logger.getLogger(FritzConnection.class.getName()).log(Level.INFO, "Getting challenge ({0}) via XPath", challenge);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(FritzConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Check if the sid is valid.
     *
     * @return Returns true if the sid is valid, otherwise false.
     */
    public boolean isSidValid() {
        // if sid is 0000000000000000 we have in invalid sid.
        return !"0000000000000000".equals(this.sid);
    }

    /**
     * Get a new sid when old sid is invalid.
     *
     */
    public void reloadSid() {
        if (!isSidValid()) {
            String newUrl = baseURLofFritzBox + "login_sid.lua" + "?username=" + username + "&response=" + FritzHelper.getResponse(challenge, password);
            Logger.getLogger(FritzConnection.class.getName()).log(Level.INFO, "Setting URL: {0}", newUrl);

            try {
                Logger.getLogger(FritzConnection.class.getName()).log(Level.INFO, "Opening connection to URL: {0}", newUrl);
                urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();

                Logger.getLogger(FritzConnection.class.getName()).log(Level.INFO, "Saving XML content of URL in a Document Object.");
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(urlConnection.getInputStream());

                Logger.getLogger(FritzConnection.class.getName()).log(Level.INFO, "Getting XPath instance to parse XML.");
                XPath xpath = XPathFactory.newInstance().newXPath();

                sid = (String) xpath.evaluate("//SID", doc, XPathConstants.STRING);
                Logger.getLogger(FritzConnection.class.getName()).log(Level.INFO, "Getting SID ({0}) via XPath", sid);
            } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException ex) {
                Logger.getLogger(FritzConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *
     */
    public void connect() {
        initConnection();
        initSid();
        reloadSid();
    }



    /**
     *
     */
    public void test() {
        try {
            URL url = new URL("http://fritz.box:49000/tr64desc.xml");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("User-Agent", "AVM UPnP/1.0 Client 1.0");
            urlConnection.setRequestProperty("CONNECTION", "Close");
            int responseCode = urlConnection.getResponseCode();
            StringBuffer response;
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()))) {
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            //print result
            System.out.println(response.toString());

        } catch (IOException ex) {
            Logger.getLogger(FritzConnection.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Trigger a disconnect of the connection to the FritzBox!.
     *
     * @throws XPathExpressionException
     * @throws ParserConfigurationException
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     */
    public void disconnect() throws XPathExpressionException, ParserConfigurationException, MalformedURLException, IOException, SAXException, TransformerException {
        if (isSidValid()) {
            String newUrl = "http://fritz.box/home/home.lua?sid=" + this.sid + "&logout=1";
            Logger
                    .getLogger(FritzConnection.class
                            .getName()).log(Level.INFO, "Calling {0} to logout.", newUrl);

            urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();

            this.sid = null;

            urlConnection.disconnect();

            Logger.getLogger(FritzConnection.class
                    .getName()).log(Level.INFO, "Connection disconnected.");
        }
    }
}
