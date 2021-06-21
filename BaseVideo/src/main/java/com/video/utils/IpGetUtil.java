package com.video.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * @CreeatBy LSQ
 * @CreateTime 2020/9/12 3:09 PM
 */
public class IpGetUtil {
    private static final String WIFISSID_UNKNOW = "<unknown ssid>";
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                //                try {
                //                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                //                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                //                        NetworkInterface intf = en.nextElement();
                //                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                //                            InetAddress inetAddress = enumIpAddr.nextElement();
                //                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                //                                return inetAddress.getHostAddress();
                //                            }
                //                        }
                //                    }
                //                } catch (SocketException e) {
                //                    e.printStackTrace();
                //                }
                Toast.makeText(context,"请连接wifi后再尝试投屏操作",Toast.LENGTH_LONG).show();
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
            Toast.makeText(context,"当前无网络连接,请在设置中打开网络",Toast.LENGTH_LONG).show();
        }
        return null;
    }
    public static String getWifiName(Context context) {
        /*
         *  先通过 WifiInfo.getSSID() 来获取
         */
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String wifiId = info != null ? info.getSSID() : null;
        String result = wifiId != null ? wifiId.trim() : null;
        if (!TextUtils.isEmpty(result)) {
            // 部分机型上获取的 ssid 可能会带有 引号
            if (result.charAt(0) == '"' && result.charAt(result.length() - 1) == '"') {
                result = result.substring(1, result.length() - 1);
            }
        }
        // 如果上面通过 WifiInfo.getSSID() 来获取到的是 空或者 <unknown ssid>，则使用 networkInfo.getExtraInfo 获取
        if (TextUtils.isEmpty(result) || WIFISSID_UNKNOW.equalsIgnoreCase(result.trim())) {
            NetworkInfo networkInfo = getNetworkInfo(context);
            if (networkInfo.isConnected()) {
                if (networkInfo.getExtraInfo() != null){
                    result = networkInfo.getExtraInfo().replace("\"","");
                }
            }
        }
        // 如果获取到的还是 空或者 <unknown ssid>，则遍历 wifi 列表来获取
        if (TextUtils.isEmpty(result) || WIFISSID_UNKNOW.equalsIgnoreCase(result.trim())) {
            result = getSSIDByNetworkId(context);
        }
        return result;
    }
    private static NetworkInfo getNetworkInfo(Context context){
        try{
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null != connectivityManager){
                return connectivityManager.getActiveNetworkInfo();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /*
     *  遍历wifi列表来获取
     */
    private static String getSSIDByNetworkId(Context context) {
        String ssid = WIFISSID_UNKNOW;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int networkId = wifiInfo.getNetworkId();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration : configuredNetworks){
                if (wifiConfiguration.networkId == networkId){
                    ssid = wifiConfiguration.SSID;
                    break;
                }
            }
        }
        return ssid;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
}
