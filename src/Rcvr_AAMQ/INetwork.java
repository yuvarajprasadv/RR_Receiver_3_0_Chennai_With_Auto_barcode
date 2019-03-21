package Rcvr_AAMQ;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class INetwork 
{
	 public String GetClientIPAddr()
	 {
	        try {
	            InetAddress ipAddr = InetAddress.getLocalHost();
	            return (ipAddr.getHostAddress());
	        } catch (UnknownHostException ex) {
	            ex.printStackTrace();
	        }
			return null;
	  }
	 public static void main(String[] args)
	 {
		 INetwork nt = new INetwork();
		 System.out.println(nt.GetClientIPAddr());
	 }

}