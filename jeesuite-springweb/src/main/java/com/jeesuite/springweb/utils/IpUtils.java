package com.jeesuite.springweb.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.jeesuite.common.http.HttpUtils;
import com.jeesuite.common.json.JsonUtils;
import com.jeesuite.springweb.WebConstants;

public class IpUtils {

	public static final String LOCAL_BACK_IP = "127.0.0.1";
	public static final String LOCAL_HOST = "localhost";
	private static final String UNKNOWN = "unknown";
	private static String localIp = getLocalIpAddr();

	public static boolean isInnerIp(String ipAddr){
		if(StringUtils.isBlank(ipAddr))return false;
		return ipAddr.startsWith("127")  || ipAddr.startsWith("192.168") || ipAddr.startsWith("10.");
	}
	 /**
     * <p>
     * 获取客户端的IP地址的方法是：request.getRemoteAddr()，这种方法在大部分情况下都是有效的。
     * 但是在通过了Apache,Squid等反向代理软件就不能获取到客户端的真实IP地址了，如果通过了多级反向代理的话，
     * X-Forwarded-For的值并不止一个，而是一串IP值， 究竟哪个才是真正的用户端的真实IP呢？
     * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     * 例如：X-Forwarded-For：192.168.1.110, 192.168.1.120,
     * 192.168.1.130, 192.168.1.100 用户真实IP为： 192.168.1.110
     * </p>
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
  
        String ip = request.getHeader(WebConstants.HEADER_FROWARDED_FOR);
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals(LOCAL_BACK_IP)) {
                /** 根据网卡取本机配置的IP */
                ip = getLocalIpAddr();
            }
        }
        /**
         * 对于通过多个代理的情况， 第一个IP为客户端真实IP,多个IP按照','分割 "***.***.***.***".length() =
         * 15
         */
        if (ip != null && ip.length() > 15) {
            int splitIndex = ip.indexOf(",");
			if (splitIndex > 0) {
                ip = ip.substring(0, splitIndex);
            }
        }
        return ip;
    }
    
    public static String getLocalIpAddr()  {  
		if(localIp != null)return localIp;
		try {			
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();  
			outter:while (en.hasMoreElements()) {  
				NetworkInterface i = en.nextElement();  
				for (Enumeration<InetAddress> en2 = i.getInetAddresses(); en2.hasMoreElements();) {  
					InetAddress addr = en2.nextElement();  
					if (!addr.isLoopbackAddress()) {  
						if (addr instanceof Inet4Address) {    
							localIp = addr.getHostAddress();  
							break outter;
						}  
					}  
				}  
			}  
		} catch (Exception e) {}
		
		if(localIp == null){
			InetAddress inet = null;
            try {
                inet = InetAddress.getLocalHost();
                localIp = inet.getHostAddress();
            } catch (UnknownHostException e) {}
		}
		
	    return localIp;  
	} 
    
    public static String getinvokerIpAddr(HttpServletRequest request)  { 
    	String ip = request.getHeader(WebConstants.HEADER_INVOKER_IP);
    	return ip;
    }
    
    public static String ipToLocation(String ip) {
    	if(isInnerIp(ip))return null;
		try {			
			String url = "http://ip.taobao.com/service/getIpInfo.php?ip="+ip;
			String content = HttpUtils.get(url).getBody();
			Map resp = JsonUtils.toObject(content, Map.class);
			if("0".equals(String.valueOf(resp.get("code")))){
				resp = (Map) resp.get("data");
				//String region = resp.get("region") == null ? "" : resp.get("region").toString();
				String city = resp.get("city") == null ? "" : resp.get("city").toString();
				return city;
			}
		} catch (Exception e) {}
		
		
		return null;
	}
    
    public static void main(String[] args) {
    	String location = ipToLocation("121.33.175.250");
		System.out.println(location);
	}
}
