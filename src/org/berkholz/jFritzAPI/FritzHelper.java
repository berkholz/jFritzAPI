/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.berkholz.jFritzAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * Class with helper methods for connecting to the FRITZ!Box.
 *
 * @author Marcel Berkholz
 */
public class FritzHelper {

    /**
     * CodePoints with a value greater than 255 in the password are converted to
     * '.' representation.
     *
     * @param password The password that shall be converted.
     * @return The String representation of the converted password.
     */
    public static String convertCodePointsInPassword(String password) {
        StringBuilder sb = new StringBuilder();

        // if codepoint of characte at i > 255 replace with '.' otherwise no replacment is done
        for (int i = 0; i < password.length(); sb.append(password.codePointAt(i) > 255 ? '.' : password.charAt(i)), i++);
        return sb.toString();
    }

    /**
     * Wrapper method to generate the SHA1 hash sum with the specified encoding.
     *
     * @param text The text which shall be hashed.
     * @param encoding The encoding which shall be used for the generation, e.g.
     * ISO-8859-1, UTF-8, UTF-16, UTF-16LE etc.
     * @return The SHA1 sum of the given text.
     */
    public static String sha1sum(String text, String encoding) {
        return hash("SHA1", text, encoding);
    }

    /**
     * Wrapper method to generate the MD5 hash sum with the specified encoding.
     * MD5 with UTF-16LE encooding is normaly used for the FritzBox response.
     *
     * @param text The text which shall be hashed.
     * @param encoding The encoding which shall be used for the generation, e.g.
     * ISO-8859-1, UTF-8, UTF-16, UTF-16LE etc.
     * @return The MD5 sum of the given text.
     */
    public static String md5sum(String text, String encoding) {
        return hash("MD5", text, encoding);
    }

    /**
     * Generate a hash sum of a text with the specified hash algorithm and
     * encoding.
     *
     * @param hashAlgorithm The hash algorithm to use. Possible algorithms:
     * SHA1, MD5.
     * @param text The text which shall be hashed.
     * @param encoding The encoding which shall be used for the generation, e.g.
     * ISO-8859-1, UTF-8, UTF-16, UTF-16LE etc.
     * @return The hash sum with the specified hash algorithm of the given text.
     */
    public static String hash(String hashAlgorithm, String text, String encoding) {
        byte[] bytesOfMessage = null;
        MessageDigest algorithm = null;

        String resultString;

        try {
            bytesOfMessage = text.getBytes(encoding);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FritzHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            algorithm = MessageDigest.getInstance(hashAlgorithm);
            algorithm.reset();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(FritzHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        algorithm.update(bytesOfMessage);
        byte messageDigest[] = algorithm.digest();
        byte convertedMessageDigest[] = new byte[messageDigest.length];

        System.arraycopy(messageDigest, 0, convertedMessageDigest, 0, messageDigest.length);

        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < convertedMessageDigest.length; i++) {
            String hex = Integer.toHexString(0xFF & convertedMessageDigest[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        resultString = hexString.toString();

        return resultString;

    }

    /**
     * Print a Document object to System.out.
     *
     * @param doc Document which shall be printed to System.out.
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public static void printDocument(Document doc) throws TransformerConfigurationException, TransformerException {
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(System.out));
    }

    /**
     * Generates the response for authenticating against the FRITZ!Box.
     *
     * @param challenge Challenge value from the login page.
     * @param password Password from the user that shall be authenticated.
     * @return The response value genereated like this: challenge + "-" +
     * MD5(challenge + "-" + convertCodePointsInPassword(password)
     */
    public static String getResponse(String challenge, String password) {
        return (challenge + "-" + FritzHelper.md5sum(challenge + "-" + FritzHelper.convertCodePointsInPassword(password), "UTF-16LE"));
    }

    /**
     * Returns out the document (org.w3c.dom.Document) as String representation.
     *
     * @param doc Document to get as String.
     * @return String represenetation of the document.
     */
    public static String documentToString(Document doc) {
        try {
            StringWriter writer = new StringWriter();
            Result result = new javax.xml.transform.stream.StreamResult(writer);
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), result);
            return writer.toString();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(FritzHelper.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } catch (TransformerException ex) {
            Logger.getLogger(FritzHelper.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    /**
     * Get the content (HTML site) of an URL. The query string defines only the
     * part without host name and host port. The sid will be appended by the
     * method. If you want to trigger the site
     * http://fritz.box/net/network_user_devices.lua?sid=0000000000000001 you
     * have to define the query string "net/network_user_devices.lua".
     *
     * @param queryString The querystring without hostname, host port and the
     * parameter sid.
     * @param fc
     * @return The content of the URL (HTML site) as StringBuffer.
     */
    public static StringBuffer getContent(String queryString, FritzConnection fc) {
        try {
            URL url = new URL(fc.getBaseURLofFritzBox() + queryString + "?sid=" + fc.getSid());
            Logger.getLogger(FritzConnection.class.getName()).log(Level.INFO, "Opening connection to URL: {0}", url.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            int responseCode = urlConnection.getResponseCode();
            Logger.getLogger(FritzConnection.class.getName()).log(Level.INFO, "Response code of URL: {0}", responseCode);
            StringBuffer response;
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response;
        } catch (ProtocolException ex) {
            Logger.getLogger(FritzConnection.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } catch (IOException ex) {
            Logger.getLogger(FritzConnection.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return null;
    }

}
