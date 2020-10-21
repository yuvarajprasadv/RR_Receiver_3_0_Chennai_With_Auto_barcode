package Rcvr_AAMQ;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystemException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;


import Rcvr_AAMQ.Utils;

public class Action {
	static Logger log = LogMQ.monitor("Rcvr_AAMQ.Action");

	public static void actionSeq_Dev(JSONObject jsonObj, boolean repeat) throws Exception {
		JsonParser jspr = new JsonParser();
		XmlUtiility xmlUtil = new XmlUtiility();
		FileSystem fls = new FileSystem();
		Utils utils = new Utils();
		

		log.info(MessageQueue.WORK_ORDER + ": " + "RR started processing");
		

		SEng.CallAdobeIllustrator();
		log.info(MessageQueue.WORK_ORDER + ": " + "Illustrator activated to load file..");

		String[] appFonts = SEng.GetApplicationFonts().split(",");
		Thread.sleep(2000);

		SEng.OpenDocument(jspr.geFilePathFromJson(jsonObj, "Master"));
		log.info(MessageQueue.WORK_ORDER + ": " + "AI file and other dependend file opening..");

		String dcFont = SEng.GetDocumentFonts();
		if (!dcFont.equalsIgnoreCase("error")) {
			if (dcFont.length() != 0) {
				String[] docFonts = (dcFont).split(",");
				Action.FindMissingFonts(appFonts, docFonts);
			}
		} else {
			MessageQueue.ERROR += "Document parsing error while finding missing fonts \n";
		}

		String dcFile = SEng.GetDocumentFiles();
		if (!dcFile.equalsIgnoreCase("error")) {
			if (dcFile.length() != 0) {
				String[] docFiles = dcFile.split(",");
				Action.FindMissingFiles(docFiles);
			}
		} else {
			MessageQueue.ERROR += "Document parsing error while finding missing image linked files \n";
		}
		
		
		String xmlFilePath = utils.RemoveForwardSlash((String) jspr.geFilePathFromJson(jsonObj, "XMLFile") );
		String exportXmlFilePath = (xmlFilePath.replace("100_XML", "080_QC")).replace(".xml", "_export.xml");
		
		if(!utils.CreateNewDirectory(exportXmlFilePath.substring(0, exportXmlFilePath.lastIndexOf("/")), false))
		{
			log.error(MessageQueue.WORK_ORDER + ": " + "Directory not able to create :  - "
					+ exportXmlFilePath.toString());
			Action.UpdateErrorStatusWithRemark("23",
					"Export xml File path not found / not able to create directory " + exportXmlFilePath);
			repeat = false;
		}
		
		
		
		Thread.sleep(1000);
		Action.UpdateErrorStatusWithRemark("39", "In progress RoadRunner (Running now)");

		String[] arrString = new String[1];
		arrString[0] = MessageQueue.MESSAGE;
		arrString[0] = utils.RemoveForwardSlash(arrString[0]);
		arrString[0] = arrString[0].replace("\"", "'");

		log.info(MessageQueue.WORK_ORDER + ": " + "RoadRunner plugin called");
		SEng.CallTyphoonShadow(arrString);

		Thread.sleep(1000);
		
		String errorMsg = fls.ReadFileReport("error.txt");
		if (errorMsg.contains("\n") && errorMsg.length() != 1)
			MessageQueue.STATUS = false;

		{

			INIReader ini = new INIReader();
			ini.readIniForSingle();

			DataOutput DO = new DataOutput();
			DO.GetAllPath(jsonObj);
			DO.ExportData(DO.FILE_NAME_TO_SAVE);

			if(DataOutput.SWATCH_ENABLE) {
				SwatchMergeFromXML(DataOutput.XML_PATH, DataOutput.SWATCH_NAME, DataOutput.SWATCH_ELEMENT);  // for PENANG alone
			}

			log.info(MessageQueue.WORK_ORDER + ": " + "Pdf and xml generated..");

			//// ----PNG---// Only 3D xml
			JsonParser jsonPars = new JsonParser();
		//	boolean is3DXMLAI = jsonPars.getJsonBooleanValueForKey(jsonObj, "region", "RR3DXML");
			if (DataOutput.IS_RR3DXML && MessageQueue.TORNADO_ENV.equals("development")) {
				
				Utils utls = new Utils();
				String sourceFile = jsonPars.getMasterAIWithoutPathValidate(jsonObj, "Master");
				String destinationFilePath = DataOutput.MAIN_PATH + DataOutput.SOURCE_FILE_DESTINATION_MOVE_PATH;

				String moveFileStatus = "";
				moveFileStatus = utls.MoveFileFromSourceToDestination(sourceFile, destinationFilePath);
				
				String jsonString = "";
				
				FileSystem fileSystem =  new FileSystem();
				
				
				 String brackGroundReferenceURI = "";
				 String logoReferenceURI = "";
				 HashMap<String, String> map = new HashMap<String, String>();
				 XmlUtiility xmlt = new XmlUtiility();
				 map = xmlt.GS1XmlParseAllElement_TMP(DataOutput.MAIN_PATH + DataOutput.RR3DXML_SAVE_PATH + "3DXML.xml", false, brackGroundReferenceURI, logoReferenceURI);

				 brackGroundReferenceURI = map.get("ESG-backgroundfilename-NA-0001");
				 logoReferenceURI = map.get("ESG-logos-LO-0001");
				
				JSONObject rr3DJson = new JSONObject();
				rr3DJson.put("ESG-backgroundfilename-NA-0001", brackGroundReferenceURI);
				rr3DJson.put("ESG-logos-LO-0001", logoReferenceURI);
				
				fileSystem.CreateFile(DataOutput.MAIN_PATH  + DataOutput.RR3DXML_SAVE_PATH,  "RRremap.txt");
				fileSystem.AppendFileString(DataOutput.MAIN_PATH  + DataOutput.RR3DXML_SAVE_PATH + "RRremap.txt", rr3DJson.toJSONString());
				
				fileSystem.CreateFile(DataOutput.MAIN_PATH  + DataOutput.RR3DXML_SAVE_PATH,  "ReRun3DXML.txt");
				
				jsonString = jspr.updateJsonForMultipleJob(jsonObj, DataOutput.RR3DXML_SAVE_PATH, "3DXML.xml", true, DataOutput.FILE_NAME_TO_SAVE);
				
				fileSystem.AppendFileString(DataOutput.MAIN_PATH  + DataOutput.RR3DXML_SAVE_PATH + "ReRun3DXML.txt", jsonString);

			}
			if((MessageQueue.RR_SOURCE.equals("HUBX_RR") || MessageQueue.RR_SOURCE.equals("WAVEPNG")) && MessageQueue.TORNADO_ENV.equals("development"))
			{
				// ***PnG***// This is to Move from different part to 051_PA_Previous.

				Utils utls = new Utils();
				String sourceFile = jsonPars.getMasterAIWithoutPathValidate(jsonObj, "Master");
				String destinationFilePath = DataOutput.MAIN_PATH + DataOutput.SOURCE_FILE_DESTINATION_MOVE_PATH;

				String moveFileStatus = "";
				if(!sourceFile.equalsIgnoreCase(""))
				moveFileStatus = utls.MoveFileFromSourceToDestination(sourceFile, destinationFilePath);

				// ***PnG**//
			}


		}
		Thread.sleep(2000);
		SEng.PostDocumentClose();

		sendRespStatusMsg("delivered");
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed process for job id  '" + MessageQueue.MSGID + "' ");
		Thread.sleep(1000);

		if(utils.FileExists(exportXmlFilePath))
		{
			Action.UpdateToServer("xmlcompare", false);
			log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparison completed..");
		}
		else
		{
			Action.UpdateToServer("xmlcompare", false);
			log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparison completed..");
			String exportXmlReport = fls.ReadFileReport("Export.txt");
			if(exportXmlReport.length() > 5 )
			{
			log.error(MessageQueue.WORK_ORDER + ": " + "Export xml not exits on the path " +  exportXmlFilePath);
			Action.UpdateErrorStatusWithRemark("14", "Export XML not generated " + exportXmlFilePath  + "Error : " + exportXmlReport.toString());
			}
		}

		Action.UpdateReport(fls.ReadFileReport("Report.txt"));
		
		MessageQueue.ERROR += errorMsg;
		Action.sendStatusMsg((String) MessageQueue.ERROR);

		MessageQueue.ERROR = "";
		MessageQueue.WORK_ORDER = "";
		Thread.sleep(1000);
		MessageQueue.GATE = true;
	}
	
	public static void ReRun3DXML_Dev(JSONObject jsonObj) throws Exception
	{
		JsonParser jspr = new JsonParser();
		XmlUtiility xmlUtil = new XmlUtiility();
		FileSystem fls = new FileSystem();
		Utils utils = new Utils();

		Thread.sleep(1000);
		
		log.info(MessageQueue.WORK_ORDER + ": " + "RR 3DXML started processing");

		SEng.CallAdobeIllustrator();
		log.info(MessageQueue.WORK_ORDER + ": " + "Illustrator activated to load file..");
		
		SEng.OpenDocument(jspr.geFilePathFromJson(jsonObj, "Master"));
		log.info(MessageQueue.WORK_ORDER + ": " + "AI file and other dependend file opening..");
		
		Thread.sleep(1000);

		{
			INIReader ini = new INIReader();
			ini.readIniForSingle();
			
			String renderPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path");
			renderPath = renderPath + "090_Deliverables/095_Renders/";
			if(!Utils.IsFolderExists(renderPath))
				utils.CreateNewDirectory(renderPath, false);
			

			String[] docPath = jspr.getPath(jsonObj);
			String fileNameToSave = xmlUtil.getFileNameFromElement((docPath[0].split("~"))[0]);
			if (fileNameToSave != null) {

				String fileName = utils.GetNewNameIfFileExists(GetLastIndex(docPath[1]) + "/" + fileNameToSave + ".ai");

				int index = fileName.lastIndexOf("/");
				fileName = fileName.substring(index + 1, fileName.length());
				fileNameToSave = fileName.split("\\.")[0];
			}

			String[] fileName = new String[4];
			if (fileNameToSave != null) {
				fileName[0] = "none"; // dummy
				fileName[1] = renderPath + fileNameToSave;
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[1]) + "/" + fileNameToSave;
			} else //// After renmaing to <workorder no>.ai
			{
				fileNameToSave = jspr.getJsonValueForKey(jsonObj, "WO");
				fileName[1] = renderPath + fileNameToSave;
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[1]) + "/" + fileNameToSave;
			} // // After renmaing to <workorder no>.ai

			if (MessageQueue.sPdfNormal) {
				if (fileNameToSave != null) {
					SEng.PostDocumentProcessForSingleJobFilename(fileName);
				} else
					SEng.PostDocumentProcess(jspr.getPath(jsonObj));
			} else if (MessageQueue.sPdfPreset) {

				String pdfPreset[] = utils.getPresetFileFromDirectory(
						utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")), "joboptions");
				String[] pdfPresetArr = new String[2];
				if (pdfPreset.length != 0) {
					pdfPresetArr[0] = utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")) + "/"
							+ pdfPreset[0];
					pdfPresetArr[1] = pdfPreset[0].split("\\.")[0];
					String resultPdfExport = "";
					if (fileNameToSave != null)
						resultPdfExport = SEng.PostDocMultiPDFPreset(fileName, pdfPresetArr);
					else {
						String[] dcPath = new String[4];
						dcPath[0] = "";
						dcPath[1] = renderPath;
						dcPath[2] = docPath[2];
						dcPath[3] = docPath[1];
						resultPdfExport = SEng.PostDocMultiPDFPreset(dcPath, pdfPresetArr);
					}
					if (!resultPdfExport.equalsIgnoreCase("null")) {
						fls.AppendFileString("\n PDF with preset export failed: " + resultPdfExport + " \n");
						MessageQueue.ERROR += resultPdfExport + "\n";
					}
				} else {
					fls.AppendFileString(
							"\n PDF with preset export failed, preset file not found in master ai file path \n");
					MessageQueue.ERROR += "\n PDF with preset export failed, preset file not found in master ai file path \n";
				}
			} else if (MessageQueue.sPdfNormalised) {
				if (PostMultipleJobPreProcess()) {
					
					
					docPath[2] = docPath[2].split("\\.")[0];
					String resultPdfExport = "";
					if (fileNameToSave != null)
						resultPdfExport = SEng.PostDocumentMultipleProcess(fileName);
					else {
						String[] dcPath = new String[4];
						dcPath[0] = "";
						dcPath[1] = renderPath;
						dcPath[2] = docPath[2];
						dcPath[3] = docPath[1];
						resultPdfExport = SEng.PostDocumentMultipleProcess(dcPath);
					}
					if (!resultPdfExport.equalsIgnoreCase("null")) {
						fls.AppendFileString("\n Normalized PDF export failed: " + resultPdfExport + " \n");
						MessageQueue.ERROR += resultPdfExport + "\n";
					}
				}
			}

			log.info(MessageQueue.WORK_ORDER + ": " + "Pdf and xml generated..");
		}

		String[] fileName = new String[4];
	
		String jsonString = "";
		String primaryPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path");
		
		if (utils.FileExists(primaryPath + DataOutput.RR3DXML_SAVE_PATH +"3DXML.xml")) 
		{
			jsonString = jspr.updateJsonForMultipleJob(jsonObj, DataOutput.RR3DXML_SAVE_PATH, "3DXML.xml");
			
			
			String brackGroundReferenceURI = "";
			String logoReferenceURI = "";
			XmlUtiility xmlt = new XmlUtiility();
			String rr3DJson = fls.ReadFile(DataOutput.MAIN_PATH + DataOutput.RR3DXML_SAVE_PATH + "RRremap.txt");
			
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
			   
			 xmlt.GS1XmlParseAllElement_TMP(DataOutput.MAIN_PATH + DataOutput.RR3DXML_SAVE_PATH + "3DXML.xml", true, brackGroundReferenceURI, logoReferenceURI);
			 Thread.sleep(2000);
			
			
		}


		if (jsonString != "") {
			String[] newArryStr = new String[1];

			newArryStr[0] = jsonString;
			newArryStr[0] = utils.RemoveForwardSlash(newArryStr[0]);
			newArryStr[0] = newArryStr[0].replace("\"", "'");
			
			Action.UpdateErrorStatusWithRemark("37", "3DXML Started");

			SEng.CallTyphoonShadow(newArryStr);
			Thread.sleep(1000);

			// PNG set swatch White color to White 2
			SEng.SetSwathColorFromTo("White 2", "White"); //// only for NCL P - N - G
			SEng.SetLayerVisibleOff(); //// only for NCL P - N - G

			Thread.sleep(1000);
			String fileRenameString = jspr.getJsonValueForKey(jsonObj, "WO") + "_3dxml";

			String road_runnerDirPath = utils.RemoveForwardSlash(
					(String) jspr.getJsonValueForKey(jsonObj, "Path") + XML3D_Save_Path + "/");
			if (!Utils.IsFolderExists(road_runnerDirPath)) {
				utils.CreateNewDirectory(road_runnerDirPath, false);
			}

			fileName[0] = utils.RemoveForwardSlash(
					(String) jspr.getJsonValueForKey(jsonObj, "Path") + XML3D_Save_Path + "/");

			fileName[1] = road_runnerDirPath + "/" + fileRenameString;
			fileName[2] = fileName[0] + fileRenameString;
			fileName[3] = fileName[0] + fileRenameString;
			SEng.PostDocumentProcessFor3DXML(fileName);
			Thread.sleep(3000);

			SEng.PostDocumentClose();
			Action.UpdateErrorStatusWithRemark("38", "3DXML Completed");
			Thread.sleep(1000);
			
			Action.UpdateToServer("xmlcompare", false);
			
			MessageQueue.ERROR = "";
			MessageQueue.WORK_ORDER = "";
			Thread.sleep(1000);
			MessageQueue.GATE = true;
		}

	
	}
	
	public static void multiActionSeq_Dev(JSONObject jsonObj) throws Exception {
		JsonParser jspr = new JsonParser();
		XmlUtiility xmlUtil = new XmlUtiility();
		FileSystem fls = new FileSystem();
		Utils utils = new Utils();

		utils.CreateNewDirectory(jspr.getXmlFolderPathFromJson(jsonObj, "XMLFile") + "DummyFolder", true);

		String pdfPreset[] = utils.getPresetFileFromDirectory(
				utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")), "joboptions");

		String xmlFiles[] = utils.getFileFromDirectory(jspr.getXmlFolderPathFromJson(jsonObj, "XMLFile"), "xml");
		ArrayList arrErrReport = new ArrayList();
		ArrayList arrDetailedReport = new ArrayList();
		ArrayList arrConsolidateErrorReport = new ArrayList();
		ArrayList arrConsolidateDetailedReport = new ArrayList();

		SEng.CallAdobeIllustrator();
		log.info(MessageQueue.WORK_ORDER + ": " + "Illustrator activated to load file..");

		String[] appFonts = SEng.GetApplicationFonts().split(",");
		Thread.sleep(5000);

		SEng.OpenDocument(jspr.geFilePathFromJson(jsonObj, "Master"));
		log.info(MessageQueue.WORK_ORDER + ": " + "AI file and other dependent file opening..");

		String dcFont = SEng.GetDocumentFonts();
		if (dcFont.length() != 0) {
			String[] docFonts = (dcFont).split(",");
			Action.FindMissingFonts(appFonts, docFonts);
		}
		String dcFile = SEng.GetDocumentFiles();
		if (dcFile.length() != 0) {
			String[] docFiles = dcFile.split(",");
			Action.FindMissingFiles(docFiles);
		}

		utils.DeleteDirectory(jspr.getXmlFolderPathFromJson(jsonObj, "XMLFile") + "DummyFolder");
		Thread.sleep(1000);

		INIReader ini = new INIReader();
		ini.readIniForMultiple();

		String[] pdfPresetArr = new String[2];
		if (MessageQueue.mPdfPreset) {
			if (pdfPreset.length != 0) {
				pdfPresetArr[0] = utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")) + "/" + pdfPreset[0];
				pdfPresetArr[1] = pdfPreset[0].split("\\.")[0];
			}
		} else if (MessageQueue.mPdfNormalised) {
			PostMultipleJobPreProcess();
		}

		String xmlDirPath = jspr.getXmlDirPath(jsonObj);
		for (int eachXmlCount = 0; eachXmlCount < xmlFiles.length; eachXmlCount++) {
			MessageQueue.MESSAGE = jspr.updateJsonForMultipleJob(jsonObj, xmlDirPath, xmlFiles[eachXmlCount]);
			String[] docPath = jspr.getMultiPath(jsonObj, xmlFiles[eachXmlCount]);
			if (!XmlUtiility.multipleJobIsValidXml(docPath[0])) {
				ConsolidateErrorReport(fls, arrErrReport, arrConsolidateErrorReport, arrDetailedReport,
						arrConsolidateDetailedReport, xmlFiles[eachXmlCount]);
				continue;
			}
			String fileNameToSave = xmlUtil.getFileNameFromElement(docPath[0]);
			// docPath[0] += "~0";

			String[] arrString = new String[1];
			arrString[0] = MessageQueue.MESSAGE;
			arrString[0] = utils.RemoveForwardSlash(arrString[0]);
			arrString[0] = arrString[0].replace("\"", "'");

			SEng.CallTyphoonShadow(arrString);

			log.info(MessageQueue.WORK_ORDER + ": " + "RoadRunner plugin called");

			String renderPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path");
			renderPath = renderPath + "090_Deliverables/095_Renders/";
			
		      String directories = renderPath;
		      File file = new File(directories);
		      file.mkdirs();
			
			String[] fileName = new String[4];
			if (fileNameToSave != null) {

				fileName[0] = "none"; // dummy
				fileName[1] = renderPath + fileNameToSave;
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[3]) + "/" + fileNameToSave;

			}
			else
			{
				fileNameToSave =  jspr.getJsonValueForKey(jsonObj, "WO") + "_" +  xmlFiles[eachXmlCount];
				fileName[0] = "none"; // dummy
				fileName[1] = renderPath + fileNameToSave;
				fileName[2] = docPath[2];
				fileName[3] = docPath[3];
				
			}

			if (MessageQueue.mPdfNormal) {
				if (fileNameToSave != null) {
					SEng.PostDocumentProcessForSingleJobFilename(fileName);
				} else {
					SEng.PostDocumentProcessForSingleJobFilename(docPath);
				}
			} else if (MessageQueue.mPdfPreset) {
				if (pdfPreset.length != 0) {
					String resultPdfExport = "";
					if (fileNameToSave != null)
						resultPdfExport = SEng.PostDocMultiPDFPreset(fileName, pdfPresetArr);
					else
						resultPdfExport = SEng.PostDocMultiPDFPreset(docPath, pdfPresetArr);
					if (!resultPdfExport.equalsIgnoreCase("null")) {
						fls.AppendFileString("\n PDF with preset export failed: " + resultPdfExport + " \n");
						MessageQueue.ERROR += resultPdfExport + "\n";
					}
				} else {
					fls.AppendFileString(
							"\n PDF with preset export failed, preset file not found in master ai file path \n");
					MessageQueue.ERROR += "\n PDF with preset export failed, preset file not found in master ai file path \n";
				}
			} else if (MessageQueue.mPdfNormalised) {
				docPath[2] = docPath[2].split("\\.")[0];
				String resultPdfExport = "";
				if (fileNameToSave != null)
					resultPdfExport = SEng.PostDocumentMultipleProcess(fileName);
				else
					resultPdfExport = SEng.PostDocumentMultipleProcess(docPath);
				if (!resultPdfExport.equalsIgnoreCase("null")) {
					fls.AppendFileString("\n Normalized PDF export failed: " + resultPdfExport + " \n");
					MessageQueue.ERROR += resultPdfExport + "\n";
				}
			}
			Thread.sleep(4000);
			log.info(MessageQueue.WORK_ORDER + ": " + "Pdf and xml generated..");
			ConsolidateErrorReport(fls, arrErrReport, arrConsolidateErrorReport, arrDetailedReport,
					arrConsolidateDetailedReport, xmlFiles[eachXmlCount]);
		}

		Thread.sleep(7000);
		SEng.PostDocumentClose();
		sendRespStatusMsg("delivered");
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed process for job id  '" + MessageQueue.MSGID + "' ");

		Action.UpdateToServer("xmlcompare", false);
		log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparision completed..");

		Action.sendStatusMsg(arrConsolidateErrorReport.toString());
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed sending error report..");
		Action.UpdateReport(arrConsolidateDetailedReport.toString());
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed sending of detailed report..");

		MessageQueue.ERROR = "";
		Thread.sleep(1000);
		MessageQueue.GATE = true;
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed job..");
	}

	
	
	
	public static void actionSeq(JSONObject jsonObj, boolean repeat) throws Exception {
		JsonParser jspr = new JsonParser();
		XmlUtiility xmlUtil = new XmlUtiility();
		FileSystem fls = new FileSystem();
		Utils utils = new Utils();
		

		log.info(MessageQueue.WORK_ORDER + ": " + "RR started processing");
		

		SEng.CallAdobeIllustrator();
		log.info(MessageQueue.WORK_ORDER + ": " + "Illustrator activated to load file..");

		String[] appFonts = SEng.GetApplicationFonts().split(",");
		Thread.sleep(2000);

		SEng.OpenDocument(jspr.geFilePathFromJson(jsonObj, "Master"));
		log.info(MessageQueue.WORK_ORDER + ": " + "AI file and other dependend file opening..");

		String dcFont = SEng.GetDocumentFonts();
		if (!dcFont.equalsIgnoreCase("error")) {
			if (dcFont.length() != 0) {
				String[] docFonts = (dcFont).split(",");
				Action.FindMissingFonts(appFonts, docFonts);
			}
		} else {
			MessageQueue.ERROR += "Document parsing error while finding missing fonts \n";
		}

		String dcFile = SEng.GetDocumentFiles();
		if (!dcFile.equalsIgnoreCase("error")) {
			if (dcFile.length() != 0) {
				String[] docFiles = dcFile.split(",");
				Action.FindMissingFiles(docFiles);
			}
		} else {
			MessageQueue.ERROR += "Document parsing error while finding missing image linked files \n";
		}
		
		
		String xmlFilePath = utils.RemoveForwardSlash((String) jspr.geFilePathFromJson(jsonObj, "XMLFile") );
		String exportXmlFilePath = (xmlFilePath.replace("100_XML", "080_QC")).replace(".xml", "_export.xml");
		
		if(!utils.CreateNewDirectory(exportXmlFilePath.substring(0, exportXmlFilePath.lastIndexOf("/")), false))
		{
			log.error(MessageQueue.WORK_ORDER + ": " + "Directory not able to create :  - "
					+ exportXmlFilePath.toString());
			Action.UpdateErrorStatusWithRemark("23",
					"Export xml File path not found / not able to create directory " + exportXmlFilePath);
			repeat = false;
		}
		
		
		
		Thread.sleep(1000);
		Action.UpdateErrorStatusWithRemark("39", "In progress RoadRunner (Running now)");

		String[] arrString = new String[1];
		arrString[0] = MessageQueue.MESSAGE;
		arrString[0] = utils.RemoveForwardSlash(arrString[0]);
		arrString[0] = arrString[0].replace("\"", "'");

		log.info(MessageQueue.WORK_ORDER + ": " + "RoadRunner plugin called");
		SEng.CallTyphoonShadow(arrString);

		Thread.sleep(1000);
		
		
		//// Swatch from xml // for PNG
		
		DataOutput DO = new DataOutput();
		DO.GetAllPath(jsonObj);
		DO.ExportData(DO.FILE_NAME_TO_SAVE);
		
		
		//// Swatch from xml // for PNG
		
		if(DataOutput.SWATCH_ENABLE) {
			SwatchMergeFromXML(DataOutput.XML_PATH, DataOutput.SWATCH_NAME, DataOutput.SWATCH_ELEMENT);  // for PENANG alone
		}
		//// Swatch from xml // for PNG

		

		String errorMsg = fls.ReadFileReport("error.txt");
		if (errorMsg.contains("\n") && errorMsg.length() != 1)
			MessageQueue.STATUS = false;

		{

			INIReader ini = new INIReader();
			ini.readIniForSingle();
			
			String renderPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path");
			renderPath = renderPath + "090_Deliverables/095_Renders/";
			if(!Utils.IsFolderExists(renderPath))
				utils.CreateNewDirectory(renderPath, false);
			

			String[] docPath = jspr.getPath(jsonObj);
			String fileNameToSave = xmlUtil.getFileNameFromElement((docPath[0].split("~"))[0]);
			if (fileNameToSave != null) {

				String fileName = utils.GetNewNameIfFileExists(GetLastIndex(docPath[1]) + "/" + fileNameToSave + ".ai");

				int index = fileName.lastIndexOf("/");
				fileName = fileName.substring(index + 1, fileName.length());
				fileNameToSave = fileName.split("\\.")[0];
			}

			String[] fileName = new String[4];
			if (fileNameToSave != null) {
				fileName[0] = "none"; // dummy
				fileName[1] = renderPath + fileNameToSave;
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[1]) + "/" + fileNameToSave;
			} else //// After renmaing to <workorder no>.ai
			{
				fileNameToSave = jspr.getJsonValueForKey(jsonObj, "WO");
				fileName[1] = renderPath + fileNameToSave;
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[1]) + "/" + fileNameToSave;
			} // // After renmaing to <workorder no>.ai

			if (MessageQueue.sPdfNormal) {
				if (fileNameToSave != null) {
					SEng.PostDocumentProcessForSingleJobFilename(fileName);
				} else
					SEng.PostDocumentProcess(jspr.getPath(jsonObj));
			} else if (MessageQueue.sPdfPreset) {

				String pdfPreset[] = utils.getPresetFileFromDirectory(
						utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")), "joboptions");
				String[] pdfPresetArr = new String[2];
				if (pdfPreset.length != 0) {
					pdfPresetArr[0] = utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")) + "/"
							+ pdfPreset[0];
					pdfPresetArr[1] = pdfPreset[0].split("\\.")[0];
					String resultPdfExport = "";
					if (fileNameToSave != null)
						resultPdfExport = SEng.PostDocMultiPDFPreset(fileName, pdfPresetArr);
					else {
						String[] dcPath = new String[4];
						dcPath[0] = "";
						dcPath[1] = renderPath;
						dcPath[2] = docPath[2];
						dcPath[3] = docPath[1];
						resultPdfExport = SEng.PostDocMultiPDFPreset(dcPath, pdfPresetArr);
					}
					if (!resultPdfExport.equalsIgnoreCase("null")) {
						fls.AppendFileString("\n PDF with preset export failed: " + resultPdfExport + " \n");
						MessageQueue.ERROR += resultPdfExport + "\n";
					}
				} else {
					fls.AppendFileString(
							"\n PDF with preset export failed, preset file not found in master ai file path \n");
					MessageQueue.ERROR += "\n PDF with preset export failed, preset file not found in master ai file path \n";
				}
			} else if (MessageQueue.sPdfNormalised) {
				if (PostMultipleJobPreProcess()) {
					
					
					docPath[2] = docPath[2].split("\\.")[0];
					String resultPdfExport = "";
					if (fileNameToSave != null)
						resultPdfExport = SEng.PostDocumentMultipleProcess(fileName);
					else {
						String[] dcPath = new String[4];
						dcPath[0] = "";
						dcPath[1] = renderPath +  jspr.getJsonValueForKey(jsonObj, "WO");
						dcPath[2] = docPath[2];
						dcPath[3] = docPath[1];
						resultPdfExport = SEng.PostDocumentMultipleProcess(dcPath);
					}
					if (!resultPdfExport.equalsIgnoreCase("null")) {
						fls.AppendFileString("\n Normalized PDF export failed: " + resultPdfExport + " \n");
						MessageQueue.ERROR += resultPdfExport + "\n";
					}
				}
			}

			log.info(MessageQueue.WORK_ORDER + ": " + "Pdf and xml generated..");

			//// ----PNG---// Only 3D xml
			JsonParser jsonPars = new JsonParser();
			boolean is3DXMLAI = jsonPars.getJsonBooleanValueForKey(jsonObj, "region", "RR3DXML");
			if (is3DXMLAI && MessageQueue.TORNADO_ENV.equals("development")) {
				
				Utils utls = new Utils();
				String sourceFile = jsonPars.getMasterAIWithoutPathValidate(jsonObj, "Master");
				String destinationFilePath = DataOutput.MAIN_PATH + DataOutput.SOURCE_FILE_DESTINATION_MOVE_PATH;

				String moveFileStatus = "";
				moveFileStatus = utls.MoveFileFromSourceToDestination(sourceFile, destinationFilePath);
				
				String jsonString = "";
				String XML3D_Save_Path = "";
				String primaryPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path");
				FileSystem fileSystem =  new FileSystem();
				
				
				 String brackGroundReferenceURI = "";
				 String logoReferenceURI = "";
				 HashMap<String, String> map = new HashMap<String, String>();
				 XmlUtiility xmlt = new XmlUtiility();
				 map = xmlt.GS1XmlParseAllElement_TMP(primaryPath + "050_Production_Art/052_PA_Working_Files/01_Roadrunner/3DXML.xml", false, brackGroundReferenceURI, logoReferenceURI);

				 brackGroundReferenceURI = map.get("ESG-backgroundfilename-NA-0001");
				 logoReferenceURI = map.get("ESG-logos-LO-0001");
				
				JSONObject rr3DJson = new JSONObject();
				rr3DJson.put("ESG-backgroundfilename-NA-0001", brackGroundReferenceURI);
				rr3DJson.put("ESG-logos-LO-0001", logoReferenceURI);
				
				fileSystem.CreateFile(primaryPath + "050_Production_Art/052_PA_Working_Files/01_Roadrunner/",  "RRremap.txt");
				fileSystem.AppendFileString(primaryPath + "050_Production_Art/052_PA_Working_Files/01_Roadrunner/RRremap.txt", rr3DJson.toJSONString());
				
				fileSystem.CreateFile(primaryPath + "050_Production_Art/052_PA_Working_Files/01_Roadrunner/",  "ReRun3DXML.txt");
				
				jsonString = jspr.updateJsonForMultipleJob(jsonObj,
						"050_Production_Art/052_PA_Working_Files/01_Roadrunner/", "3DXML.xml", true, fileNameToSave);
				
				fileSystem.AppendFileString(primaryPath + "050_Production_Art/052_PA_Working_Files/01_Roadrunner/ReRun3DXML.txt", jsonString);

			}
			else if(is3DXMLAI && MessageQueue.TORNADO_ENV.equals("production"))
			{

				Thread.sleep(2000);
				String jsonString = "";
				String XML3D_Save_Path = "";
				String primaryPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path");

				if (utils.FileExists(primaryPath + "050_Production_Art/052_PA_Working_Files/01_Roadrunner/3DXML.xml")) 
				{
					jsonString = jspr.updateJsonForMultipleJob(jsonObj,
							"050_Production_Art/052_PA_Working_Files/01_Roadrunner/", "3DXML.xml");
					XML3D_Save_Path = "050_Production_Art/052_PA_Working_Files/01_Roadrunner";
				}

				if (jsonString != "") {
					String[] newArryStr = new String[1];

					newArryStr[0] = jsonString;
					newArryStr[0] = utils.RemoveForwardSlash(newArryStr[0]);
					newArryStr[0] = newArryStr[0].replace("\"", "'");

					SEng.CallTyphoonShadow(newArryStr);
					Thread.sleep(4000);

					// PNG set swatch White color to White 2
					SEng.SetSwathColorFromTo("White 2", "White"); //// only for NCL P - N - G
					SEng.SetLayerVisibleOff(); //// only for NCL P - N - G

					Thread.sleep(1000);
					String fileRenameString = jspr.getJsonValueForKey(jsonObj, "WO") + "_3dxml";

					String road_runnerDirPath = utils.RemoveForwardSlash(
							(String) jspr.getJsonValueForKey(jsonObj, "Path") + XML3D_Save_Path + "/");
					if (!Utils.IsFolderExists(road_runnerDirPath)) {
						utils.CreateNewDirectory(road_runnerDirPath, false);
					}

					fileName[0] = utils.RemoveForwardSlash(
							(String) jspr.getJsonValueForKey(jsonObj, "Path") + XML3D_Save_Path + "/");

					fileName[1] = road_runnerDirPath + "/" + fileRenameString;
					fileName[2] = fileName[0] + fileRenameString;
					fileName[3] = fileName[0] + fileRenameString;
					SEng.PostDocumentProcessFor3DXML(fileName);
				}

			
		

			//// ----PNG---

			// ***PnG***// This is to Move from different part to 051_PA_Previous.

			Utils utls = new Utils();
			String sourceFile = jsonPars.getMasterAIWithoutPathValidate(jsonObj, "Master");
			String destinationFilePath = jsonPars.getJsonValueFromGroupKey(jsonObj, "aaw", "Path")
					+ "050_Production_Art/051_PA_Previous/";

			String moveFileStatus = "";
			moveFileStatus = utls.MoveFileFromSourceToDestination(sourceFile, destinationFilePath);
			}
			else if((MessageQueue.RR_SOURCE.equals("HUBX_RR") || MessageQueue.RR_SOURCE.equals("WAVEPNG")) && MessageQueue.TORNADO_ENV.equals("development"))
			{
				// ***PnG***// This is to Move from different part to 051_PA_Previous.

				Utils utls = new Utils();
				String sourceFile = jsonPars.getMasterAIWithoutPathValidate(jsonObj, "Master");
				String destinationFilePath = jsonPars.getJsonValueFromGroupKey(jsonObj, "aaw", "Path")
						+ "050_Production_Art/051_PA_Previous/";

				String moveFileStatus = "";
				if(!sourceFile.equalsIgnoreCase(""))
				moveFileStatus = utls.MoveFileFromSourceToDestination(sourceFile, destinationFilePath);

				// ***PnG**//
			}
			else if(MessageQueue.TORNADO_ENV.equals("production"))
			{
				// ***PnG***// This is to Move from different part to 051_PA_Previous.

				Utils utls = new Utils();
				String sourceFile = jsonPars.getMasterAIWithoutPathValidate(jsonObj, "Master");
				String destinationFilePath = jsonPars.getJsonValueFromGroupKey(jsonObj, "aaw", "Path")
						+ "050_Production_Art/051_PA_Previous/";

				String moveFileStatus = "";
				if(!sourceFile.equalsIgnoreCase(""))
				moveFileStatus = utls.MoveFileFromSourceToDestination(sourceFile, destinationFilePath);

				// ***PnG**//
			}
			// ***PnG**//

		}
		Thread.sleep(2000);
		SEng.PostDocumentClose();

		sendRespStatusMsg("delivered");
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed process for job id  '" + MessageQueue.MSGID + "' ");
		Thread.sleep(1000);

		if(utils.FileExists(exportXmlFilePath))
		{
			Action.UpdateToServer("xmlcompare", false);
			log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparison completed..");
		}
		else
		{
			Action.UpdateToServer("xmlcompare", false);
			log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparison completed..");
			String exportXmlReport = fls.ReadFileReport("Export.txt");
			if(exportXmlReport.length() > 5 )
			{
			log.error(MessageQueue.WORK_ORDER + ": " + "Export xml not exits on the path " +  exportXmlFilePath);
			Action.UpdateErrorStatusWithRemark("14", "Export XML not generated " + exportXmlFilePath  + "Error : " + exportXmlReport.toString());
			}
		}

		Action.UpdateReport(fls.ReadFileReport("Report.txt"));
		
		MessageQueue.ERROR += errorMsg;
		Action.sendStatusMsg((String) MessageQueue.ERROR);

		MessageQueue.ERROR = "";
		MessageQueue.WORK_ORDER = "";
		Thread.sleep(1000);
		MessageQueue.GATE = true;
	}
	
	
	public static void ReRun3DXML(JSONObject jsonObj) throws Exception
	{
		JsonParser jspr = new JsonParser();
		XmlUtiility xmlUtil = new XmlUtiility();
		FileSystem fls = new FileSystem();
		Utils utils = new Utils();

		Thread.sleep(1000);
		
		log.info(MessageQueue.WORK_ORDER + ": " + "RR 3DXML started processing");

		SEng.CallAdobeIllustrator();
		log.info(MessageQueue.WORK_ORDER + ": " + "Illustrator activated to load file..");
		
		SEng.OpenDocument(jspr.geFilePathFromJson(jsonObj, "Master"));
		log.info(MessageQueue.WORK_ORDER + ": " + "AI file and other dependend file opening..");
		
		Thread.sleep(1000);

		{
			INIReader ini = new INIReader();
			ini.readIniForSingle();
			
			String renderPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path");
			renderPath = renderPath + "090_Deliverables/095_Renders/";
			if(!Utils.IsFolderExists(renderPath))
				utils.CreateNewDirectory(renderPath, false);
			

			String[] docPath = jspr.getPath(jsonObj);
			String fileNameToSave = xmlUtil.getFileNameFromElement((docPath[0].split("~"))[0]);
			if (fileNameToSave != null) {

				String fileName = utils.GetNewNameIfFileExists(GetLastIndex(docPath[1]) + "/" + fileNameToSave + ".ai");

				int index = fileName.lastIndexOf("/");
				fileName = fileName.substring(index + 1, fileName.length());
				fileNameToSave = fileName.split("\\.")[0];
			}

			String[] fileName = new String[4];
			if (fileNameToSave != null) {
				fileName[0] = "none"; // dummy
				fileName[1] = renderPath + fileNameToSave;
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[1]) + "/" + fileNameToSave;
			} else //// After renmaing to <workorder no>.ai
			{
				fileNameToSave = jspr.getJsonValueForKey(jsonObj, "WO");
				fileName[1] = renderPath + fileNameToSave;
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[1]) + "/" + fileNameToSave;
			} // // After renmaing to <workorder no>.ai

			if (MessageQueue.sPdfNormal) {
				if (fileNameToSave != null) {
					SEng.PostDocumentProcessForSingleJobFilename(fileName);
				} else
					SEng.PostDocumentProcess(jspr.getPath(jsonObj));
			} else if (MessageQueue.sPdfPreset) {

				String pdfPreset[] = utils.getPresetFileFromDirectory(
						utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")), "joboptions");
				String[] pdfPresetArr = new String[2];
				if (pdfPreset.length != 0) {
					pdfPresetArr[0] = utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")) + "/"
							+ pdfPreset[0];
					pdfPresetArr[1] = pdfPreset[0].split("\\.")[0];
					String resultPdfExport = "";
					if (fileNameToSave != null)
						resultPdfExport = SEng.PostDocMultiPDFPreset(fileName, pdfPresetArr);
					else {
						String[] dcPath = new String[4];
						dcPath[0] = "";
						dcPath[1] = renderPath;
						dcPath[2] = docPath[2];
						dcPath[3] = docPath[1];
						resultPdfExport = SEng.PostDocMultiPDFPreset(dcPath, pdfPresetArr);
					}
					if (!resultPdfExport.equalsIgnoreCase("null")) {
						fls.AppendFileString("\n PDF with preset export failed: " + resultPdfExport + " \n");
						MessageQueue.ERROR += resultPdfExport + "\n";
					}
				} else {
					fls.AppendFileString(
							"\n PDF with preset export failed, preset file not found in master ai file path \n");
					MessageQueue.ERROR += "\n PDF with preset export failed, preset file not found in master ai file path \n";
				}
			} else if (MessageQueue.sPdfNormalised) {
				if (PostMultipleJobPreProcess()) {
					
					
					docPath[2] = docPath[2].split("\\.")[0];
					String resultPdfExport = "";
					if (fileNameToSave != null)
						resultPdfExport = SEng.PostDocumentMultipleProcess(fileName);
					else {
						String[] dcPath = new String[4];
						dcPath[0] = "";
						dcPath[1] = renderPath;
						dcPath[2] = docPath[2];
						dcPath[3] = docPath[1];
						resultPdfExport = SEng.PostDocumentMultipleProcess(dcPath);
					}
					if (!resultPdfExport.equalsIgnoreCase("null")) {
						fls.AppendFileString("\n Normalized PDF export failed: " + resultPdfExport + " \n");
						MessageQueue.ERROR += resultPdfExport + "\n";
					}
				}
			}

			log.info(MessageQueue.WORK_ORDER + ": " + "Pdf and xml generated..");
		}

		String[] fileName = new String[4];
	
		String jsonString = "";
		String XML3D_Save_Path = "";
		String primaryPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path");
		
		if (utils.FileExists(primaryPath + "050_Production_Art/052_PA_Working_Files/01_Roadrunner/3DXML.xml")) 
		{
			jsonString = jspr.updateJsonForMultipleJob(jsonObj,
					"050_Production_Art/052_PA_Working_Files/01_Roadrunner/", "3DXML.xml");
			XML3D_Save_Path = "050_Production_Art/052_PA_Working_Files/01_Roadrunner";
			
			
			String brackGroundReferenceURI = "";
			String logoReferenceURI = "";
			XmlUtiility xmlt = new XmlUtiility();
			String rr3DJson = fls.ReadFile(primaryPath + "050_Production_Art/052_PA_Working_Files/01_Roadrunner/RRremap.txt");
			
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
			   
			 xmlt.GS1XmlParseAllElement_TMP(primaryPath + "050_Production_Art/052_PA_Working_Files/01_Roadrunner/3DXML.xml", true, brackGroundReferenceURI, logoReferenceURI);
			 Thread.sleep(2000);
			
			
		}


		if (jsonString != "") {
			String[] newArryStr = new String[1];

			newArryStr[0] = jsonString;
			newArryStr[0] = utils.RemoveForwardSlash(newArryStr[0]);
			newArryStr[0] = newArryStr[0].replace("\"", "'");
			
			Action.UpdateErrorStatusWithRemark("37", "3DXML Started");

			SEng.CallTyphoonShadow(newArryStr);
			Thread.sleep(1000);

			// PNG set swatch White color to White 2
			SEng.SetSwathColorFromTo("White 2", "White"); //// only for NCL P - N - G
			SEng.SetLayerVisibleOff(); //// only for NCL P - N - G

			Thread.sleep(1000);
			String fileRenameString = jspr.getJsonValueForKey(jsonObj, "WO") + "_3dxml";

			String road_runnerDirPath = utils.RemoveForwardSlash(
					(String) jspr.getJsonValueForKey(jsonObj, "Path") + XML3D_Save_Path + "/");
			if (!Utils.IsFolderExists(road_runnerDirPath)) {
				utils.CreateNewDirectory(road_runnerDirPath, false);
			}

			fileName[0] = utils.RemoveForwardSlash(
					(String) jspr.getJsonValueForKey(jsonObj, "Path") + XML3D_Save_Path + "/");

			fileName[1] = road_runnerDirPath + "/" + fileRenameString;
			fileName[2] = fileName[0] + fileRenameString;
			fileName[3] = fileName[0] + fileRenameString;
			SEng.PostDocumentProcessFor3DXML(fileName);
			Thread.sleep(3000);

			SEng.PostDocumentClose();
			Action.UpdateErrorStatusWithRemark("38", "3DXML Completed");
			Thread.sleep(1000);
			
			Action.UpdateToServer("xmlcompare", false);
			
			MessageQueue.ERROR = "";
			MessageQueue.WORK_ORDER = "";
			Thread.sleep(1000);
			MessageQueue.GATE = true;
		}

	
	}
	

	public static void multiActionSeq(JSONObject jsonObj) throws Exception {
		JsonParser jspr = new JsonParser();
		XmlUtiility xmlUtil = new XmlUtiility();
		FileSystem fls = new FileSystem();
		Utils utils = new Utils();

		utils.CreateNewDirectory(jspr.getXmlFolderPathFromJson(jsonObj, "XMLFile") + "DummyFolder", true);

		String pdfPreset[] = utils.getPresetFileFromDirectory(
				utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")), "joboptions");

		String xmlFiles[] = utils.getFileFromDirectory(jspr.getXmlFolderPathFromJson(jsonObj, "XMLFile"), "xml");
		ArrayList arrErrReport = new ArrayList();
		ArrayList arrDetailedReport = new ArrayList();
		ArrayList arrConsolidateErrorReport = new ArrayList();
		ArrayList arrConsolidateDetailedReport = new ArrayList();

		SEng.CallAdobeIllustrator();
		log.info(MessageQueue.WORK_ORDER + ": " + "Illustrator activated to load file..");

		String[] appFonts = SEng.GetApplicationFonts().split(",");
		Thread.sleep(5000);

		SEng.OpenDocument(jspr.geFilePathFromJson(jsonObj, "Master"));
		log.info(MessageQueue.WORK_ORDER + ": " + "AI file and other dependent file opening..");

		String dcFont = SEng.GetDocumentFonts();
		if (dcFont.length() != 0) {
			String[] docFonts = (dcFont).split(",");
			Action.FindMissingFonts(appFonts, docFonts);
		}
		String dcFile = SEng.GetDocumentFiles();
		if (dcFile.length() != 0) {
			String[] docFiles = dcFile.split(",");
			Action.FindMissingFiles(docFiles);
		}

		utils.DeleteDirectory(jspr.getXmlFolderPathFromJson(jsonObj, "XMLFile") + "DummyFolder");
		Thread.sleep(1000);

		INIReader ini = new INIReader();
		ini.readIniForMultiple();

		String[] pdfPresetArr = new String[2];
		if (MessageQueue.mPdfPreset) {
			if (pdfPreset.length != 0) {
				pdfPresetArr[0] = utils.GetParentPath(jspr.geFilePathFromJson(jsonObj, "Master")) + "/" + pdfPreset[0];
				pdfPresetArr[1] = pdfPreset[0].split("\\.")[0];
			}
		} else if (MessageQueue.mPdfNormalised) {
			PostMultipleJobPreProcess();
		}

		String xmlDirPath = jspr.getXmlDirPath(jsonObj);
		for (int eachXmlCount = 0; eachXmlCount < xmlFiles.length; eachXmlCount++) {
			MessageQueue.MESSAGE = jspr.updateJsonForMultipleJob(jsonObj, xmlDirPath, xmlFiles[eachXmlCount]);
			String[] docPath = jspr.getMultiPath(jsonObj, xmlFiles[eachXmlCount]);
			if (!XmlUtiility.multipleJobIsValidXml(docPath[0])) {
				ConsolidateErrorReport(fls, arrErrReport, arrConsolidateErrorReport, arrDetailedReport,
						arrConsolidateDetailedReport, xmlFiles[eachXmlCount]);
				continue;
			}
			String fileNameToSave = xmlUtil.getFileNameFromElement(docPath[0]);
			// docPath[0] += "~0";

			String[] arrString = new String[1];
			arrString[0] = MessageQueue.MESSAGE;
			arrString[0] = utils.RemoveForwardSlash(arrString[0]);
			arrString[0] = arrString[0].replace("\"", "'");

			SEng.CallTyphoonShadow(arrString);

			log.info(MessageQueue.WORK_ORDER + ": " + "RoadRunner plugin called");

			String renderPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path");
			renderPath = renderPath + "090_Deliverables/095_Renders/";
			
		      String directories = renderPath;
		      File file = new File(directories);
		      file.mkdirs();
			
			String[] fileName = new String[4];
			if (fileNameToSave != null) {

				fileName[0] = "none"; // dummy
				fileName[1] = renderPath + fileNameToSave;
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[3]) + "/" + fileNameToSave;

			}
			else
			{
				fileNameToSave =  jspr.getJsonValueForKey(jsonObj, "WO") + "_" +  xmlFiles[eachXmlCount];
				fileName[0] = "none"; // dummy
				fileName[1] = renderPath + fileNameToSave;
				fileName[2] = docPath[2];
				fileName[3] = docPath[3];
				
			}

			if (MessageQueue.mPdfNormal) {
				if (fileNameToSave != null) {
					SEng.PostDocumentProcessForSingleJobFilename(fileName);
				} else {
					SEng.PostDocumentProcessForSingleJobFilename(docPath);
				}
			} else if (MessageQueue.mPdfPreset) {
				if (pdfPreset.length != 0) {
					String resultPdfExport = "";
					if (fileNameToSave != null)
						resultPdfExport = SEng.PostDocMultiPDFPreset(fileName, pdfPresetArr);
					else
						resultPdfExport = SEng.PostDocMultiPDFPreset(docPath, pdfPresetArr);
					if (!resultPdfExport.equalsIgnoreCase("null")) {
						fls.AppendFileString("\n PDF with preset export failed: " + resultPdfExport + " \n");
						MessageQueue.ERROR += resultPdfExport + "\n";
					}
				} else {
					fls.AppendFileString(
							"\n PDF with preset export failed, preset file not found in master ai file path \n");
					MessageQueue.ERROR += "\n PDF with preset export failed, preset file not found in master ai file path \n";
				}
			} else if (MessageQueue.mPdfNormalised) {
				docPath[2] = docPath[2].split("\\.")[0];
				String resultPdfExport = "";
				if (fileNameToSave != null)
					resultPdfExport = SEng.PostDocumentMultipleProcess(fileName);
				else
					resultPdfExport = SEng.PostDocumentMultipleProcess(docPath);
				if (!resultPdfExport.equalsIgnoreCase("null")) {
					fls.AppendFileString("\n Normalized PDF export failed: " + resultPdfExport + " \n");
					MessageQueue.ERROR += resultPdfExport + "\n";
				}
			}
			Thread.sleep(4000);
			log.info(MessageQueue.WORK_ORDER + ": " + "Pdf and xml generated..");
			ConsolidateErrorReport(fls, arrErrReport, arrConsolidateErrorReport, arrDetailedReport,
					arrConsolidateDetailedReport, xmlFiles[eachXmlCount]);
		}

		Thread.sleep(7000);
		SEng.PostDocumentClose();
		sendRespStatusMsg("delivered");
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed process for job id  '" + MessageQueue.MSGID + "' ");

		Action.UpdateToServer("xmlcompare", false);
		log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparision completed..");

		Action.sendStatusMsg(arrConsolidateErrorReport.toString());
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed sending error report..");
		Action.UpdateReport(arrConsolidateDetailedReport.toString());
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed sending of detailed report..");

		MessageQueue.ERROR = "";
		Thread.sleep(1000);
		MessageQueue.GATE = true;
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed job..");
	}

	
	public static void SwatchMergeFromXML(String xmlPathString, String swatchName, String privateElmtTypeCode)
			throws Exception {
		FileSystem fls = new FileSystem();
		try {
			List<String> SwatchListFromXML = new ArrayList<String>();
			XmlUtiility xmlUtils = new XmlUtiility();
			
			String swatchString = SEng.SwatchTest();
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
				String result =  SEng.ExecuteIllustratorActions(swatchColorArray);
				
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
	


	public static String GetLastIndex(String filePath) {
		int index = filePath.lastIndexOf("/");
		return filePath.substring(0, index);
	}

	public static boolean PostMultipleJobPreProcess() throws IOException {
		Utils utls = new Utils();
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
		}

		if (!eskoPluginbool) {
			MessageQueue.ERROR += "PDF cannot be generated following plugin missing : " + eskoPdfPlugin + "\n";
			return false;
		}
		return true;
	}

	public static void ConsolidateErrorReport(FileSystem fls, ArrayList arrErrReport,
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

		JsonParser jspr = new JsonParser();
		String[] pathString = jspr.getPath(jsonObj);
		String[] xmlFilesPath = pathString[0].split(",");
		for (int eachXmlCount = 0; eachXmlCount < xmlFilesPath.length; eachXmlCount++) {
			XmlUtiility.IsValidXML(xmlFilesPath[eachXmlCount].split("~")[0]);
		}
	}

	public static void acknowledge(String jsonString) throws Exception {
		Utils utls = new Utils();
		JSONObject jsonObj = JsonParser.ParseJson(jsonString);
		JsonParser jsonPars = new JsonParser();
		String version = (String) jsonObj.get("version");
		MessageQueue.VERSION = version;
		MessageQueue.WORK_ORDER = jsonPars.getJsonValueForKey(jsonObj, "WO");
		MessageQueue.RR_SOURCE = (String) jsonObj.get("source");

	//	Thread.sleep(1000);

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
					ThrowException.CustomExitWithErrorMsgID(new Exception("Invalid Json: "),
							"Invalid JSON environment value from Tornado", "14");
				}
			} catch (Exception ex) {
				log.error(MessageQueue.WORK_ORDER + ": " + "Issue with environment value, process terminated.");
				ThrowException.CustomExitWithErrorMsgID(new Exception("Invalid Json: "),
						"Invalid JSON environment value from Tornado", "14");
			}

			INIReader iniRdr = new INIReader();
			iniRdr.writeValueforKey(MessageQueue.TORNADO_HOST);

			FileSystem fls = new FileSystem();
			fls.CreateFile("Report.txt");
			fls.CreateFile("error.txt");
			fls.CreateFile("Export.txt");

			INetwork iNet = new INetwork();

			MessageQueue.MSGID = (String) jsonObj.get("Id");

			// Action.AddVolumes(); // mount volume after creating message id.
			

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
			String sourceFile = jsonPars.getJsonValueFromGroupKey(jsonObj, "aaw", "MasterTemplate");
			if (sourceFile != null)
			{
				if (!sourceFile.isEmpty()) {
					String destinationFilePath = jsonPars.getMasterAIWithoutPathValidate(jsonObj, "Master");
					copyFileStatus = utls.CopyFileFromSourceToDestination(sourceFile, destinationFilePath);
				}
			}
			else
				sourceFile = null;
			// ***PnG**//
			
		//	Thread.sleep(2000);
			if (copyFileStatus != "" && !sourceFile.isEmpty() && sourceFile != null) {
				if (!((String) jsonObj.get("type")).equals("multi"))
					ValidateFiles(jsonObj);
				try {

					String workOrderNo = jsonPars.getJsonValueForKey(jsonObj, "WO");
					sendRespStatusMsg("received" + "::" + iNet.GetClientIPAddr());
					log.info(MessageQueue.WORK_ORDER + ": " + "Message received acknowledgement for job id  '"
							+ MessageQueue.MSGID + "' " + " WO: " + workOrderNo);

					if (!((String) jsonObj.get("type")).equals("multi"))
					{
						if(MessageQueue.TORNADO_ENV.equalsIgnoreCase("production"))
						{
							actionSeq(jsonObj, true);
						}
						else if (MessageQueue.TORNADO_ENV.equalsIgnoreCase("development") || MessageQueue.TORNADO_ENV.equalsIgnoreCase("qa"))
						{
							actionSeq_Dev(jsonObj, true);
						}
					}
					else
					{
						if(MessageQueue.TORNADO_ENV.equalsIgnoreCase("production"))
						{
							multiActionSeq(jsonObj);
						}
						else if (MessageQueue.TORNADO_ENV.equalsIgnoreCase("development") || MessageQueue.TORNADO_ENV.equalsIgnoreCase("qa"))
						{
							multiActionSeq_Dev(jsonObj);
						}
						
					}
				} 
				catch (FileSystemException fileExp)
				{
					log.error(MessageQueue.WORK_ORDER + ": " + "File system exception : " + fileExp.getMessage());					
					ThrowException.CatchExceptionWithErrorMsgId(fileExp, "File system exception : " + fileExp.getMessage(), "14");
				}
				catch (Exception ex) 
				{
					log.error(MessageQueue.WORK_ORDER + ": " + "Msg Ack err: " + ex);
					ThrowException.CatchExceptionWithErrorMsgId(ex, ex.getMessage(), "14");
				}
			} else if (sourceFile == null) {
				if (!((String) jsonObj.get("type")).equals("multi"))
					ValidateFiles(jsonObj);
				try {

					String workOrderNo = jsonPars.getJsonValueForKey(jsonObj, "WO");
					sendRespStatusMsg("received" + "::" + iNet.GetClientIPAddr());
					log.info(MessageQueue.WORK_ORDER + ": " + "Message received acknowledgement for job id  '"
							+ MessageQueue.MSGID + "' " + " WO: " + workOrderNo);

					if (!((String) jsonObj.get("type")).equals("multi"))
					{
						if(MessageQueue.TORNADO_ENV.equalsIgnoreCase("production"))
						{
							actionSeq(jsonObj, true);
						}
						else if (MessageQueue.TORNADO_ENV.equalsIgnoreCase("development") || MessageQueue.TORNADO_ENV.equalsIgnoreCase("qa"))
						{
							actionSeq_Dev(jsonObj, true);
						}
					}
					else
					{
						if(MessageQueue.TORNADO_ENV.equalsIgnoreCase("production"))
						{
							multiActionSeq(jsonObj);
						}
						else if (MessageQueue.TORNADO_ENV.equalsIgnoreCase("development") || MessageQueue.TORNADO_ENV.equalsIgnoreCase("qa"))
						{
							multiActionSeq_Dev(jsonObj);
						}
						
					}
				} catch (Exception ex) {
					log.error(MessageQueue.WORK_ORDER + ": " + "Msg Ack err: " + ex);
				}
			}
			}
		} else {
			ThrowException.CustomExitWithErrorMsgID(new Exception("Invalid version: "),
					"RR plugin error: check illustrator version used or RR plugin doesn't exist for that version: "
							+ "/Applications/Adobe Illustrator " + MessageQueue.VERSION,
					"14");

		}

	}

	public static void sendRespStatusMsg(String status) throws Exception {
		try {
			log.info(MessageQueue.WORK_ORDER + ": " + "Before calling send report status message post method");
			String postResponse = HttpConnection.excutePost("http://" + MessageQueue.HOST_IP + ":8080/AAW/message/resp",
					MessageQueue.MSGID + "::" + status);
			log.info(MessageQueue.WORK_ORDER + ": " + "Status of sending report status msg response " + postResponse);
		} catch (Exception ex) {
			log.error(ex);
		}

	}

	public static void sendStatusMsg(String status) throws Exception {
		try {
			log.info(MessageQueue.WORK_ORDER + ": " + "Before calling send status message post method");
			String postResponse = HttpConnection.excutePost(
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
			HttpsConnection httpsCon = new HttpsConnection();
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

				log.error(
						MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed - " + urlStr.toString());
			}
			if ((connection.getResponseCode() != HttpURLConnection.HTTP_OK || connection == null)
					&& MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_1)
					&& MessageQueue.TORNADO_ENV.equals("production")) {

				if (connection != null)
					connection.disconnect();

				HttpsConnection httpsCon2 = new HttpsConnection();
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
							HttpsConnection httpsCon3 = new HttpsConnection();
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
								Action.UpdateErrorStatusWithRemark("26",
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
										Action.UpdateErrorStatusWithRemark("26",
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
							Action.UpdateErrorStatusWithRemark("26",
									"Road Runner not received any response: " + (String) ex3.getMessage());
						} catch (IOException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Http IO exception: "
									+ ex3.getMessage());
							Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");
						} catch (Exception ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Error Http connection: "
									+ (String) ex3.getMessage());
							Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");
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
							Action.UpdateErrorStatusWithRemark("26",
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
							HttpsConnection httpsCon3 = new HttpsConnection();
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
								Action.UpdateErrorStatusWithRemark("26",
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
										Action.UpdateErrorStatusWithRemark("26",
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
							Action.UpdateErrorStatusWithRemark("26",
									"Road Runner not received any response: " + (String) ex3.getMessage());
						} catch (IOException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Http IO exception: "
									+ ex3.getMessage());
							Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

						} catch (Exception ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Error Http connection: "
									+ (String) ex3.getMessage());
							Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

						}

					} else {

						log.error(MessageQueue.WORK_ORDER + ": " + MessageQueue.TORNADO_HOST_LIVE_2
								+ " Http response time out: " + (String) ex2.getMessage());
						Action.UpdateErrorStatusWithRemark("26",
								"Road Runner not received any response: " + (String) ex2.getMessage());
					}

				} catch (IOException ex2) {
					log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString() + " Http IO exception: "
							+ ex2.getMessage());
					Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

				} catch (Exception ex2) {
					log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString() + " Error Http connection: "
							+ (String) ex2.getMessage());
					Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

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
					Action.UpdateErrorStatusWithRemark("26",
							"Road Runner received response : " + connection.getResponseCode());
				}

			}

			if (connection != null)
				connection.disconnect();

		} catch (java.net.SocketTimeoutException ex) {
			// Connect1 catch

			if (MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_1)
					&& MessageQueue.TORNADO_ENV.equals("production")) {

				HttpsConnection httpsCon2 = new HttpsConnection();
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
							HttpsConnection httpsCon3 = new HttpsConnection();
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
								Action.UpdateErrorStatusWithRemark("26",
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
										Action.UpdateErrorStatusWithRemark("26",
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
							Action.UpdateErrorStatusWithRemark("26",
									"Road Runner not received any response: " + (String) ex3.getMessage());
						} catch (IOException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Http IO exception: "
									+ ex3.getMessage());
							Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");
						} catch (Exception ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Error Http connection: "
									+ (String) ex3.getMessage());
							Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");
						}

					} else {
						System.out.println("XML compare: " + connection2.getResponseCode());
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "
								+ MessageQueue.TORNADO_HOST_LIVE_2 + "/rest/pub/aaw/" + actionStr + "?mqid="
								+  MessageQueue.MSGID + " - response : " + connection2.getResponseCode());
						
						if(connection2.getResponseCode() != HttpURLConnection.HTTP_OK)
						{
							Action.UpdateErrorStatusWithRemark("26",
									"Road Runner received response - " + connection2.getResponseCode());
						}
						
					}

				} catch (java.net.SocketTimeoutException ex2) {

					if (!(MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_2))
							&& MessageQueue.TORNADO_ENV.equals("production")) {
						URL urlStr_3 = new URL(MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr
								+ "?mqid=" +  MessageQueue.MSGID);
						try {
							HttpsConnection httpsCon3 = new HttpsConnection();
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
								Action.UpdateErrorStatusWithRemark("26",
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
										Action.UpdateErrorStatusWithRemark("26",
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
							Action.UpdateErrorStatusWithRemark("26",
									"Road Runner not received any response: " + (String) ex3.getMessage());
						} catch (IOException ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Http IO exception: "
									+ ex3.getMessage());
							Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

						} catch (Exception ex3) {
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString() + " Error Http connection: "
									+ (String) ex3.getMessage());
							Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

						}

					} else {

						log.error(MessageQueue.WORK_ORDER + ": " + MessageQueue.TORNADO_HOST_LIVE_2
								+ " Http response time out: " + (String) ex2.getMessage());
						Action.UpdateErrorStatusWithRemark("26",
								"Road Runner not received any response: " + (String) ex2.getMessage());
					}

				} catch (IOException ex2) {
					log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString() + " Http IO exception: "
							+ ex2.getMessage());
					Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

				} catch (Exception ex2) {
					log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString() + " Error Http connection: "
							+ (String) ex2.getMessage());
					Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");

				}

			} else {
				log.error(MessageQueue.WORK_ORDER + ": Error Http connection 'Failed' "
						+ MessageQueue.TORNADO_HOST_LIVE_1 + " <> " + MessageQueue.TORNADO_HOST);
				Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response");
			}

		} catch (IOException ex1) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Http IO exception: " + ex1.getMessage());
			Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response ");
		} catch (Exception ex1) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Error Http connection: " + (String) ex1.getMessage());
			Action.UpdateErrorStatusWithRemark("26", "Road Runner not received any response ");
		}
	}

	public static void UpdateReport(String reportStr) throws IOException {
		try {
			HttpsConnection httpsCon = new HttpsConnection();
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

			HttpsConnection httpsCon = new HttpsConnection();
			
			
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
			HttpsConnection httpsCon = new HttpsConnection();
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
		Utils utls = new Utils();
		FileSystem fls = new FileSystem();
		ArrayList<String> missingFonts = utls.GetMissingFonts(appFonts, docFonts);
		if (missingFonts.size() > 0) {
			fls.AppendFileString("Document font not found:" + missingFonts.toString() + "\n");
			MessageQueue.ERROR += "\nDocument linked font missing \n";
		}
	}

	public static void FindMissingFiles(String[] arrFiles) throws Exception {

		Utils utls = new Utils();
		FileSystem fls = new FileSystem();
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
		Utils utls = new Utils();
		rowList = utls.ReadXLSXFile(utls.GetPathFromResource("SMB.xlsx"), "sheet1");
		for (String[] row : rowList) {
			int noOfShareFolder = row.length - 1;
			for (int inc = 0; inc < noOfShareFolder; inc++) {
				String status = SEng.MountVolume(row[0], "", "", row[row.length - inc - 1]);
				if (status.contains("Error")) {
					Action.UpdateErrorStatusWithRemark("20", "Volume not able to mount, " + "server: " + row[0]
							+ " share directory: " + row[row.length - inc - 1]);
				}
			}
		}
	}

	public static void Mount() throws Exception {
		String[] arg = null;
		Utils utl = new Utils();
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
		

		String brackGroundReferenceURI = "";
		String logoReferenceURI = "";
		Utils utils = new Utils();
		FileSystem fls = new FileSystem();
		JsonParser jspr = new JsonParser();
		
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
