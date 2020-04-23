package Rcvr_AAMQ;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

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
	 

	public String GetClientIPAddrs()
	{
		try
		{
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
	    for (NetworkInterface netint : Collections.list(nets))
	    {
	        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
	        if(netint.getDisplayName().equals("en0"))
	        for (InetAddress inetAddress : Collections.list(inetAddresses)) 
	        {
	        		if((inetAddress.toString().split("\\.")).length ==  4 )
	            return inetAddress.getHostAddress();
	        }
	     }
		return null;
		}
		catch (Exception SocketException)
		{
			return null;
		}
	}

	 
	 
	 public static void main(String[] args)
	 {
		 INetwork nt = new INetwork();
		 System.out.println(nt.GetClientIPAddrs());
	 }

}