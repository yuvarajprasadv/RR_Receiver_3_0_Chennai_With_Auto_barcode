package Rcvr_AAMQ;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

public class Action {
	static Logger log = LogMQ.monitor("Rcvr_AAMQ.Action");
	
	
	public static void actionSeq(JSONObject jsonObj) throws Exception
	{
		JsonParser jspr = new JsonParser();
		XmlUtiility xmlUtil = new XmlUtiility();
		FileSystem fls = new FileSystem();
		Utils utils = new Utils();

		////TEMP
		/*
		if(!XmlUtiility.CheckGraphicsElementExist(jspr.geFilePathFromJson(jsonObj, "XMLFile")))
		{
			String barcodePath =  jspr.geFilePathFromJson(jsonObj, "") + "030_Barcodes/";
			if(!utils.FileExists(barcodePath))
			{
				barcodePath = jspr.geFilePathFromJson(jsonObj, "") + "030 Barcodes/";
			}
			XmlUtiility.GS1XmlAppendGraphicsElement(jspr.geFilePathFromJson(jsonObj, "XMLFile"), XmlUtiility.GetFileFromPathString(barcodePath).toString());
		}
		else
		{
			String barcodePath =  jspr.geFilePathFromJson(jsonObj, "") + "030_Barcodes/";
			if(!utils.FileExists(barcodePath))
			{
				barcodePath = jspr.geFilePathFromJson(jsonObj, "") + "030 Barcodes/";
			}
			XmlUtiility.GS1XmlParseGraphicsElement(jspr.geFilePathFromJson(jsonObj, "XMLFile"), XmlUtiility.GetFileFromPathString(barcodePath).toString());
		}
		/////TEMP
		*/
		SEng.CallAdobeIllustrator();
		log.info("Illustrator activated to load file..");
		
		String[] appFonts = SEng.GetApplicationFonts().split(",");
		Thread.sleep(2000);

		SEng.OpenDocument(jspr.geFilePathFromJson(jsonObj, "Master"));
		log.info("AI file and other dependend file opening..");
		
		String dcFont = SEng.GetDocumentFonts();
		if (!dcFont.equalsIgnoreCase("error"))
		{
			if (dcFont.length() != 0)
			{
				String[] docFonts = (dcFont).split(",");
				Action.FindMissingFonts(appFonts, docFonts);
			}
		}
		else
		{
			MessageQueue.ERROR += "Document parsing error while finding missing fonts \n";
		}
		
		String dcFile = SEng.GetDocumentFiles();
		if(!dcFile.equalsIgnoreCase("error"))
		{
			if(dcFile.length() != 0)
			{
				String[] docFiles = dcFile.split(",");
				Action.FindMissingFiles(docFiles);
			}
		}
		else
		{
			MessageQueue.ERROR += "Document parsing error while finding missing image linked files \n";
		}
		Thread.sleep(1000);
		
		 String[] arrString= new String[1];
		 arrString[0] = MessageQueue.MESSAGE;
		 arrString[0] = utils.RemoveForwardSlash(arrString[0]);
		 arrString[0] = arrString[0].replace("\"", "'");
		 
	//	 System.out.println(arrString[0] );
		 
		 SEng.CallTyphoonShadow(arrString);
		
		log.info("TyphoonShadow called");

		String errorMsg = fls.ReadFileReport("error.txt");
		if(errorMsg.contains("\n") && errorMsg.length() != 1)
			MessageQueue.STATUS = false;
		
	//	if(MessageQueue.STATUS) 
		{
		
			INIReader ini = new INIReader();
			ini.readIniForSingle();
			
			String[] docPath = jspr.getPath(jsonObj);
			String fileNameToSave = xmlUtil.getFileNameFromElement((docPath[0].split("~"))[0]);
			String[] fileName = new String[4];
			if(fileNameToSave != null)
			{
				fileName[0] = "none"; //dummy
				fileName[1] = "none"; //dummy
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[1]) + "/" + fileNameToSave;
			}
			
			if(MessageQueue.sPdfNormal)
			{
				if (fileNameToSave != null)
					SEng.PostDocumentProcessForSingleJobFilename(fileName);
				else
					SEng.PostDocumentProcess(jspr.getPath(jsonObj));
			}
			else if(MessageQueue.sPdfPreset)
			{

				String pdfPreset[] = utils.getPresetFileFromDirectory(utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")), "joboptions");
				String[] pdfPresetArr = new String[2];
				if(pdfPreset.length !=0 )
				{
					pdfPresetArr[0] = utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")) +"/"+ pdfPreset[0];
					pdfPresetArr[1] = pdfPreset[0].split("\\.")[0];
					String resultPdfExport = "";
					if (fileNameToSave != null)
						resultPdfExport = SEng.PostDocMultiPDFPreset(fileName, pdfPresetArr);
					else
					{
						String[] dcPath = new String[4];
						dcPath[0] = "";
						dcPath[1] = "";
						dcPath[2] = docPath[2];
						dcPath[3] = docPath[1];
						resultPdfExport = SEng.PostDocMultiPDFPreset(dcPath, pdfPresetArr);
					}
			    		if(!resultPdfExport.equalsIgnoreCase("null"))
			    		{
			    			fls.AppendFileString("\n PDF with preset export failed: " + resultPdfExport + " \n");
			    			MessageQueue.ERROR += resultPdfExport + "\n";
			    		}
				}
				else
				{
					fls.AppendFileString("\n PDF with preset export failed, preset file not found in master ai file path \n");
	    				MessageQueue.ERROR += "\n PDF with preset export failed, preset file not found in master ai file path \n";
				}
			}
			else if(MessageQueue.sPdfNormalised)
			{
				if(PostMultipleJobPreProcess())
				{
					docPath[2] = docPath[2].split("\\.")[0];
					String resultPdfExport = "";
					if (fileNameToSave != null)
						resultPdfExport = SEng.PostDocumentMultipleProcess(fileName);
					else
					{
						String[] dcPath = new String[4];
						dcPath[0] = "";
						dcPath[1] = "";
						dcPath[2] = docPath[2];
						dcPath[3] = docPath[1];
						resultPdfExport = SEng.PostDocumentMultipleProcess(dcPath);
					}
			    		if(!resultPdfExport.equalsIgnoreCase("null"))
			    		{
			    			fls.AppendFileString("\n Normalized PDF export failed: " + resultPdfExport + " \n");
			    			MessageQueue.ERROR += resultPdfExport + "\n";
			    		}
				}
			}
			
			log.info("Pdf and xml generated..");	

		}
		Thread.sleep(8000);
		SEng.PostDocumentClose();
		
		sendRespStatusMsg("delivered");
	    log.info("Completed process for job id  '" + MessageQueue.MSGID + "' ");
	    Thread.sleep(1000);
	    
		Action.UpdateToServer(jsonObj, "xmlcompare");
		log.info("Xml comparision completed..");
		
		Action.UpdateReport(jsonObj, fls.ReadFileReport("Report.txt"));
		
		MessageQueue.ERROR += errorMsg;
		Action.sendStatusMsg((String)MessageQueue.ERROR);
		MessageQueue.ERROR = "";
		Thread.sleep(1000);
		MessageQueue.GATE = true;
		
}

	public static void multiActionSeq(JSONObject jsonObj) throws Exception
	{
		JsonParser jspr = new JsonParser();
		XmlUtiility xmlUtil = new XmlUtiility();
		FileSystem fls = new FileSystem();
		Utils utils = new Utils();

		utils.CreateNewDirectory(jspr.getXmlFolderPathFromJson(jsonObj, "XMLFile") + "DummyFolder");

		String pdfPreset[] = utils.getPresetFileFromDirectory(utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")), "joboptions");
		
 		String xmlFiles[] = utils.getFileFromDirectory(jspr.getXmlFolderPathFromJson(jsonObj, "XMLFile"),"xml");
		ArrayList arrErrReport = new ArrayList();
		ArrayList arrDetailedReport = new ArrayList();
		ArrayList arrConsolidateErrorReport = new ArrayList();
		ArrayList arrConsolidateDetailedReport = new ArrayList();
		
		SEng.CallAdobeIllustrator();
		log.info("Illustrator activated to load file..");
		
		String[] appFonts = SEng.GetApplicationFonts().split(",");
		Thread.sleep(5000);
		
		SEng.OpenDocument(jspr.geFilePathFromJson(jsonObj, "Master"));
		log.info("AI file and other dependend file opening..");
		
		String dcFont = SEng.GetDocumentFonts();
		if (dcFont.length() != 0)
		{
			String[] docFonts = (dcFont).split(",");
			Action.FindMissingFonts(appFonts, docFonts);
		}
		String dcFile = SEng.GetDocumentFiles();
		if(dcFile.length() != 0)
		{
			String[] docFiles = dcFile.split(",");
			Action.FindMissingFiles(docFiles);
		}
		
		utils.DeleteDirectory(jspr.getXmlFolderPathFromJson(jsonObj, "XMLFile") + "DummyFolder");
		Thread.sleep(1000);
		
		
		INIReader ini = new INIReader();
		ini.readIniForMultiple();
		
		String[] pdfPresetArr = new String[2];
		if(MessageQueue.mPdfPreset)
		{
			if(pdfPreset.length !=0 )
			{
				pdfPresetArr[0] = utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")) +"/"+ pdfPreset[0];
				pdfPresetArr[1] = pdfPreset[0].split("\\.")[0];
			}
		}
		else if(MessageQueue.mPdfNormalised)
		{
			PostMultipleJobPreProcess();
		}
		
		String xmlDirPath = jspr.getXmlDirPath(jsonObj);
		for(int eachXmlCount = 0; eachXmlCount < xmlFiles.length; eachXmlCount++)
		{
			MessageQueue.MESSAGE = jspr.updateJsonForMultipleJob(jsonObj, xmlDirPath, xmlFiles[eachXmlCount]);
			String[] docPath = jspr.getMultiPath(jsonObj, xmlFiles[eachXmlCount]);
			if(!XmlUtiility.multipleJobIsValidXml(docPath[0]))
			{
				ConsolidateErrorReport(fls, arrErrReport, arrConsolidateErrorReport, arrDetailedReport, arrConsolidateDetailedReport, xmlFiles[eachXmlCount]);
				continue;
			}
			String fileNameToSave = xmlUtil.getFileNameFromElement(docPath[0]);
		//	docPath[0] += "~0";
			
			
			 String[] arrString= new String[1];
			 arrString[0] = MessageQueue.MESSAGE;
			 arrString[0] = utils.RemoveForwardSlash(arrString[0]);
			 arrString[0] = arrString[0].replace("\"", "'");
			 
			 System.out.println(arrString[0]);
			 
			 SEng.CallTyphoonShadow(arrString);

			
		//	SEng.CallTyphoonShadow(docPath);
			log.info("TyphoonShadow called");
						
			String[] fileName = new String[4];
			if(fileNameToSave != null)
			{
				fileName[0] = "none"; //dummy
				fileName[1] = "none"; //dummy
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[3]) + "/" + fileNameToSave;
			}
				
			if(MessageQueue.mPdfNormal)
			{
				if (fileNameToSave != null)
				{
				//	SEng.PostDocumentProcess(fileName);
					SEng.PostDocumentProcessForSingleJobFilename(fileName);
				}
				else
					SEng.PostDocumentProcessForSingleJobFilename(docPath);
			}
			else if(MessageQueue.mPdfPreset)
			{
				if(pdfPreset.length !=0)
				{
					String resultPdfExport = "";
					if(fileNameToSave != null)
						resultPdfExport = SEng.PostDocMultiPDFPreset(fileName, pdfPresetArr);
					else
						resultPdfExport = SEng.PostDocMultiPDFPreset(docPath, pdfPresetArr);
			    		if(!resultPdfExport.equalsIgnoreCase("null"))
			    		{
			    			fls.AppendFileString("\n PDF with preset export failed: " + resultPdfExport + " \n");
			    			MessageQueue.ERROR += resultPdfExport + "\n";
			    		}
				}
				else
				{
					fls.AppendFileString("\n PDF with preset export failed, preset file not found in master ai file path \n");
	    				MessageQueue.ERROR += "\n PDF with preset export failed, preset file not found in master ai file path \n";
				}
			}
			else if(MessageQueue.mPdfNormalised)
			{
				docPath[2] = docPath[2].split("\\.")[0];
				String resultPdfExport = "";
				if(fileNameToSave != null)
					resultPdfExport = SEng.PostDocumentMultipleProcess(fileName);
				else
					resultPdfExport = SEng.PostDocumentMultipleProcess(docPath);
		    		if(!resultPdfExport.equalsIgnoreCase("null"))
		    		{
		    			fls.AppendFileString("\n Normalized PDF export failed: " + resultPdfExport + " \n");
		    			MessageQueue.ERROR += resultPdfExport + "\n";
		    		}
			}
			Thread.sleep(4000);
			log.info("Pdf and xml generated..");		
			ConsolidateErrorReport(fls, arrErrReport, arrConsolidateErrorReport, arrDetailedReport, arrConsolidateDetailedReport, xmlFiles[eachXmlCount]);
		}
		
		Thread.sleep(7000);
		SEng.PostDocumentClose();
		sendRespStatusMsg("delivered");
	    log.info("Completed process for job id  '" + MessageQueue.MSGID + "' ");
		
		Action.UpdateToServer(jsonObj, "xmlcompare");
		//Action.UpdateToServer(jsonObj, "xmlcompare&type=multi");
		log.info("Xml comparision completed..");

		Action.sendStatusMsg(arrConsolidateErrorReport.toString());
		log.info("Completed sending error report..");
		Action.UpdateReport(jsonObj, arrConsolidateDetailedReport.toString());
		log.info("Completed sending of detailed report..");

		MessageQueue.ERROR = "";
		Thread.sleep(1000);
		MessageQueue.GATE = true;
		log.info("Completed job..");
	}
	
	public static String GetLastIndex(String filePath)
	{
		int index = filePath.lastIndexOf("/");
		return filePath.substring(0, index);
	}
	
	public static boolean PostMultipleJobPreProcess()
	{
		Utils utls = new Utils();
		boolean eskoPluginbool = false;
		String eskoPdfPlugin = "";
		if(MessageQueue.VERSION.equalsIgnoreCase("CS6"))
		{
			eskoPdfPlugin = "/Applications/Adobe Illustrator "+ MessageQueue.VERSION +"/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI16r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		}
		else if(MessageQueue.VERSION.equalsIgnoreCase("CC"))
		{
			eskoPdfPlugin = "/Applications/Adobe Illustrator "+ MessageQueue.VERSION +"/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI17r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		}
		else if(MessageQueue.VERSION.equalsIgnoreCase("CC 2014"))
		{
			eskoPdfPlugin = "/Applications/Adobe Illustrator "+ MessageQueue.VERSION +"/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI18r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		}
		else if(MessageQueue.VERSION.equalsIgnoreCase("CC 2015"))
		{
			eskoPdfPlugin = "/Applications/Adobe Illustrator "+ MessageQueue.VERSION +"/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI20r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		}
		else if(MessageQueue.VERSION.equalsIgnoreCase("CC 2015.3"))
		{
			eskoPdfPlugin = "/Applications/Adobe Illustrator "+ MessageQueue.VERSION +"/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI20r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		}
		else if(MessageQueue.VERSION.equalsIgnoreCase("CC 2017"))
		{
			eskoPdfPlugin = "/Applications/Adobe Illustrator "+ MessageQueue.VERSION +"/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI21r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		}
		
		if(!eskoPluginbool)
		{
			MessageQueue.ERROR += "PDF cannot be generated following plugin missing : " + eskoPdfPlugin + "\n";
			return false;
		}
		return true;
	}
	
	public static void ConsolidateErrorReport(FileSystem fls, ArrayList arrErrReport, ArrayList arrConsolidateErrorReport, ArrayList arrDetailedReport, ArrayList arrConsolidateDetailedReport,  String xmlFile)
	{
		String errorMsg = fls.ReadFileReport("error.txt");
		MessageQueue.ERROR += errorMsg;
		arrErrReport.clear();
		arrErrReport.add(MessageQueue.ERROR);
		arrConsolidateErrorReport.add(xmlFile + ":" + arrErrReport);
		fls.CreateFile("error.txt");
		MessageQueue.ERROR = "";
		
		String errDetailedReport = fls.ReadFileReport("Report.txt");
		arrDetailedReport.clear();
		arrDetailedReport.add(errDetailedReport);
		arrConsolidateDetailedReport.add(xmlFile + ":" + arrDetailedReport);
		fls.CreateFile("Report.txt");
	}

	public static void ValidateFiles(JSONObject jsonObj) throws Exception
	{
		
		JsonParser jspr = new JsonParser();
		String[] pathString = jspr.getPath(jsonObj);
		try
		{
			String[] xmlFilesPath = pathString[0].split(",");
			for (int eachXmlCount = 0; eachXmlCount < xmlFilesPath.length; eachXmlCount++)
			{
				XmlUtiility.IsValidXML(xmlFilesPath[eachXmlCount].split("~")[0]);
			}
		}
		catch(Exception ex)
		{
			log.error("xml err: " + ex);
			ThrowException.CatchException(ex);
		}
	}
	
	public static void acknowledge(String jsonString) throws Exception
	{
		JSONObject jsonObj = JsonParser.ParseJson(jsonString);
		String version =  (String) jsonObj.get("version");
		MessageQueue.VERSION = version;
		FileSystem fls = new FileSystem();
		fls.CreateFile("Report.txt");
		fls.CreateFile("error.txt");
		
		INetwork iNet = new INetwork();
		
		MessageQueue.MSGID = (String) jsonObj.get("Id");
		try
		{
			if(!((String) jsonObj.get("type")).equals("multi"))
				ValidateFiles(jsonObj);
		}
		catch(java.lang.NullPointerException Ex)
		{
			log.error(Ex.getMessage());
			MessageQueue.ERROR += "\nInvalid Json request";
			fls.AppendFileString("\nInvalid Json request:" + " \n");
			ThrowException.CustomExit(Ex, "Invalid JSON request from Tornado");
		}
		try
		{
			sendRespStatusMsg("received"+"::"+iNet.GetClientIPAddr());
			log.info("Message received acknowledgement for job id  '" + MessageQueue.MSGID + "' ");
			
			if(!((String) jsonObj.get("type")).equals("multi"))
				actionSeq(jsonObj);
			else
				multiActionSeq(jsonObj);
		}
		catch(Exception ex)
		{
			log.error("Msg Ack err: " + ex);
		}
		
	}
	
	
	public static void sendRespStatusMsg(String status) throws Exception
	{
		try
		{
			
			HttpConnection.excutePost("http://"+ MessageQueue.HOST_IP +":8080/AAW/message/resp", MessageQueue.MSGID + "::" + status);
		}
		catch(Exception ex)
		{
			log.error(ex);
		}
		
	}
	

	public static void sendStatusMsg(String status) throws Exception
	{
		try
		{
			HttpConnection.excutePost("http://"+ MessageQueue.HOST_IP +":8080/AAW/message/error", MessageQueue.MSGID + "::" + status);
		}
		catch(Exception ex)
		{
			log.error(ex);
		}
		
	}

	public static void UpdateToServer(JSONObject jsonObj, String actionStr) throws IOException
	{
		try{
		HttpsConnection httpsCon = new HttpsConnection();
		HttpURLConnection connection;
		URL urlStr = new URL(MessageQueue.TORNADO_HOST + "/rest/pub/aaw/"+ actionStr +"?mqid="+ (String) jsonObj.get("Id"));  
		connection = (httpsCon.getURLConnection(urlStr, true));
//		System.out.println("xml Compare response " + connection.getResponseMessage());
		System.out.println("XML Compare : " + connection.getResponseCode());
		}
		catch(Exception ex)
		{
			log.error((String)ex.getMessage());
		}
	}
	
	public static void UpdateReport(JSONObject jsonObj, String reportStr) throws IOException
	{
		try{
		HttpsConnection httpsCon = new HttpsConnection();
		httpsCon.excuteHttpJsonPost( MessageQueue.TORNADO_HOST + "/rest/pub/aaw/finalreport",  (String) jsonObj.get("Id"), reportStr);
		}
		catch(Exception ex)
		{
			log.error((String)ex.getMessage());
		}
	}
	
	
	public static void FindMissingFonts(String[] appFonts, String[] docFonts) throws Exception
	{
		Utils utls = new Utils();
		FileSystem fls = new FileSystem();
		ArrayList<String> missingFonts = utls.GetMissingFonts(appFonts, docFonts);
		if (missingFonts.size() > 0)
		{
			fls.AppendFileString("Document font not found:"+ missingFonts.toString()+"\n");
			MessageQueue.ERROR += "\nDocument linked font missing \n";
		}
	}
	
	public static void FindMissingFiles(String[]  arrFiles) throws Exception
	{
		
		Utils utls = new Utils();
		FileSystem fls = new FileSystem();
		ArrayList<String> missingFiles = new ArrayList<String>();
		{
			int arrLen = arrFiles.length;
			if(arrLen>0)
			{
				for (int i = 0; i < arrLen; i++)
				{
					if(!utls.FileExists(arrFiles[i]))
					{
						missingFiles.add(arrFiles[i]);
					}
				}
				List<String> duplicateList = missingFiles;
				HashSet<String> listToSet = new HashSet<String>(duplicateList);
				List<String> listWithoutDuplicates = new ArrayList<String>(listToSet);
				missingFiles = (ArrayList<String>) listWithoutDuplicates;
			}

		}

		if (missingFiles.size() > 0)
		{
			fls.AppendFileString("Document file not found :"+ missingFiles.toString()+"\n");
			MessageQueue.ERROR += "\nDocument linked file missing \n";
		}
	}
	
	public static void AddVolumes() throws Exception
	{
		List<String[]> rowList = new ArrayList<String[]>();
		Utils utls = new Utils();
		rowList = utls.ReadXLSXFile(utls.GetPathFromResource("smb.xlsx"), "sheet1");
	    for (String[] row : rowList)
	    {
	       	int noOfShareFolder = row.length - 3;
			for(int inc=0; inc < noOfShareFolder; inc++)
			{
			SEng.MountVolume(row[0], row[1], row[2], row[row.length - inc-1]);	
			} 
	    }
	}
	
	public static void Mount() throws Exception
	{
		String[] arg = null;
		Utils utl = new Utils();
		arg = utl.ReadFromExcel("SMB.xls", true, 0, false, 0, false);
		int noOfShareFolder = arg.length - 3;
			for(int inc=0; inc < noOfShareFolder; inc++)
			{
				SEng.MountVolume(arg[0], arg[1], arg[2], arg[arg.length - inc-1]);	
			}

	}
	
	public static void main(String args[]) throws Exception
	{
		String str = "//Users//yuvaraj//TEST FILES//AAW//446512A//100 XML//A02//dummy.xml";
		System.out.println(str.split("\\.")[0]);
		
	}


}