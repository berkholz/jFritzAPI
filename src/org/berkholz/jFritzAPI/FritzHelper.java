/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.berkholz.jFritzAPI;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
     * @param encoding The encoding which shall be used for the generation,
     * e.g. ISO-8859-1, UTF-8, UTF-16, UTF-16LE etc.
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
     * @param encoding The encoding which shall be used for the generation,
     * e.g. ISO-8859-1, UTF-8, UTF-16, UTF-16LE etc.
     * @return The MD5 sum of the given text.
     */
    public static String md5sum(String text, String encoding) {
        return hash("MD5", text, encoding);
    }

    /**
     * Generate a hash sum of a text with the specified hash algorithm and
     * encoding.
     *
     * @param text The text which shall be hashed.
     * @param encoding The encoding which shall be used for the generation,
     * e.g. ISO-8859-1, UTF-8, UTF-16, UTF-16LE etc.
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
     * @return The response value genereated like this: challenge + "-" + MD5(challenge + "-" + convertCodePointsInPassword(password)
     */
    public static String getResponse(String challenge, String password) {
        return (challenge + "-" + FritzHelper.md5sum(challenge + "-" + FritzHelper.convertCodePointsInPassword(password), "UTF-16LE"));
    }
}
