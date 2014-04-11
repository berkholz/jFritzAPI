/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.berkholz.jFritzAPI;

/**
 *
 * @author Marcel Berkholz
 */
public class Main {

    /**
     * @param args the command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO: get username and password from properties file
        FritzConnection fc = new FritzConnection("user", "password");

        fc.setTargetSite("http://fritz.box/login_sid.lua");
        fc.connect();
        fc.test();
        /*
         fc.initConnection();
         fc.initSid();

         System.out.println("challenge: " + fc.getChallenge());
         System.out.println("response: " + FritzHelper.getResponse(fc.getChallenge(), fc.getPassword()));
         System.out.println("sid: " + fc.getSid());
         fc.reloadSid();
         System.out.println("sid: " + fc.getSid());
         */
        fc.disconnect();
    }

}
