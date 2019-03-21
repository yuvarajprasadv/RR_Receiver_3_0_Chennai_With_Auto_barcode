package Rcvr_AAMQ;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.print.attribute.standard.PDLOverrideSupported;



public class INIReader 
{
	
	Utils utl = new Utils();
    Path file = Paths.get(utl.ConvertToAbsolutePath("/Applications/Adobe Illustrator "+ MessageQueue.VERSION +"/Plug-ins.localized/Sgk/Configuration/RoadRunner.ini"));
    File fileName = file.toFile();
    
    
    public  void readIniForSingle() throws Exception
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
    
    
    public  void readIniForMultiple() throws Exception
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
    
    
    public static void main(String[] args) throws Exception
    {
    	 INIReader iniRdr = new INIReader();
    	 iniRdr.readIniForMultiple();
    }
}