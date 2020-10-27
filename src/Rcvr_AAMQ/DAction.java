package Rcvr_AAMQ;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.rabbitmq.client.AMQP.Connection;

import Rcvr_AAMQ.DUtils;

public class DAction {
	static Logger log = DLogMQ.monitor("Rcvr_AAMQ.DAction");

	public static void actionSeq(JSONObject jsonObj, boolean repeat) throws Exception {
		DJsonParser jspr = new DJsonParser();
		DXmlUtiility xmlUtil = new DXmlUtiility();
		DFileSystem fls = new DFileSystem();
		DUtils utils = new DUtils();
		

		log.info(MessageQueue.WORK_ORDER + ": " + "RR started processing");
		

		DSEng.CallAdobeIllustrator();
		log.info(MessageQueue.WORK_ORDER + ": " + "Illustrator activated to load file..");
		
		String[] appFonts = DSEng.GetApplicationFonts().split(",");
		Thread.sleep(2000);
		
		DSEng.OpenDocument(jspr.geFilePathFromJson(jsonObj, "Master"));
		log.info(MessageQueue.WORK_ORDER + ": " + "AI file and other dependent file opening..");

		String dcFont = DSEng.GetDocumentFonts();
		if (!dcFont.equalsIgnoreCase("error")) {
			if (dcFont.length() != 0) {
				String[] docFonts = (dcFont).split(",");
				DAction.FindMissingFonts(appFonts, docFonts);
			}
		} else {
			MessageQueue.ERROR += "Document parsing error while finding missing fonts \n";
		}

		String dcFile = DSEng.GetDocumentFiles();
		if (!dcFile.equalsIgnoreCase("error")) {
			if (dcFile.length() != 0) {
				String[] docFiles = dcFile.split(",");
				DAction.FindMissingFiles(docFiles);
			}
		} else {
			MessageQueue.ERROR += "Document parsing error while finding missing image linked files \n";
		}
		
		
		String xmlFilePath = utils.RemoveForwardSlash((String) jspr.geFilePathFromJson(jsonObj, "XMLFile") );
		String exportXmlFilePath = (xmlFilePath.replace("100_XML", "080_QC")).replace(".xml", "_export.xml");
		
		utils.DeleteFile(exportXmlFilePath);
		
		if(!utils.CreateNewDirectory(exportXmlFilePath.substring(0, exportXmlFilePath.lastIndexOf("/")), false))
		{
			log.error(MessageQueue.WORK_ORDER + ": " + "Directory not able to create :  - "
					+ exportXmlFilePath.toString());
			DAction.UpdateErrorStatusWithRemark("23",
					"Export xml File path not found / not able to create directory " + exportXmlFilePath);
			repeat = false;
		}
		

		
		Thread.sleep(1000);
		DAction.UpdateErrorStatusWithRemark("39", "In progress RoadRunner (Running now)");

		String[] arrString = new String[1];
		arrString[0] = MessageQueue.MESSAGE;
		arrString[0] = utils.RemoveForwardSlash(arrString[0]);
		arrString[0] = arrString[0].replace("\"", "'");


		log.info(MessageQueue.WORK_ORDER + ": " + "RoadRunner plugin called");
		DSEng.CallTyphoonShadow(arrString);

		Path file = Paths.get(utils.ConvertToAbsolutePath("/Applications/Adobe Illustrator "+ MessageQueue.VERSION +"/Plug-ins.localized/Sgk/Configuration/StatusJson.json"));
		JSONObject StatusJsonObj = null;
		StatusJsonObj = jspr.ParseJsonFile(file.toString());
		String masterXMLCheck = (String) StatusJsonObj.get("MasterXML");
		String masterXMLErorr = (String) StatusJsonObj.get("MasterXMLError");
		String ExportXMLCheck = (String) StatusJsonObj.get("ExportXML");
		String ExportXMLError = (String) StatusJsonObj.get("ExportXMLError");
		
		if(masterXMLCheck.equalsIgnoreCase("false"))
		{
			log.error(MessageQueue.WORK_ORDER + ": " + "Master xml not exits " +  masterXMLErorr);
			DAction.UpdateReport(fls.ReadFileReport("Report.txt"));
			DAction.sendStatusMsg((String) MessageQueue.ERROR);
			DSEng.PostDocumentClose();
			DThrowException.CustomExitWithErrorMsgID(new Exception("Master XML not exits "), "Master XML not exits - " + masterXMLErorr, "23");
		}
		
		
		
		Thread.sleep(2000);
	
		DDataOutput DO = new DDataOutput();
		DO.GetAllPath(jsonObj);
	
		

		//if(DDataOutput.SWATCH_ENABLE) {
	//	SwatchMergeFromXML(DDataOutput.XML_PATH, DDataOutput.SWATCH_NAME, DDataOutput.SWATCH_ELEMENT);  // for PENANG alone
		SwatchMergeFromXML(DDataOutput.XML_PATH, "Color Merge", "SL_ColorName");
		//}
		
		DO.ExportData(DDataOutput.FILE_NAME_TO_SAVE);
		DO.ExportCustomizedData(jsonObj, DDataOutput.FILE_NAME_TO_SAVE, false);
		
		String errorMsg = fls.ReadFileReport("error.txt");
		if (errorMsg.contains("\n") && errorMsg.length() != 1)
			MessageQueue.STATUS = false;
			
		{
			log.info(MessageQueue.WORK_ORDER + ": " + "Pdf and xml generated..");

			//// ----PNG---// Only 3D xml
		//	JsonParser jsonPars = new JsonParser();
			
			if (DDataOutput.IS_RR3DXML && ( MessageQueue.TORNADO_ENV.equals("development") || MessageQueue.TORNADO_ENV.equals("qa") || MessageQueue.TORNADO_ENV.equals("production"))) {
				
				DUtils utls = new DUtils();
				
				utls.MoveFileFromSourceToDestination(DDataOutput.MASTER_ART_PATH, DDataOutput.MAIN_PATH + DDataOutput.SOURCE_FILE_DESTINATION_MOVE_PATH);
				
				String jsonString = "";
				DFileSystem fileSystem =  new DFileSystem();
				
				 String brackGroundReferenceURI = "";
				 String logoReferenceURI = "";
				 HashMap<String, String> map = new HashMap<String, String>();
				 DXmlUtiility xmlt = new DXmlUtiility();
				 map = xmlt.GS1XmlParseAllElement_TMP(DDataOutput.MAIN_PATH + DDataOutput.RR3DXML_SAVE_PATH +  "3DXML.xml", false, brackGroundReferenceURI, logoReferenceURI);

				 brackGroundReferenceURI = map.get("ESG-backgroundfilename-NA-0001");
				 logoReferenceURI = map.get("ESG-logos-LO-0001");
				
				JSONObject rr3DJson = new JSONObject();
				rr3DJson.put("ESG-backgroundfilename-NA-0001", brackGroundReferenceURI);
				rr3DJson.put("ESG-logos-LO-0001", logoReferenceURI);
				
				fileSystem.CreateFile(DDataOutput.MAIN_PATH + DDataOutput.RR3DXML_SAVE_PATH,  "RRremap.txt");
				fileSystem.AppendFileString(DDataOutput.MAIN_PATH + DDataOutput.RR3DXML_SAVE_PATH + "RRremap.txt", rr3DJson.toJSONString());
				
				fileSystem.CreateFile(DDataOutput.MAIN_PATH + DDataOutput.RR3DXML_SAVE_PATH,  "ReRun3DXML.txt");
				
				jsonString = jspr.updateJsonForMultipleJob(jsonObj,
						DDataOutput.RR3DXML_SAVE_PATH, "3DXML.xml", true, DDataOutput.FILE_NAME_TO_SAVE);
				
				fileSystem.AppendFileString(DDataOutput.MAIN_PATH + DDataOutput.RR3DXML_SAVE_PATH + "ReRun3DXML.txt", jsonString);

			}
			else if((MessageQueue.RR_SOURCE.equals("HUBX_RR") || MessageQueue.RR_SOURCE.equals("WAVEPNG")) && (MessageQueue.TORNADO_ENV.equals("qa") || MessageQueue.TORNADO_ENV.equals("development") || MessageQueue.TORNADO_ENV.equals("production")))
			{	
				if(DDataOutput.MASTER_TEMPLATE_PATH != null)
				{
					if(!DDataOutput.MASTER_TEMPLATE_PATH.isEmpty())
					{
						DUtils utls = new DUtils();
						utls.MoveFileFromSourceToDestination(DDataOutput.MASTER_ART_PATH, DDataOutput.MAIN_PATH + DDataOutput.SOURCE_FILE_DESTINATION_MOVE_PATH);
					}
				}
			}
		}
		Thread.sleep(2000);
		DSEng.PostDocumentClose();

		sendRespStatusMsg("delivered");
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed process for job id  '" + MessageQueue.MSGID + "' ");
		Thread.sleep(1000);
		
		if(utils.FileExists(exportXmlFilePath) && !ExportXMLCheck.equalsIgnoreCase("false"))
		{
			DAction.UpdateToServer("xmlcompare", false);
			log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparison completed..");
		}
		else
		{
			if(!ExportXMLCheck.equalsIgnoreCase("false"))
			{
				DAction.UpdateToServer("xmlcompare", false);
				log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparison completed..");
			}
			else
			{
				log.error(MessageQueue.WORK_ORDER + ": " + "Export xml not exits on the path " +  ExportXMLError);
			}
			String exportXmlReport = fls.ReadFileReport("Export.txt");
			if(exportXmlReport.length() > 5 )
			{
			log.error(MessageQueue.WORK_ORDER + ": " + "Export xml not exits on the path " +  exportXmlFilePath);
			DAction.UpdateErrorStatusWithRemark("14", "Export XML not generated " + exportXmlFilePath  + "Error : " + exportXmlReport.toString());
			}
		}

//		if(utils.FileExists(exportXmlFilePath))
//		{
//			DAction.UpdateToServer("xmlcompare", false);
//			log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparison completed..");
//		}
//		else
//		{
//			DAction.UpdateToServer("xmlcompare", false);
//			log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparison completed..");
//			String exportXmlReport = fls.ReadFileReport("Export.txt");
//			if(exportXmlReport.length() > 5 )
//			{
//			log.error(MessageQueue.WORK_ORDER + ": " + "Export xml not exits on the path " +  exportXmlFilePath);
//			DAction.UpdateErrorStatusWithRemark("14", "Export XML not generated " + exportXmlFilePath  + "Error : " + exportXmlReport.toString());
//			}
//		}

		DAction.UpdateReport(fls.ReadFileReport("Report.txt"));
		
		MessageQueue.ERROR += errorMsg;
		DAction.sendStatusMsg((String) MessageQueue.ERROR);

		MessageQueue.ERROR = "";
		MessageQueue.WORK_ORDER = "";
		Thread.sleep(1000);
		MessageQueue.GATE = true;
	}
	
	
	public static void ReRun3DXML(JSONObject jsonObj) throws Exception
	{
		DJsonParser jspr = new DJsonParser();
		DXmlUtiility xmlUtil = new DXmlUtiility();
		DFileSystem fls = new DFileSystem();
		DUtils utils = new DUtils();

		Thread.sleep(1000);
		
		log.info(MessageQueue.WORK_ORDER + ": " + "RR 3DXML started processing");

		DSEng.CallAdobeIllustrator();
		log.info(MessageQueue.WORK_ORDER + ": " + "Illustrator activated to load file..");
		
		DSEng.OpenDocument(jspr.geFilePathFromJson(jsonObj, "Master"));
		log.info(MessageQueue.WORK_ORDER + ": " + "AI file and other dependend file opening..");
		
		Thread.sleep(1000);

		{
			DINIReader ini = new DINIReader();
			ini.readIniForSingle();
			
			String renderPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path");
			renderPath = renderPath + "090_Deliverables/095_Renders/";
			if(!DUtils.IsFolderExists(renderPath))
				utils.CreateNewDirectory(renderPath, false);
			
			DDataOutput DO = new DDataOutput();
			DO.GetAllPath(jsonObj);
			DO.ExportData(DDataOutput.FILE_NAME_TO_SAVE);
			DO.ExportCustomizedData(jsonObj, DDataOutput.FILE_NAME_TO_SAVE, false);
			
			log.info(MessageQueue.WORK_ORDER + ": " + "Pdf and xml generated..");
		}

		String[] fileName = new String[4];
	
		String jsonString = "";
		if (utils.FileExists(DDataOutput.MAIN_PATH + DDataOutput.RR3DXML_SAVE_PATH + "3DXML.xml")) 
		{
			jsonString = jspr.updateJsonForMultipleJob(jsonObj,
					DDataOutput.RR3DXML_SAVE_PATH, "3DXML.xml");
			
			String brackGroundReferenceURI = "";
			String logoReferenceURI = "";
			DXmlUtiility xmlt = new DXmlUtiility();
			String rr3DJson = fls.ReadFile(DDataOutput.MAIN_PATH + DDataOutput.RR3DXML_SAVE_PATH + "RRremap.txt");
			
			try
			{
			JSONObject rr3DJsonObj = jspr.ParseJson(rr3DJson.toString());
			
			brackGroundReferenceURI = (String) rr3DJsonObj.get("ESG-backgroundfilename-NA-0001");
			logoReferenceURI = (String) rr3DJsonObj.get("ESG-logos-LO-0001");
			}
			catch(Exception ex)
			{
				System.out.println(ex.getMessage());
			}
			   
			 xmlt.GS1XmlParseAllElement_TMP(DDataOutput.MAIN_PATH + DDataOutput.RR3DXML_SAVE_PATH + "3DXML.xml", true, brackGroundReferenceURI, logoReferenceURI);
			 Thread.sleep(2000);
		}


		if (jsonString != "") {
			String[] newArryStr = new String[1];

			newArryStr[0] = jsonString;
			newArryStr[0] = utils.RemoveForwardSlash(newArryStr[0]);
			newArryStr[0] = newArryStr[0].replace("\"", "'");
			
			DAction.UpdateErrorStatusWithRemark("37", "3DXML Started");

			DSEng.CallTyphoonShadow(newArryStr);
			Thread.sleep(1000);

			// PNG set swatch White color to White 2
		//	SEng.SetSwathColorFromTo("White 2", "White"); //// only for NCL P - N - G
		//	DSEng.SetSwathColorFromTo(DDataOutput.SWATCH_FROM, DDataOutput.SWATCH_TO); //// only for NCL P - N - G
			SwatchMergeFromXML(DDataOutput.XML_PATH, "Color Merge", "SL_ColorName");
			DSEng.SetSwathColorFromTo("White 2", "White");
			DSEng.SetLayerVisibleOff(); //// only for NCL P - N - G

			Thread.sleep(1000);
			String fileRenameString = jspr.getJsonValueForKey(jsonObj, "WO") + "_3dxml";
/*
			String road_runnerDirPath = utils.RemoveForwardSlash(DataOutput.MAIN_PATH + DataOutput.RR3DXML_SAVE_PATH);
			if (!Utils.IsFolderExists(road_runnerDirPath)) {
				utils.CreateNewDirectory(road_runnerDirPath, false);
			}

			fileName[0] = utils.RemoveForwardSlash(DataOutput.MAIN_PATH + DataOutput.RR3DXML_SAVE_PATH);

			fileName[1] = road_runnerDirPath + "/" + fileRenameString;
			fileName[2] = fileName[0] + fileRenameString;
			fileName[3] = fileName[0] + fileRenameString;
			SEng.PostDocumentProcessFor3DXML(fileName); */
			
			DDataOutput DO = new DDataOutput();
			DO.GetAllPath(jsonObj);
			DO.ExportCustomizedData(jsonObj, fileRenameString,  true);
			Thread.sleep(3000);

			DSEng.PostDocumentClose();
			DAction.UpdateErrorStatusWithRemark("38", "3DXML Completed");
			Thread.sleep(1000);
			
			DAction.UpdateToServer("xmlcompare", false);
			
			MessageQueue.ERROR = "";
			MessageQueue.WORK_ORDER = "";
			Thread.sleep(1000);
			MessageQueue.GATE = true;
		}

	
	}
	

	public static void multiActionSeq(JSONObject jsonObj) throws Exception {
		DJsonParser jspr = new DJsonParser();
		DXmlUtiility xmlUtil = new DXmlUtiility();
		DFileSystem fls = new DFileSystem();
		DUtils utils = new DUtils();
		
		DDataOutput DO = new DDataOutput();
		DO.GetAllPath(jsonObj);

		utils.CreateNewDirectory(DDataOutput.XML_PATH + "DummyFolder", true);

		String xmlFiles[] = utils.getFileFromDirectory(DDataOutput.XML_PATH, "xml");
		ArrayList arrErrReport = new ArrayList();
		ArrayList arrDetailedReport = new ArrayList();
		ArrayList arrConsolidateErrorReport = new ArrayList();
		ArrayList arrConsolidateDetailedReport = new ArrayList();

		DSEng.CallAdobeIllustrator();
		log.info(MessageQueue.WORK_ORDER + ": " + "Illustrator activated to load file..");

		String[] appFonts = DSEng.GetApplicationFonts().split(",");
		Thread.sleep(5000);

		DSEng.OpenDocument(DDataOutput.MASTER_ART_PATH);
		
		log.info(MessageQueue.WORK_ORDER + ": " + "AI file and other dependent file opening..");

		String dcFont = DSEng.GetDocumentFonts();
		if (dcFont.length() != 0) {
			String[] docFonts = (dcFont).split(",");
			DAction.FindMissingFonts(appFonts, docFonts);
		}
		String dcFile = DSEng.GetDocumentFiles();
		if (dcFile.length() != 0) {
			String[] docFiles = dcFile.split(",");
			DAction.FindMissingFiles(docFiles);
		}

		utils.DeleteDirectory(DDataOutput.XML_PATH + "DummyFolder");
		Thread.sleep(1000);

		for (int eachXmlCount = 0; eachXmlCount < xmlFiles.length; eachXmlCount++) {
			MessageQueue.MESSAGE = jspr.updateJsonForMultipleJob(jsonObj, DDataOutput.XML_PATH, xmlFiles[eachXmlCount]);
			String[] docPath = jspr.getMultiPath(jsonObj, xmlFiles[eachXmlCount]);
			if (!DXmlUtiility.multipleJobIsValidXml(docPath[0])) {
				ConsolidateErrorReport(fls, arrErrReport, arrConsolidateErrorReport, arrDetailedReport,
						arrConsolidateDetailedReport, xmlFiles[eachXmlCount]);
				continue;
			}
			String fileNameToSave = xmlUtil.getFileNameFromElement(docPath[0]);
			
			// docPath[0] += "~0"; //commented 

			String[] arrString = new String[1];
			arrString[0] = MessageQueue.MESSAGE;
			arrString[0] = utils.RemoveForwardSlash(arrString[0]);
			arrString[0] = arrString[0].replace("\"", "'");

			DSEng.CallTyphoonShadow(arrString);

			log.info(MessageQueue.WORK_ORDER + ": " + "RoadRunner plugin called");
			
			File file = new File(DDataOutput.MAIN_PATH + DDataOutput.RENDER_SAVE_PATH);
			file.mkdirs();

			if(fileNameToSave == null)
			{
				fileNameToSave =  MessageQueue.WORK_ORDER + "_" +  xmlFiles[eachXmlCount];
			}
			DO.ExportData(fileNameToSave);

			Thread.sleep(2000);
			log.info(MessageQueue.WORK_ORDER + ": " + "Pdf and xml generated..");
			ConsolidateErrorReport(fls, arrErrReport, arrConsolidateErrorReport, arrDetailedReport,
					arrConsolidateDetailedReport, xmlFiles[eachXmlCount]);
		}

		Thread.sleep(7000);
		DSEng.PostDocumentClose();
		sendRespStatusMsg("delivered");
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed process for job id  '" + MessageQueue.MSGID + "' ");

		DAction.UpdateToServer("xmlcompare", false);
		log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparision completed..");

		DAction.sendStatusMsg(arrConsolidateErrorReport.toString());
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed sending error report..");
		DAction.UpdateReport(arrConsolidateDetailedReport.toString());
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed sending of detailed report..");

		MessageQueue.ERROR = "";
		Thread.sleep(1000);
		MessageQueue.GATE = true;
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed job..");
	}
	
	public static void SwatchMergeFromXML(String xmlPathString, String swatchName, String privateElmtTypeCode)
			throws Exception {
		DFileSystem fls = new DFileSystem();
		try {
			List<String> SwatchListFromXML = new ArrayList<String>();
			DXmlUtiility xmlUtils = new DXmlUtiility();
			
			String swatchString = DSEng.SwatchTest();
			String[] arrSwatch = swatchString.split("~");
			List<String> swatchList = Arrays.asList(arrSwatch);
			List<String> filterSwatchList = new ArrayList<String>();
			for(int j = 0; j < swatchList.size(); j++ )
			{
				if(swatchList.get(j).contains(swatchName))
				{
					filterSwatchList.add(swatchList.get(j));
				}
			}
			
			
			HashMap<String, String> kMap = new HashMap<String, String>();
			   
			kMap = xmlUtils.ParsePrivateElementSwatchColor(xmlPathString, privateElmtTypeCode);

			
			String[] swatchColorArray = new String[2];
			int checkCount = 0;
			 
			for(int k=0; k<filterSwatchList.size();k++)
			{
				swatchColorArray[0] = filterSwatchList.get(k);
				swatchColorArray[1] = kMap.get(filterSwatchList.get(k));
				String result =  DSEng.ExecuteIllustratorActions(swatchColorArray);
				
				if(result != null)
				{
					log.error(MessageQueue.WORK_ORDER + ": " + "Error on swatch color merge");
					fls.AppendFileString("Issue on Swatch Merge from '" +swatchColorArray[0] +"' to '" +swatchColorArray[1] +"'\n");
				}
				else
					checkCount += 1;
				
			}

		} catch (Exception ex) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Error on swatch color merge " + ex.toString() );
			fls.AppendFileString("Error on swatch color merge \n");
		}
	}

	
	public static void SwatchMergeFromXML(String xmlPathString, String privateElmtTypeCode)
			throws Exception {
		DFileSystem fls = new DFileSystem();
		try {
			List<String> SwatchListFromXML = new ArrayList<String>();
			DXmlUtiility xmlUtils = new DXmlUtiility();
			
			String swatchString = DSEng.SwatchTest();
			String[] arrSwatch = swatchString.split("~");
			List<String> swatchList = Arrays.asList(arrSwatch);
			List<String> filterSwatchList = new ArrayList<String>();
			for(int j = 0; j < swatchList.size(); j++ )
			{
				if(swatchList.get(j).contains("Color Merge"))
				{
					filterSwatchList.add(swatchList.get(j));
				}
			}
			
			
			HashMap<String, String> kMap = new HashMap<String, String>();
			   
			kMap = xmlUtils.ParsePrivateElementSwatchColor(xmlPathString, privateElmtTypeCode);

			
			String[] swatchColorArray = new String[2];
			int checkCount = 0;
			 
			for(int k=0; k<filterSwatchList.size();k++)
			{
				swatchColorArray[0] = filterSwatchList.get(k);
				swatchColorArray[1] = kMap.get(filterSwatchList.get(k));
				String result =  DSEng.ExecuteIllustratorActions(swatchColorArray);
				
				if(result != null)
				{
					log.error(MessageQueue.WORK_ORDER + ": " + "Error on swatch color merge");
					fls.AppendFileString("Issue on Swatch Merge from '" +swatchColorArray[0] +"' to '" +swatchColorArray[1] +"'\n");
				}
				else
					checkCount += 1;
				
			}
//			if(checkCount != filterSwatchList.size())
//			{
//				log.error(MessageQueue.WORK_ORDER + ": " + "Error on swatch color merge , swatch not exist from '" +filterSwatchList.get(checkCount+1) +"' to '" +SwatchListFromXML.get(checkCount+1) +"'\n");
//				fls.AppendFileString("Issue on Swatch Merge, swatch not exist from '" +filterSwatchList.get(checkCount+1) +"' to '" +SwatchListFromXML.get(checkCount+1) +"'\n");
//			
//			}

		} catch (Exception ex) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Error on swatch color merge " + ex.toString() );
			fls.AppendFileString("Error on swatch color merge \n");
		}
	}
	

	public static void SwatchMergeFromXML(String xmlPathString, String privateElmtTypeCode, List<String> fromSwatchColorName, String toSwatchColorName)
			throws Exception {
		DFileSystem fls = new DFileSystem();
		try {
			List<String> SwatchListFromXML = new ArrayList<String>();
			DXmlUtiility xmlUtils = new DXmlUtiility();
			SwatchListFromXML = xmlUtils.ParsePrivateElementSwatchColor(xmlPathString, privateElmtTypeCode,
					toSwatchColorName);

			String swatchString = DSEng.SwatchTest();

			String[] arrSwatch = swatchString.split("~");
			List<String> swatchList = Arrays.asList(arrSwatch);
			String[] swatchColorArray = new String[2];
			int checkCount = 0;
			for(int i = 0; i< fromSwatchColorName.size(); i++ )
			{
				if(swatchList.contains(fromSwatchColorName.get(i)) && swatchList.contains(SwatchListFromXML.get(i)))
				{
					swatchColorArray[0] = fromSwatchColorName.get(i);
					swatchColorArray[1] = SwatchListFromXML.get(i);
					String result =  DSEng.ExecuteIllustratorActions(swatchColorArray);
					if(result != null)
					{
						log.error(MessageQueue.WORK_ORDER + ": " + "Error on swatch color merge");
						fls.AppendFileString("Issue on Swatch Merge from '" +swatchColorArray[0] +"' to '" +swatchColorArray[1] +"'\n");
					}
					else
						checkCount += 1;
				}
			}
			if(checkCount != fromSwatchColorName.size())
			{
				log.error(MessageQueue.WORK_ORDER + ": " + "Error on swatch color merge , swatch not exist from '" +fromSwatchColorName.get(checkCount+1) +"' to '" +SwatchListFromXML.get(checkCount+1) +"'\n");
				fls.AppendFileString("Issue on Swatch Merge, swatch not exist from '" +fromSwatchColorName.get(checkCount+1) +"' to '" +SwatchListFromXML.get(checkCount+1) +"'\n");
			
			}
			
		} catch (Exception ex) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Error on swatch color merge " + ex.toString() );
			fls.AppendFileString("Error on swatch color merge \n");
		}

	}

	public static String GetLastIndex(String filePath) {
		int index = filePath.lastIndexOf("/");
		return filePath.substring(0, index);
	}

	public static boolean PostMultipleJobPreProcess() throws IOException {
		DUtils utls = new DUtils();
		boolean eskoPluginbool = false;
		String eskoPdfPlugin = "";
		if (MessageQueue.VERSION.equalsIgnoreCase("CS6")) {
			eskoPdfPlugin = "/Applications/Adobe Illustrator " + MessageQueue.VERSION
					+ "/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI16r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		} else if (MessageQueue.VERSION.equalsIgnoreCase("CC")) {
			eskoPdfPlugin = "/Applications/Adobe Illustrator " + MessageQueue.VERSION
					+ "/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI17r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		} else if (MessageQueue.VERSION.equalsIgnoreCase("CC 2014")) {
			eskoPdfPlugin = "/Applications/Adobe Illustrator " + MessageQueue.VERSION
					+ "/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI18r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		} else if (MessageQueue.VERSION.equalsIgnoreCase("CC 2015")) {
			eskoPdfPlugin = "/Applications/Adobe Illustrator " + MessageQueue.VERSION
					+ "/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI20r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		} else if (MessageQueue.VERSION.equalsIgnoreCase("CC 2015.3")) {
			eskoPdfPlugin = "/Applications/Adobe Illustrator " + MessageQueue.VERSION
					+ "/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI20r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		} else if (MessageQueue.VERSION.equalsIgnoreCase("CC 2017")) {
			eskoPdfPlugin = "/Applications/Adobe Illustrator " + MessageQueue.VERSION
					+ "/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI21r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		} else if (MessageQueue.VERSION.equalsIgnoreCase("CC 2018")) {
			eskoPdfPlugin = "/Applications/Adobe Illustrator " + MessageQueue.VERSION
					+ "/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI22r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		} else if (MessageQueue.VERSION.equalsIgnoreCase("CC 2019")) {
			eskoPdfPlugin = "/Applications/Adobe Illustrator " + MessageQueue.VERSION
					+ "/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI23r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		} else if (MessageQueue.VERSION.equalsIgnoreCase("2020")) {
			eskoPdfPlugin = "/Applications/Adobe Illustrator " + MessageQueue.VERSION
					+ "/Plug-ins.localized/Esko/Data Exchange/PDF Export/PDFExport_MAI24r.aip";
			eskoPluginbool = utls.FileExists(eskoPdfPlugin);
		}

		if (!eskoPluginbool) {
			MessageQueue.ERROR += "PDF cannot be generated following plugin missing : " + eskoPdfPlugin + "\n";
			return false;
		}
		return true;
	}

	public static void ConsolidateErrorReport(DFileSystem fls, ArrayList arrErrReport,
			ArrayList arrConsolidateErrorReport, ArrayList arrDetailedReport, ArrayList arrConsolidateDetailedReport,
			String xmlFile) {
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

	public static void ValidateFiles(JSONObject jsonObj) throws Exception {

		DJsonParser jspr = new DJsonParser();
		String[] pathString = jspr.getPath(jsonObj);
		String[] xmlFilesPath = pathString[0].split(",");
		for (int eachXmlCount = 0; eachXmlCount < xmlFilesPath.length; eachXmlCount++) {
			DXmlUtiility.IsValidXML(xmlFilesPath[eachXmlCount].split("~")[0]);
		}
	}

	public static void acknowledge(String jsonString) throws Exception {
		DUtils utls = new DUtils();
		JSONObject jsonObj = DJsonParser.ParseJson(jsonString);
		DJsonParser jsonPars = new DJsonParser();
		String version = (String) jsonObj.get("version");
		MessageQueue.VERSION = version;
		MessageQueue.WORK_ORDER = jsonPars.getJsonValueForKey(jsonObj, "WO");
		MessageQueue.RR_SOURCE = (String) jsonObj.get("source");
		if(MessageQueue.RR_SOURCE == null)
			MessageQueue.RR_SOURCE = "TORNADO_RR";
		Thread.sleep(1000);

		if (utls.CheckRRExistForVersion()) {
			try {
				MessageQueue.TORNADO_ENV = (String) jsonPars.getJsonValueFromGroupKey(jsonObj, "region", "env");
				if (MessageQueue.TORNADO_ENV.equals("development"))
					MessageQueue.TORNADO_HOST = MessageQueue.TORNADO_HOST_DEV;
				else if (MessageQueue.TORNADO_ENV.equals("production"))
					MessageQueue.TORNADO_HOST = MessageQueue.TORNADO_HOST_LIVE_1;
				else if (MessageQueue.TORNADO_ENV.equals("qa"))
					MessageQueue.TORNADO_HOST = MessageQueue.TORNADO_HOST_QA;
				else {
					log.error(MessageQueue.WORK_ORDER + ": " + "Issue with environment value, process terminated.");
					DThrowException.CustomExitWithErrorMsgID(new Exception("Invalid Json: "),
							"Invalid JSON environment value from Tornado", "14");
				}
			} catch (Exception ex) {
				log.error(MessageQueue.WORK_ORDER + ": " + "Issue with environment value, process terminated.");
				DThrowException.CustomExitWithErrorMsgID(new Exception("Invalid Json: "),
						"Invalid JSON environment value from Tornado", "14");
			}

			DINIReader iniRdr = new DINIReader();
			iniRdr.writeValueforKey(MessageQueue.TORNADO_HOST);

			DFileSystem fls = new DFileSystem();
			fls.CreateFile("Report.txt");
			fls.CreateFile("error.txt");
			fls.CreateFile("Export.txt");

			DINetwork iNet = new DINetwork();

			MessageQueue.MSGID = (String) jsonObj.get("Id");
			DDataOutput DO = new DDataOutput();
			DO.GetAllPath(jsonObj);

			Boolean ReRun3DXMLBool = jsonPars.getJsonBooleanValueForKey(jsonObj, "region", "ReRun3DXML");
			if(ReRun3DXMLBool != null)
			if(ReRun3DXMLBool == true)
			{
				ReRun3DXML(jsonObj);
			}
			else
			{
			// ***PnG***// This is to copy from different part to 050_Production.

			String copyFileStatus = "";
			if (DDataOutput.MASTER_TEMPLATE_PATH != null && !DDataOutput.MASTER_TEMPLATE_PATH.isEmpty())
				if (!DDataOutput.MASTER_TEMPLATE_PATH.isEmpty()) {
					copyFileStatus = utls.CopyFileFromSourceToDestination(DDataOutput.MASTER_TEMPLATE_PATH, DDataOutput.MASTER_ART_PATH);
				}
			// ***PnG**//
			
			Thread.sleep(1000);
			if(DDataOutput.MASTER_TEMPLATE_PATH != null && !DDataOutput.MASTER_TEMPLATE_PATH.isEmpty())
			{
			if (copyFileStatus != "" && !DDataOutput.MASTER_TEMPLATE_PATH.isEmpty() && DDataOutput.MASTER_TEMPLATE_PATH != null) {
				if (!((String) jsonObj.get("type")).equals("multi"))
					ValidateFiles(jsonObj);
				try {

					String workOrderNo = jsonPars.getJsonValueForKey(jsonObj, "WO");
					sendRespStatusMsg("received" + "::" + iNet.GetClientIPAddr());
					log.info(MessageQueue.WORK_ORDER + ": " + "Message received acknowledgement for job id  '"
							+ MessageQueue.MSGID + "' " + " WO: " + workOrderNo);

					if (!((String) jsonObj.get("type")).equals("multi"))
						actionSeq(jsonObj, true);
					else
						multiActionSeq(jsonObj);
				} 
				catch (FileSystemException fileExp)
				{
					System.out.println(fileExp.getMessage());
					log.error(MessageQueue.WORK_ORDER + ": " + "File system exception : " + fileExp.getMessage());					
					DThrowException.CatchExceptionWithErrorMsgId(fileExp, "File system exception : " + fileExp.getMessage(), "14");
				}
				catch (Exception ex) 
				{
					System.out.println(ex.getMessage());
					log.error(MessageQueue.WORK_ORDER + ": " + "Msg Ack err: " + ex);
					DThrowException.CatchExceptionWithErrorMsgId(ex, ex.getMessage(), "14");
				}
			} 
			}else {
				if (!((String) jsonObj.get("type")).equals("multi"))
					ValidateFiles(jsonObj);
				try {

					String workOrderNo = jsonPars.getJsonValueForKey(jsonObj, "WO");
					sendRespStatusMsg("received" + "::" + iNet.GetClientIPAddr());
					log.info(MessageQueue.WORK_ORDER + ": " + "Message received acknowledgement for job id  '"
							+ MessageQueue.MSGID + "' " + " WO: " + workOrderNo);

					if (!((String) jsonObj.get("type")).equals("multi"))
						actionSeq(jsonObj, true);
					else
						multiActionSeq(jsonObj);
				} catch (Exception ex) {
					log.error(MessageQueue.WORK_ORDER + ": " + "Msg Ack err: " + ex);
				}
			}
			}
		} else {
			DThrowException.CustomExitWithErrorMsgID(new Exception("Invalid version: "),
					"RR plugin error: check illustrator version used or RR plugin doesn't exist for that version: "
							+ "/Applications/Adobe Illustrator " + MessageQueue.VERSION,
					"14");

		}

	}

	public static void sendRespStatusMsg(String status) throws Exception {
		try {
			log.info(MessageQueue.WORK_ORDER + ": " + "Before calling send report status message post method");
			String postResponse = DHttpConnection.excutePost("http://" + MessageQueue.HOST_IP + ":8080/AAW/message/resp",
					MessageQueue.MSGID + "::" + status);
			log.info(MessageQueue.WORK_ORDER + ": " + "Status of sending report status msg response " + postResponse);
		} catch (Exception ex) {
			log.error(ex);
		}

	}

	public static void sendStatusMsg(String status) throws Exception {
		try {
			log.info(MessageQueue.WORK_ORDER + ": " + "Before calling send status message post method");
			String postResponse = DHttpConnection.excutePost(
					"http://" + MessageQueue.HOST_IP + ":8080/AAW/message/error", MessageQueue.MSGID + "::" + status);

			log.info(MessageQueue.WORK_ORDER + ": " + "Status of sending error msg response " + postResponse);
		} catch (Exception ex) {
			log.error(ex);
		}

	}

	public static void UpdateToServer(String actionStr, boolean repeatCheck) throws IOException {
		URL urlStr = new URL(
				MessageQueue.TORNADO_HOST + "/rest/pub/aaw/" + actionStr + "?mqid=" + MessageQueue.MSGID);
		int timeoutMin = 2;
		int milliseconds = 30000;
		try {
			DHttpsConnection httpsCon = new DHttpsConnection();
			HttpURLConnection connection;

			log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr.toString());
			connection = (httpsCon.getURLConnection(urlStr, true));
			log.info(MessageQueue.WORK_ORDER + ": " + "received response of url: " + urlStr.toString() + " - response " + connection.getResponseCode());
			if (connection != null) {
				log.info(MessageQueue.WORK_ORDER + ": " + "Waiting for response of url: " + urlStr.toString());
				connection.setConnectTimeout(milliseconds * timeoutMin);
				connection.setReadTimeout(milliseconds * timeoutMin);
			}
			if (connection == null) {
				// System.out.println("XML compare : API connection failed");
				log.error(
						MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed - " + urlStr.toString());
			}
			if ((connection.getResponseCode() != HttpURLConnection.HTTP_OK || connection == null)
					&& MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_1)
					&& MessageQueue.TORNADO_ENV.equals("production")) {

				if (connection != null)
					connection.disconnect();

				DHttpsConnection httpsCon2 = new DHttpsConnection();
				HttpURLConnection connection2;

				URL urlStr_2 = new URL(MessageQueue.TORNADO_HOST_LIVE_2 + "/rest/pub/aaw/" + actionStr + "?mqid="
						+  MessageQueue.MSGID);
				try {

					log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_2.toString());
					connection2 = (httpsCon2.getURLConnection(urlStr_2, true));
					log.info(MessageQueue.WORK_ORDER + ": " + "received response of url: " + urlStr_2.toString() + " - response " + connection2.getResponseCode());

					if (connection2 != null) {
						log.info(
								MessageQueue.WORK_ORDER + ": " + "Waiting for response of url: " + urlStr_2.toString());
						connection2.setConnectTimeout(milliseconds * timeoutMin);
						connection2.setReadTimeout(milliseconds * timeoutMin);
					}
					if (connection2 == null) {
						// System.out.println("XML compare : API connection failed");
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed - "
								+ urlStr_2.toString());
					}
					if ((connection2.getResponseCode() != HttpURLConnection.HTTP_OK || connection2 == null)
							&& !(MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_2))
							&& MessageQueue.TORNADO_ENV.equals("production")) {
						if (connection2 != null)
							connection2.disconnect();
						URL urlStr_3 = new URL(MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr
								+ "?mqid=" +  MessageQueue.MSGID);
						try {
							DHttpsConnection httpsCon3 = new DHttpsConnection();
							HttpURLConnection connection3;

							log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_3.toString());
							connection3 = (httpsCon3.getURLConnection(urlStr_3, true));
							log.info(MessageQueue.WORK_ORDER + ": " + "received response of url: " + urlStr_3.toString() + " - response " + connection3.getResponseCode());


							if (connection3 != null) {
								log.info(MessageQueue.WORK_ORDER + ": " + "Waiting for response of url: "
										+ urlStr_3.toString());
								connection3.setConnectTimeout(milliseconds * timeoutMin);
								connection3.setReadTimeout(milliseconds * timeoutMin);
							}
							if (connection3 == null) {
								System.out.println("XML compare : API connection failed");
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed - "
										+ urlStr_3.toString());
								DAction.UpdateErrorStatusWithRemark("26",
										"Road Runner not received any response - connection time out");
							} else {
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "
										+ MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid="
										+  MessageQueue.MSGID + " - response : "
										+ connection3.getResponseCode());
								
								if(connection3.getResponseCode() != HttpURLConnection.HTTP_OK)
								{
									if(repeatCheck)
										UpdateToServer(actionStr, false);
									else
									{
										System.out.println("XML compare : " + connection3.getResponseCode());
										DAction.UpdateErrorStatusWithRemark("26",
											"Road Runner received response - " + connection3.getResponseCode());
									}
								}
								else
									System.out.println("XML compare : " + connection3.getResponseCode());
							
							}

							if (connection3 != null)
								connection3.disconnect();
						} catch (java.net.SocketTimeoutException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Http response time out: "
									+ (String) ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26",
									"Road Runner not received any response: " + (String) ex3.getMessage());
						} catch (IOException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Http IO exception: "
									+ ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");
						} catch (Exception ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Error Http connection: "
									+ (String) ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");
						}

					} else {
						System.out.println("XML compare: " + connection2.getResponseCode());
						// log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API response : " +
						// connection.getResponseCode());
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "
								+ MessageQueue.TORNADO_HOST_LIVE_2 + "/rest/pub/aaw/" + actionStr + "?mqid="
								+  MessageQueue.MSGID + " - response : " + connection2.getResponseCode());
						
						if(connection2.getResponseCode() != HttpURLConnection.HTTP_OK)
						{
							DAction.UpdateErrorStatusWithRemark("26",
									"Road Runner received response : " + connection2.getResponseCode());
						}

					}

					if (connection != null)
						connection.disconnect();
				} catch (java.net.SocketTimeoutException ex2) {

					if (!(MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_2))
							&& MessageQueue.TORNADO_ENV.equals("production")) {
						URL urlStr_3 = new URL(MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr
								+ "?mqid=" +  MessageQueue.MSGID);
						try {
							DHttpsConnection httpsCon3 = new DHttpsConnection();
							HttpURLConnection connection3;

							log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_3.toString());
							connection3 = (httpsCon3.getURLConnection(urlStr_3, true));
							log.info(MessageQueue.WORK_ORDER + ": " + "received response of url: " + urlStr_3.toString() + " - response " + connection3.getResponseCode());

							if (connection3 != null) {
								log.info(MessageQueue.WORK_ORDER + ": " + "wating for response - "
										+ urlStr_3.toString());
								connection3.setConnectTimeout(milliseconds * timeoutMin);
								connection3.setReadTimeout(milliseconds * timeoutMin);
							}
							if (connection3 == null) {
								System.out.println("XML compare : API connection failed");
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed");
								DAction.UpdateErrorStatusWithRemark("26",
										"Road Runner not received any response " + "'connection not established'");

							} else {
								
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "
										+ MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid="
										+  MessageQueue.MSGID + " - response : "
										+ connection3.getResponseCode());
								
								if(connection3.getResponseCode() != HttpURLConnection.HTTP_OK)
								{
									if(repeatCheck)
										UpdateToServer(actionStr, false);
									else
									{
										System.out.println("XML compare : " + connection3.getResponseCode());
										DAction.UpdateErrorStatusWithRemark("26",
												"Road Runner received response - " + connection3.getResponseCode());
									}
								}
								else
									System.out.println("XML compare : " + connection3.getResponseCode());
							
							}

							if (connection3 != null)
								connection3.disconnect();
						} catch (java.net.SocketTimeoutException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + MessageQueue.TORNADO_HOST_LIVE_3
									+ " Http response time out: " + (String) ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26",
									"Road Runner not received any response: " + (String) ex3.getMessage());
						} catch (IOException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Http IO exception: "
									+ ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

						} catch (Exception ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Error Http connection: "
									+ (String) ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

						}

					} else {

						log.error(MessageQueue.WORK_ORDER + ": " + MessageQueue.TORNADO_HOST_LIVE_2
								+ " Http response time out: " + (String) ex2.getMessage());
						DAction.UpdateErrorStatusWithRemark("26",
								"Road Runner not received any response: " + (String) ex2.getMessage());
					}

				} catch (IOException ex2) {
					log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString() + " Http IO exception: "
							+ ex2.getMessage());
					DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

				} catch (Exception ex2) {
					log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString() + " Error Http connection: "
							+ (String) ex2.getMessage());
					DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

				}

			} else {
				System.out.println("XML compare: " + connection.getResponseCode());
				// log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API response : " +
				// connection.getResponseCode());
				log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: " + MessageQueue.TORNADO_HOST
						+ "/rest/pub/aaw/" + actionStr + "?mqid=" +  MessageQueue.MSGID + " - response : "
						+ connection.getResponseCode());
				
				if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
				{
					DAction.UpdateErrorStatusWithRemark("26",
							"Road Runner received response : " + connection.getResponseCode());
				}

			}

			if (connection != null)
				connection.disconnect();

		} catch (java.net.SocketTimeoutException ex) {
			// Connect1 catch

			if (MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_1)
					&& MessageQueue.TORNADO_ENV.equals("production")) {

				DHttpsConnection httpsCon2 = new DHttpsConnection();
				HttpURLConnection connection2;

				URL urlStr_2 = new URL(MessageQueue.TORNADO_HOST_LIVE_2 + "/rest/pub/aaw/" + actionStr + "?mqid="
						+  MessageQueue.MSGID);
				try {

					log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_2.toString());
					connection2 = (httpsCon2.getURLConnection(urlStr_2, true));
					log.info(MessageQueue.WORK_ORDER + ": " + "received response of url: " + urlStr_2.toString() + " - response " + connection2.getResponseCode());


					if (connection2 != null) {
						log.info(
								MessageQueue.WORK_ORDER + ": " + "Waiting for response of url: " + urlStr_2.toString());
						connection2.setConnectTimeout(milliseconds * timeoutMin);
						connection2.setReadTimeout(milliseconds * timeoutMin);
					}
					if (connection2 == null) {
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed - "
								+ urlStr_2.toString());
					}
					if ((connection2.getResponseCode() != HttpURLConnection.HTTP_OK || connection2 == null)
							&& !(MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_2))
							&& MessageQueue.TORNADO_ENV.equals("production")) {
						if (connection2 != null)
							connection2.disconnect();
						URL urlStr_3 = new URL(MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr
								+ "?mqid=" +  MessageQueue.MSGID);
						try {
							DHttpsConnection httpsCon3 = new DHttpsConnection();
							HttpURLConnection connection3;

							log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_3.toString());
							connection3 = (httpsCon3.getURLConnection(urlStr_3, true));
							log.info(MessageQueue.WORK_ORDER + ": " + "received response of url: " + urlStr_3.toString() + " - response " + connection3.getResponseCode());


							if (connection3 != null) {
								log.info(MessageQueue.WORK_ORDER + ": " + "Waiting for response of url: "
										+ urlStr_3.toString());
								connection3.setConnectTimeout(milliseconds * timeoutMin);
								connection3.setReadTimeout(milliseconds * timeoutMin);
							}
							if (connection3 == null) {
								System.out.println("XML compare : API connection failed");
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed - "
										+ urlStr_3.toString());
								DAction.UpdateErrorStatusWithRemark("26",
										"Road Runner not received any response - connection time out");
							} else {
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "
										+ MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid="
										+  MessageQueue.MSGID + " - response : "
										+ connection3.getResponseCode());
								
								if(connection3.getResponseCode() != HttpURLConnection.HTTP_OK)
								{
									if(repeatCheck)
										UpdateToServer(actionStr, false);
									else
									{
										System.out.println("XML compare : " + connection3.getResponseCode());
										DAction.UpdateErrorStatusWithRemark("26",
												"Road Runner received response - " + connection3.getResponseCode());
									}
								}
								else
									System.out.println("XML compare : " + connection3.getResponseCode());
							
							}

							if (connection3 != null)
								connection3.disconnect();
						} catch (java.net.SocketTimeoutException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Http response time out: "
									+ (String) ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26",
									"Road Runner not received any response: " + (String) ex3.getMessage());
						} catch (IOException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Http IO exception: "
									+ ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");
						} catch (Exception ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Error Http connection: "
									+ (String) ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");
						}

					} else {
						System.out.println("XML compare: " + connection2.getResponseCode());
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "
								+ MessageQueue.TORNADO_HOST_LIVE_2 + "/rest/pub/aaw/" + actionStr + "?mqid="
								+  MessageQueue.MSGID + " - response : " + connection2.getResponseCode());
						
						if(connection2.getResponseCode() != HttpURLConnection.HTTP_OK)
						{
							DAction.UpdateErrorStatusWithRemark("26",
									"Road Runner received response - " + connection2.getResponseCode());
						}
						
					}

				} catch (java.net.SocketTimeoutException ex2) {

					if (!(MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_2))
							&& MessageQueue.TORNADO_ENV.equals("production")) {
						URL urlStr_3 = new URL(MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr
								+ "?mqid=" +  MessageQueue.MSGID);
						try {
							DHttpsConnection httpsCon3 = new DHttpsConnection();
							HttpURLConnection connection3;

							log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_3.toString());
							connection3 = (httpsCon3.getURLConnection(urlStr_3, true));
							log.info(MessageQueue.WORK_ORDER + ": " + "received response of url: " + urlStr_3.toString() + " - response " + connection3.getResponseCode());

							if (connection3 != null) {
								log.info(MessageQueue.WORK_ORDER + ": " + "wating for response - "
										+ urlStr_3.toString());
								connection3.setConnectTimeout(milliseconds * timeoutMin);
								connection3.setReadTimeout(milliseconds * timeoutMin);
							}
							if (connection3 == null) {
								System.out.println("XML compare : API connection failed");
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed");
								DAction.UpdateErrorStatusWithRemark("26",
										"Road Runner not received any response " + "'connection not established'");

							} else {
								
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "
										+ MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid="
										+  MessageQueue.MSGID + " - response : "
										+ connection3.getResponseCode());
								
								
								if(connection3.getResponseCode() != HttpURLConnection.HTTP_OK)
								{
									if(repeatCheck)
										UpdateToServer(actionStr, false);
									else
									{
										System.out.println("XML compare : " + connection3.getResponseCode());
										DAction.UpdateErrorStatusWithRemark("26",
												"Road Runner received response - " + connection3.getResponseCode());
									}
								}
								else
									System.out.println("XML compare : " + connection3.getResponseCode());
							}

							if (connection3 != null)
								connection3.disconnect();
						} catch (java.net.SocketTimeoutException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + MessageQueue.TORNADO_HOST_LIVE_3
									+ " Http response time out: " + (String) ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26",
									"Road Runner not received any response: " + (String) ex3.getMessage());
						} catch (IOException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Http IO exception: "
									+ ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

						} catch (Exception ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Error Http connection: "
									+ (String) ex3.getMessage());
							DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

						}

					} else {

						log.error(MessageQueue.WORK_ORDER + ": " + MessageQueue.TORNADO_HOST_LIVE_2
								+ " Http response time out: " + (String) ex2.getMessage());
						DAction.UpdateErrorStatusWithRemark("26",
								"Road Runner not received any response: " + (String) ex2.getMessage());
					}

				} catch (IOException ex2) {
					log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString() + " Http IO exception: "
							+ ex2.getMessage());
					DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

				} catch (Exception ex2) {
					log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString() + " Error Http connection: "
							+ (String) ex2.getMessage());
					DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

				}

			} else {
				log.error(MessageQueue.WORK_ORDER + ": Error Http connection 'Failed' "
						+ MessageQueue.TORNADO_HOST_LIVE_1 + " <> " + MessageQueue.TORNADO_HOST);
				DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");
			}

		} catch (IOException ex1) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Http IO exception: " + ex1.getMessage());
			DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response ");
		} catch (Exception ex1) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Error Http connection: " + (String) ex1.getMessage());
			DAction.UpdateErrorStatusWithRemark("26", "Road Runner not received any response ");
		}
	}

	public static void UpdateReport(String reportStr) throws IOException {
		try {
			DHttpsConnection httpsCon = new DHttpsConnection();
			log.info(MessageQueue.WORK_ORDER + ": " + "Before calling update report post method" + " - " + MessageQueue.TORNADO_HOST + "/rest/pub/aaw/finalreport "
					+ MessageQueue.MSGID );
			String postResponse = httpsCon.excuteHttpJsonPost(MessageQueue.TORNADO_HOST + "/rest/pub/aaw/finalreport",
					MessageQueue.MSGID, reportStr);
			log.info(MessageQueue.WORK_ORDER + ": " + "Error report status sent : " + MessageQueue.TORNADO_HOST
					+ "/rest/pub/rr/updatestatus - " + MessageQueue.MSGID + " - " + "\n response : " + postResponse);
			
			if(!(postResponse.toString()).equalsIgnoreCase("{\"status\":\"success\",\"code\":\"0\",\"detailsObj\":null}"))
			{
				
				String postResponse2 = httpsCon.excuteHttpJsonPost(MessageQueue.TORNADO_HOST + "/rest/pub/aaw/finalreport",
						MessageQueue.MSGID, reportStr);
				log.info(MessageQueue.WORK_ORDER + ": " + "Error report status sent : " + MessageQueue.TORNADO_HOST
						+ "/rest/pub/rr/updatestatus - " + MessageQueue.MSGID + " - " + "\n response : " + postResponse2);
				
				if(!(postResponse2.toString()).equalsIgnoreCase("{\"status\":\"success\",\"code\":\"0\",\"detailsObj\":null}"))
				{
					
					String postResponse3 = httpsCon.excuteHttpJsonPost(MessageQueue.TORNADO_HOST + "/rest/pub/aaw/finalreport",
							MessageQueue.MSGID, reportStr);
					log.info(MessageQueue.WORK_ORDER + ": " + "Error report status sent : " + MessageQueue.TORNADO_HOST
							+ "/rest/pub/rr/updatestatus - " + MessageQueue.MSGID + " - " + "\n response : " + postResponse3);
				}
			}
			
		} catch (Exception ex) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Error when updating report " + (String) ex.getMessage());
		}
	}

//	public static void UpdateErrorStatus(String reportStr) throws IOException {
//		try {
//			HttpsConnection httpsCon = new HttpsConnection();
//			log.info(MessageQueue.WORK_ORDER + ": " + "Before calling update error status post method");
//			String postResponse = httpsCon.excuteErrorStatusHttpJsonPost(
//					MessageQueue.TORNADO_HOST + "/rest/pub/rr/updatestatus", MessageQueue.MSGID, reportStr);
//			log.info(MessageQueue.WORK_ORDER + ": " + "Status of sending error status response " + postResponse);
//		} catch (Exception ex) {
//			log.error(MessageQueue.WORK_ORDER + ": " + "Error when sending error status : " + (String) ex.getMessage());
//		}
//	}

	public static void UpdateClientMachineRunningStatus(String ipAddress, String locationKey, String category)
			throws IOException {
		try {
			ipAddress = ipAddress;
			locationKey = locationKey;
			category = category;

			DHttpsConnection httpsCon = new DHttpsConnection();
			
			
			log.info(MessageQueue.WORK_ORDER + ": " + "Before calling heartbeat post method " + ipAddress + " "
					+ locationKey + " " + category);
			String postResponse = httpsCon.excuteClientMachineStatusHttpJsonPost(
					MessageQueue.TORNADO_HOST_LIVE_1 + "/rest/pub/rr/rrstatusOfHeartBeat", ipAddress, locationKey,
					category);
			String postResponseDev = httpsCon.excuteClientMachineStatusHttpJsonPost(
					MessageQueue.TORNADO_HOST_DEV + "/rest/pub/rr/rrstatusOfHeartBeat", ipAddress, locationKey,
					category);
			log.info(MessageQueue.WORK_ORDER + ": " + "Status of sending heartbeat response Live" + postResponse);
			log.info(MessageQueue.WORK_ORDER + ": " + "Status of sending heartbeat response dev " + postResponseDev);
		} catch (Exception ex) {

			log.error(MessageQueue.WORK_ORDER + ": " + "Error while sending HB running status :"
					+ (String) ex.getMessage());
		}
	}

	public static void UpdateErrorStatusWithRemark(String reportStr, String remarks) throws IOException {
		if(MessageQueue.RR_SOURCE != null)
		if(MessageQueue.RR_SOURCE.equalsIgnoreCase("HUBX_RR") || MessageQueue.RR_SOURCE.equalsIgnoreCase("WAVEPNG"))
		{
		try {
			log.info(MessageQueue.WORK_ORDER + ": " + "Before sending error status :" + reportStr + " - remark :"
					+ remarks);
			DHttpsConnection httpsCon = new DHttpsConnection();
			String postResponse = httpsCon.excuteErrorStatusHttpJsonPostWithRemark(
					MessageQueue.TORNADO_HOST + "/rest/pub/rr/updatestatus", MessageQueue.MSGID, reportStr, remarks);
			log.info(MessageQueue.WORK_ORDER + ": " + "Error status sent : " + MessageQueue.TORNADO_HOST
					+ "/rest/pub/rr/updatestatus" + MessageQueue.MSGID + " - " + reportStr + "\n remark : " + remarks
					+ "\n response : " + postResponse);
			
			if(!(postResponse.toString()).equalsIgnoreCase("{\"status\":\"success\",\"code\":\"0\",\"detailsObj\":\"POST Received\"}"))
			{
				
				String postResponse2 = httpsCon.excuteErrorStatusHttpJsonPostWithRemark(
						MessageQueue.TORNADO_HOST_LIVE_2 + "/rest/pub/rr/updatestatus", MessageQueue.MSGID, reportStr, remarks);
				log.info(MessageQueue.WORK_ORDER + ": " + "Error status sent : " + MessageQueue.TORNADO_HOST
						+ "/rest/pub/rr/updatestatus" + MessageQueue.MSGID + " - " + reportStr + "\n remark : " + remarks
						+ "\n response : " + postResponse2);
				
				if(!(postResponse2.toString()).equalsIgnoreCase("{\"status\":\"success\",\"code\":\"0\",\"detailsObj\":\"POST Received\"}"))
				{
					
					String postResponse3 = httpsCon.excuteErrorStatusHttpJsonPostWithRemark(
							MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/rr/updatestatus", MessageQueue.MSGID, reportStr, remarks);
					log.info(MessageQueue.WORK_ORDER + ": " + "Error status sent : " + MessageQueue.TORNADO_HOST
							+ "/rest/pub/rr/updatestatus" + MessageQueue.MSGID + " - " + reportStr + "\n remark : " + remarks
							+ "\n response : " + postResponse3);
				}
				
			}
			
		} catch (Exception ex) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Error when updating error status with remark :"
					+ (String) ex.getMessage());
		}
	}
	}

	public static void FindMissingFonts(String[] appFonts, String[] docFonts) throws Exception {
		DUtils utls = new DUtils();
		DFileSystem fls = new DFileSystem();
		ArrayList<String> missingFonts = utls.GetMissingFonts(appFonts, docFonts);
		if (missingFonts.size() > 0) {
			fls.AppendFileString("Document font not found:" + missingFonts.toString() + "\n");
			MessageQueue.ERROR += "\nDocument linked font missing \n";
		}
	}

	public static void FindMissingFiles(String[] arrFiles) throws Exception {

		DUtils utls = new DUtils();
		DFileSystem fls = new DFileSystem();
		ArrayList<String> missingFiles = new ArrayList<String>();
		{
			int arrLen = arrFiles.length;
			if (arrLen > 0) {
				for (int i = 0; i < arrLen; i++) {
					if (!utls.FileExists(arrFiles[i])) {
						missingFiles.add(arrFiles[i]);
					}
				}
				List<String> duplicateList = missingFiles;
				HashSet<String> listToSet = new HashSet<String>(duplicateList);
				List<String> listWithoutDuplicates = new ArrayList<String>(listToSet);
				missingFiles = (ArrayList<String>) listWithoutDuplicates;
			}

		}

		if (missingFiles.size() > 0) {
			fls.AppendFileString("Document file not found :" + missingFiles.toString() + "\n");
			MessageQueue.ERROR += "\nDocument linked file missing \n";
		}
	}

	public static void AddVolumes() throws Exception {
		List<String[]> rowList = new ArrayList<String[]>();
		DUtils utls = new DUtils();
		rowList = utls.ReadXLSXFile(utls.GetPathFromResource("SMB.xlsx"), "sheet1");
		for (String[] row : rowList) {
			int noOfShareFolder = row.length - 1;
			for (int inc = 0; inc < noOfShareFolder; inc++) {
				String status = DSEng.MountVolume(row[0], "", "", row[row.length - inc - 1]);
				if (status.contains("Error")) {
					DAction.UpdateErrorStatusWithRemark("20", "Volume not able to mount, " + "server: " + row[0]
							+ " share directory: " + row[row.length - inc - 1]);
				}
			}
		}
	}

	public static void Mount() throws Exception {
		String[] arg = null;
		DUtils utl = new DUtils();
		arg = utl.ReadFromExcel("SMB.xls", true, 0, false, 0, false);
		// int noOfShareFolder = arg.length - 3;
		int noOfShareFolder = arg.length - 1;
		for (int inc = 0; inc < noOfShareFolder; inc++) {
			// SEng.MountVolume(arg[0], arg[1], arg[2], arg[arg.length - inc - 1]);
			// SEng.MountVolume(arg[0], "", "", arg[arg.length - inc - 1]);
		}

	}

	public static void main(String args[]) throws Exception {
		MessageQueue.VERSION = "CC 2018";
		
		List<String> fromList = new ArrayList();
		
		fromList.add("Color Merge 6");
		fromList.add("Color Merge 7");

		
		
		//SwatchMergeFromXML("/Users/yuvaraj/Desktop/TEST/PENANG ! PNG/Color Merge/GS1_40188848201_3.xml", "SL_ColorName","Color Merge 6", "PANTONE 152 C");
		//SwatchMergeFromXML("/Users/yuvaraj/Desktop/TEST/PENANG ! PNG/Color Merge/GS1_40188848201_3.xml", "SL_ColorName", fromList, "PANTONE");
		
		
		
		//UpdateClientMachineRunningStatus("10.129.128.35","PNGNCL","Wave Road Runner");
		

	//	SEng.SetLayerVisibleOff(); //// only for NCL P - N - G
	//	SwatchMergeFromXML("/Users/yuvaraj/Desktop/TEST/PENANG ! PNG/Color Merge/GS1_40188848201_3.xml", "SL_ColorName");
		String brackGroundReferenceURI = "";
		String logoReferenceURI = "";
		DUtils utils = new DUtils();
		DFileSystem fls = new DFileSystem();
		DJsonParser jspr = new DJsonParser();
		
		String rr3DJson = fls.ReadFile("/Users/yuvaraj/Desktop/TEST/401893123-PNGNCL/050_Production_Art/052_PA_Working_Files/01_Roadrunner/RRremap.txt");
		System.out.println(rr3DJson);
		try
		{
		JSONObject rr3DJsonObj = jspr.ParseJson(rr3DJson.toString());
		

		brackGroundReferenceURI = (String) rr3DJsonObj.get("ESG-backgroundfilename-NA-0001");
		logoReferenceURI = (String) rr3DJsonObj.get("ESG-logos-LO-0001");
		
		System.out.println(brackGroundReferenceURI +" \n "+ logoReferenceURI);
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		
	}

}
