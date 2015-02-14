/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.berkholz.jFritzAPI.FritzInfos;

import org.berkholz.jFritzAPI.FritzConnection;
import org.berkholz.jFritzAPI.FritzHelper;

/**
 *
 * @author Marcel Berkholz
 */
public class FritzSystem {

    public static Object getEnergy(FritzConnection fritzConnection) {

        StringBuffer sb = FritzHelper.getContent("system/energy.lua", fritzConnection);
        return sb;
    }

    public static StringBuffer getSyslog(FritzConnection fritzConnection) {
        StringBuffer sb = FritzHelper.getContent("system/syslog.lua", fritzConnection);
        return sb;
    }

    public static StringBuffer getBoxuser(FritzConnection fritzConnection) {
        StringBuffer sb = FritzHelper.getContent("system/boxuser_list.lua", fritzConnection);
        return sb;
    }
}
