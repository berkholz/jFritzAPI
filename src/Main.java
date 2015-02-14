/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import org.berkholz.jFritzAPI.FritzConnection;
import org.berkholz.jFritzAPI.FritzInfos.FritzSystem;

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
        FritzConnection fc = new FritzConnection("user", "password", "http://fritz.box/");

        // connect to your fritzbox and get a valid sid
        fc.connect();

        // print out the HTML site of URL http://fritz.box/system/boxuser_list.lua
        System.out.println("networkuserdevices: " + FritzSystem.getBoxuser(fc));

        fc.disconnect();
    }

}
