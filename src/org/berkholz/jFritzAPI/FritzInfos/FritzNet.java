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
public class FritzNet {

    public static Object getNetworkUserDevices(FritzConnection fritzConnection) {
        StringBuffer sb = FritzHelper.getContent("net/network_user_devices.lua", fritzConnection);
        return sb;
    }

    public static Object getFritzName(FritzConnection fritzConnection) {
        StringBuffer sb = FritzHelper.getContent("net/fritz_name.lua", fritzConnection);
        return sb;
    }

    public static Object getHomeAutoOverview(FritzConnection fritzConnection) {
        StringBuffer sb = FritzHelper.getContent("net/home_auto_overview.lua", fritzConnection);
        return sb;
    }
}
