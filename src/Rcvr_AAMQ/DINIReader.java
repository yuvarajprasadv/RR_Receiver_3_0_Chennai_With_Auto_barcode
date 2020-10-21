package Rcvr_AAMQ;

import org.apache.log4j.Logger;
import org.ini4j.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;




public class DINIReader 
{
	static Logger log = DLogMQ.monitor("Rcvr_AAMQ.DINIReader");
	DUtils utl = new DUtils();
    Path file = Paths.get(utl.ConvertToAbsolutePath("/Applications/Adobe Illustrator "+ MessageQueue.VERSION +"/Plug-ins.localized/Sgk/Configuration/RoadRunner.ini"));
    
    
    
    File fileName = file.toFile();
    
    
    

	
    public void readRRINI() throws Exception
    {
		try
		{
		    DUtils utils = new DUtils();
		    Ini ini = new Ini(new FileReader(utils.ConvertToAbsolutePath("RR.ini").toString()));
		  
    			String temp = ini.get("RR_WAVE").fetch("CATEGORY");
    			MessageQueue.category = temp;
		    		
		}
		catch(Exception ex)
		{
			log.error(MessageQueue.WORK_ORDER + ": " + "Error reading INI file - " + ex.getMessage());
		}
    }
    
    
    public  void readIniForSingle() throws Exception
    {
  
    	try
    	{
        Ini ini = new Ini(new FileReader(fileName.toString()));
        for (String key : ini.get("pdf_config_single").keySet())
        {
        		if (key.toString().equals("pdf_normal"))
        		{
        			String temp = ini.get("pdf_config_single").fetch(key);
        			MessageQueue.sPdfNormal = Boolean.valueOf(temp);
        		}
        		if(key.toString().equals("pdf_preset"))
        		{
        			String temp = ini.get("pdf_config_single").fetch(key);
        			MessageQueue.sPdfPreset  = Boolean.valueOf(temp);
        		}
        		if(key.toString().equals("pdf_normalized"))
        		{
        			String temp = ini.get("pdf_config_single").fetch(key);
        			MessageQueue.sPdfNormalised = Boolean.valueOf(temp);
        		}
        }
    	}
    	catch(Exception ex)
    	{
    		log.error(MessageQueue.WORK_ORDER + ": " + "Error reading INI file :" + ex.getMessage());
    	}
    }
    
    
    public  void readIniForMultiple() throws Exception
    {
  
    		try
    		{
        Ini ini = new Ini(new FileReader(fileName.toString()));
        for (String key : ini.get("pdf_config_multiple").keySet())
        {
        		if (key.toString().equals("pdf_normal"))
        		{
        			String temp = ini.get("pdf_config_multiple").fetch(key);
        			MessageQueue.mPdfNormal = Boolean.valueOf(temp);
        		}
        		if(key.toString().equals("pdf_preset"))
        		{
        			String temp = ini.get("pdf_config_multiple").fetch(key);
        			MessageQueue.mPdfPreset  = Boolean.valueOf(temp);
        		}
        		if(key.toString().equals("pdf_normalized"))
        		{
        			String temp = ini.get("pdf_config_multiple").fetch(key);
        			MessageQueue.mPdfNormalised = Boolean.valueOf(temp);
        		}
        }
    		}
    		catch(Exception ex)
    		{
    			log.error(MessageQueue.WORK_ORDER + ": " + "Error reading INI file - " + ex.getMessage());
    		}
    }
    
    
    public void writeValueforKey(String tornadoCurrentURL) throws IOException
    {
   	 try
   	 {
   		Wini wini = new Wini(new File(fileName.toString()));
        wini.put("torndoconfig", "TORNADO_SERVER_IP", tornadoCurrentURL);
        wini.store();
   	 }
   	 catch(Exception e)
   	 {
   		System.out.println("ERR writing INI  " + e.getMessage());
   		log.error(MessageQueue.WORK_ORDER + ": " + "Error Writing INI file :" + e.getMessage());
   	 }		
    }
    
    
    public static void main(String[] args) throws Exception
    {
     	MessageQueue.VERSION = "CC 2017";
    	
    	 	DINIReader iniRdr = new DINIReader();
    	 //	iniRdr.readIniForMultiple();
    	 	iniRdr.writeValueforKey("");
    	 

    	 
    	 
    	 
    }
}