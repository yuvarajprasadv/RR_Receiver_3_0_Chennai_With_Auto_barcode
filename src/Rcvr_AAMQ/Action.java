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

import Rcvr_AAMQ.ImageConvertor;

public class Action {
	static Logger log = LogMQ.monitor("Rcvr_AAMQ.Action");

	public static void actionSeq(JSONObject jsonObj) throws Exception {
		JsonParser jspr = new JsonParser();
		XmlUtiility xmlUtil = new XmlUtiility();
		FileSystem fls = new FileSystem();
		Utils utils = new Utils();

		//// TEMP
		/*
		 * if(!XmlUtiility.CheckGraphicsElementExist(jspr.geFilePathFromJson(jsonObj,
		 * "XMLFile"))) { String barcodePath = jspr.geFilePathFromJson(jsonObj, "") +
		 * "030_Barcodes/"; if(!utils.FileExists(barcodePath)) { barcodePath =
		 * jspr.geFilePathFromJson(jsonObj, "") + "030 Barcodes/"; }
		 * XmlUtiility.GS1XmlAppendGraphicsElement(jspr.geFilePathFromJson(jsonObj,
		 * "XMLFile"), XmlUtiility.GetFileFromPathString(barcodePath).toString()); }
		 * else { String barcodePath = jspr.geFilePathFromJson(jsonObj, "") +
		 * "030_Barcodes/"; if(!utils.FileExists(barcodePath)) { barcodePath =
		 * jspr.geFilePathFromJson(jsonObj, "") + "030 Barcodes/"; }
		 * XmlUtiility.GS1XmlParseGraphicsElement(jspr.geFilePathFromJson(jsonObj,
		 * "XMLFile"), XmlUtiility.GetFileFromPathString(barcodePath).toString()); }
		 * /////TEMP
		 */
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
		Thread.sleep(1000);

		String[] arrString = new String[1];
		arrString[0] = MessageQueue.MESSAGE;
		arrString[0] = utils.RemoveForwardSlash(arrString[0]);
		arrString[0] = arrString[0].replace("\"", "'");

		// System.out.println(arrString[0] );

		SEng.CallTyphoonShadow(arrString);

		log.info(MessageQueue.WORK_ORDER + ": " + "TyphoonShadow called");

		String errorMsg = fls.ReadFileReport("error.txt");
		if (errorMsg.contains("\n") && errorMsg.length() != 1)
			MessageQueue.STATUS = false;

		// if(MessageQueue.STATUS)
		{

			INIReader ini = new INIReader();
			ini.readIniForSingle();
			
			
			String printXML = jspr.getJsonValueFromGroupKey(jsonObj, "region", "printXML");
			if (printXML == null)
				printXML = "false";
			if (printXML.equalsIgnoreCase("true"))
			{

			String[] docPath = jspr.getPath(jsonObj);
			String fileNameToSave = xmlUtil.getFileNameFromElement((docPath[0].split("~"))[0]);
			String[] fileName = new String[4];
			if (fileNameToSave != null) {
				fileName[0] = "none"; // dummy
				fileName[1] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[1]) + "/" + fileNameToSave;
			}

			if (MessageQueue.sPdfNormal) {
				String barCodeVisible = jspr.getJsonValueFromGroupKey(jsonObj, "region", "barCodeVisible");
				ImageConvertor imageConvertor = new ImageConvertor();
				if (fileNameToSave != null) {
					{
						String pdfOnlyPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path") + "/090_Deliverables/PDF_ONLY/";
						if(!utils.IsFolderExists(pdfOnlyPath))
						{
							/*
							log.error(MessageQueue.WORK_ORDER + ": " + "Directory doesn't exist: " + pdfOnlyPath);
							fls.AppendFileString("Directory doesn't exist: " + pdfOnlyPath + "\n");
							System.out.println("Directory doesn't exist: " + pdfOnlyPath);
							*/
							
							utils.CreateNewDirectory(pdfOnlyPath, false);
							fileName[2] = pdfOnlyPath + fileNameToSave;
							
						}
						else
							fileName[2] = pdfOnlyPath + fileNameToSave;
						SEng.PostDocumentProcessForSingleJobFilename(fileName);
					}
					if (printXML != null)
						if (printXML.equalsIgnoreCase("true")) {
							//// Layer Visiblity off
							Thread.sleep(5000);
							if (barCodeVisible != null)
							{
								if (barCodeVisible.equalsIgnoreCase("false"))
									SEng.SetLayerVisibleOff("false");
								else
									SEng.SetLayerVisibleOff("true");
							}
							else
								SEng.SetLayerVisibleOff("true");
							
							String dataCollectionPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path") + "/090_Deliverables/Data_Collection/";
							if(!utils.IsFolderExists(dataCollectionPath))
							{
								/*
								log.error(MessageQueue.WORK_ORDER + ": " + "Directory doesn't exist: " + dataCollectionPath);
								fls.AppendFileString("Directory doesn't exist: " + dataCollectionPath + "\n");
								System.out.println("Directory doesn't exist: " + dataCollectionPath);
								*/
								utils.CreateNewDirectory(dataCollectionPath, false);
								fileName[2] = dataCollectionPath + fileNameToSave;
								
							}
							else
								fileName[2] = dataCollectionPath + fileNameToSave;
							
							
							SEng.PostDocumentProcessForSingleJobFilenameJPEG(fileName);

							String imageFormat = "bmp";
							String inputImagePath = fileName[2] + ".jpg";
							String outputImagePath = fileName[2] + "." + imageFormat;
							Thread.sleep(1000);
							imageConvertor.ConvertImageTo(inputImagePath, outputImagePath, imageFormat);
						}
				} 
				else 
				{
					String[] pathArray = new String[4];
					
					pathArray[0] = "none"; // dummy
					pathArray[1] = GetLastIndex(docPath[2]) + jspr.getJsonValueForKey(jsonObj, "WO");
					pathArray[2] = GetLastIndex(docPath[2]);
					pathArray[3] = GetLastIndex(docPath[1]);
					
					
					String pdfOnlyPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path") + "/090_Deliverables/PDF_ONLY/";
					if(!utils.IsFolderExists(pdfOnlyPath))
					{
						/*log.error(MessageQueue.WORK_ORDER + ": " + "Directory doesn't exist: " + pdfOnlyPath);
						System.out.println("Directory doesn't exist: " + pdfOnlyPath);*/
						
						utils.CreateNewDirectory(pdfOnlyPath, false);
						pathArray[2] = pdfOnlyPath +  "/" + jspr.getJsonValueForKey(jsonObj, "WO");
					}
					else
						pathArray[2] = pdfOnlyPath +  "/" + jspr.getJsonValueForKey(jsonObj, "WO");
					
					
				//	SEng.PostDocumentProcess(jspr.getPath(jsonObj));
					SEng.PostDocumentProcess(pathArray);
					
					if (printXML != null)
						if (printXML.equalsIgnoreCase("true")) {
							//// Layer Visiblity off
							Thread.sleep(5000);
							if (barCodeVisible != null)
							{
								if (barCodeVisible.equalsIgnoreCase("false"))
									SEng.SetLayerVisibleOff("false");
								else
									SEng.SetLayerVisibleOff("true");
							}
							else
							{
								SEng.SetLayerVisibleOff("true");
							}
							
							
							String dataCollectionPath = jspr.getJsonValueFromGroupKey(jsonObj, "aaw", "Path") + "/090_Deliverables/Data_Collection/";
							if(!utils.IsFolderExists(dataCollectionPath))
							{
								/*log.error(MessageQueue.WORK_ORDER + ": " + "Directory doesn't exist: " + dataCollectionPath);
								System.out.println("Directory doesn't exist: " + dataCollectionPath);
								*/
								utils.CreateNewDirectory(dataCollectionPath, false);
								pathArray[2] = dataCollectionPath + "/" + jspr.getJsonValueForKey(jsonObj, "WO");
							}
							else
								pathArray[2] = dataCollectionPath + "/" + jspr.getJsonValueForKey(jsonObj, "WO");
							
							
							SEng.PostDocumentProcessJPEG(pathArray);

							String imageFormat = "bmp";
							String inputImagePath = pathArray[2] + ".jpg";
							String outputImagePath = pathArray[2] + "." + imageFormat;
							Thread.sleep(1000);

							imageConvertor.ConvertImageTo(inputImagePath, outputImagePath, imageFormat);
						}
				}
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
						dcPath[1] = "";
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
						dcPath[1] = "";
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
			}
			else if(printXML.equalsIgnoreCase("false"))
			{
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
				
			}

			log.info(MessageQueue.WORK_ORDER + ": " + "Pdf and xml generated..");

		}

		Thread.sleep(8000);

		SEng.PostDocumentClose();

		sendRespStatusMsg("delivered");
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed process for job id  '" + MessageQueue.MSGID + "' ");
		Thread.sleep(1000);

		Action.UpdateToServer(jsonObj, "xmlcompare");
		log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparision completed..");

		Action.UpdateReport(jsonObj, fls.ReadFileReport("Report.txt"));

		MessageQueue.ERROR += errorMsg;
		Action.sendStatusMsg((String) MessageQueue.ERROR);
		MessageQueue.ERROR = "";
		Thread.sleep(1000);
		MessageQueue.GATE = true;

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
		log.info(MessageQueue.WORK_ORDER + ": " + "AI file and other dependend file opening..");

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

			System.out.println(arrString[0]);

			SEng.CallTyphoonShadow(arrString);

			// SEng.CallTyphoonShadow(docPath);
			log.info(MessageQueue.WORK_ORDER + ": " + "TyphoonShadow called");

			String[] fileName = new String[4];
			if (fileNameToSave != null) {
				fileName[0] = "none"; // dummy
				fileName[1] = "none"; // dummy
				fileName[2] = GetLastIndex(docPath[2]) + "/" + fileNameToSave;
				fileName[3] = GetLastIndex(docPath[3]) + "/" + fileNameToSave;
			}

			if (MessageQueue.mPdfNormal) {
				if (fileNameToSave != null) {
					// SEng.PostDocumentProcess(fileName);
					SEng.PostDocumentProcessForSingleJobFilename(fileName);
				} else
					SEng.PostDocumentProcessForSingleJobFilename(docPath);
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

		Action.UpdateToServer(jsonObj, "xmlcompare");
		// Action.UpdateToServer(jsonObj, "xmlcompare&type=multi");
		log.info(MessageQueue.WORK_ORDER + ": " + "Xml comparision completed..");

		Action.sendStatusMsg(arrConsolidateErrorReport.toString());
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed sending error report..");
		Action.UpdateReport(jsonObj, arrConsolidateDetailedReport.toString());
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed sending of detailed report..");

		MessageQueue.ERROR = "";
		Thread.sleep(1000);
		MessageQueue.GATE = true;
		log.info(MessageQueue.WORK_ORDER + ": " + "Completed job..");
	}

	public static String GetLastIndex(String filePath) {
		int index = filePath.lastIndexOf("/");
		return filePath.substring(0, index);
	}

	public static boolean PostMultipleJobPreProcess() {
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
		try {
			String[] xmlFilesPath = pathString[0].split(",");
			for (int eachXmlCount = 0; eachXmlCount < xmlFilesPath.length; eachXmlCount++) {
				XmlUtiility.IsValidXML(xmlFilesPath[eachXmlCount].split("~")[0]);
			}
		} catch (Exception ex) {
			log.error(MessageQueue.WORK_ORDER + ": " + "xml err: " + ex);
			ThrowException.CatchException(ex);
		}
	}

	public static void acknowledge(String jsonString) throws Exception {
		Utils utls = new Utils();
		JSONObject jsonObj = JsonParser.ParseJson(jsonString);
		JsonParser jsonPars = new JsonParser();
		String version = (String) jsonObj.get("version");
		MessageQueue.VERSION = version;
		MessageQueue.WORK_ORDER = jsonPars.getJsonValueForKey(jsonObj, "WO");
		Thread.sleep(1000);
		
		if (utls.CheckRRExistForVersion())
		{
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
				ThrowException.CustomExit(null, "Invalid JSON environment value from Tornado");
			}
		} catch (Exception ex) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Issue with environment value, process terminated.");
			ThrowException.CustomExit(null, "Invalid JSON environment value from Tornado");
		}

		INIReader iniRdr = new INIReader();
		iniRdr.writeValueforKey(MessageQueue.TORNADO_HOST);

		FileSystem fls = new FileSystem();
		fls.CreateFile("Report.txt");
		fls.CreateFile("error.txt");

		INetwork iNet = new INetwork();

		MessageQueue.MSGID = (String) jsonObj.get("Id");
		
		// ***CHENNAi Spec req to copy from master template path***// This is to copy from different part to 050_Production.
		
		String copyFileStatus = "";
		String sourceFile = jsonPars.getJsonValueFromGroupKey(jsonObj, "aaw", "MasterTemplate");
		if(sourceFile != null)
		if(!sourceFile.isEmpty())
		{
			String destinationFilePath = jsonPars.getMasterAIWithoutPathValidate(jsonObj, "Master");
			copyFileStatus = utls.CopyFileFromSourceToDestination(sourceFile, destinationFilePath); 
		}
		// ******CHENNAi**//
		
		
		try {
			if (!((String) jsonObj.get("type")).equals("multi"))
				ValidateFiles(jsonObj);
		} catch (java.lang.NullPointerException Ex) {
			log.error(Ex.getMessage());
			MessageQueue.ERROR += "\nInvalid Json request";
			fls.AppendFileString("\nInvalid Json request:" + " \n");
			ThrowException.CustomExit(Ex, "Invalid JSON request from Tornado");
		}
		try {
			sendRespStatusMsg("received" + "::" + iNet.GetClientIPAddr());
			log.info(MessageQueue.WORK_ORDER + ": " + "Message received acknowledgement for job id  '"
					+ MessageQueue.MSGID + "' ");

			if (!((String) jsonObj.get("type")).equals("multi"))
				actionSeq(jsonObj);
			else
				multiActionSeq(jsonObj);
		} catch (Exception ex) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Msg Ack err: " + ex);
		}
	}
	else {
		ThrowException.CustomExit(new Exception("Invalid version: "),
				"RR plugin error: check illustrator version used or RR plugin doesn't exist for that version: "
						+ "/Applications/Adobe Illustrator " + MessageQueue.VERSION);

	}

	}

	public static void sendRespStatusMsg(String status) throws Exception {
		try {

			HttpConnection.excutePost("http://" + MessageQueue.HOST_IP + ":8080/AAW/message/resp",
					MessageQueue.MSGID + "::" + status);
		} catch (Exception ex) {
			log.error(ex);
		}

	}

	public static void sendStatusMsg(String status) throws Exception {
		try {
			HttpConnection.excutePost("http://" + MessageQueue.HOST_IP + ":8080/AAW/message/error",
					MessageQueue.MSGID + "::" + status);
		} catch (Exception ex) {
			log.error(ex);
		}

	}
	
	public static void UpdateToServer(JSONObject jsonObj, String actionStr) throws IOException {
		URL urlStr = new URL(
				MessageQueue.TORNADO_HOST + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id"));
		try 
		{
			HttpsConnection httpsCon = new HttpsConnection();
			HttpURLConnection connection;

			log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr.toString());
			connection = (httpsCon.getURLConnection(urlStr, true));
			if(connection != null)
			{
				log.info(MessageQueue.WORK_ORDER + ": " + "Waiting for response of url: " + urlStr.toString());
				connection.setConnectTimeout(60000 * 6);
				connection.setReadTimeout(60000 * 6);
			}
			if(connection == null)
			{
			//	System.out.println("XML compare : API connection failed");
				log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed - "+ urlStr.toString());
			}
			 if((connection.getResponseCode() != HttpURLConnection.HTTP_OK || connection == null) && MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_1) && MessageQueue.TORNADO_ENV.equals("production"))
			{
				 
					if(connection != null)
						connection.disconnect();
					
					HttpsConnection httpsCon2 = new HttpsConnection();
					HttpURLConnection connection2;
		
					URL urlStr_2 = new URL(
							MessageQueue.TORNADO_HOST_LIVE_2 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id"));
					try
					{
					
					log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_2.toString());
					connection2 = (httpsCon2.getURLConnection(urlStr_2, true));
					
					if(connection2 != null)
					{
						log.info(MessageQueue.WORK_ORDER + ": " + "Waiting for response of url: " + urlStr_2.toString());
						connection2.setConnectTimeout(60000 * 6);
						connection2.setReadTimeout(60000 * 6);
					}
					if(connection2 == null)
					{
					//	System.out.println("XML compare : API connection failed");
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed - " + urlStr_2.toString());
					}
					if((connection2.getResponseCode() != HttpURLConnection.HTTP_OK || connection2 == null) && MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_2) && MessageQueue.TORNADO_ENV.equals("production"))
					{
						if(connection2 != null)
							connection2.disconnect();
						URL urlStr_3 = new URL(
								MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id"));
						try
						{
						HttpsConnection httpsCon3= new HttpsConnection();
						HttpURLConnection connection3;
						
						log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_3.toString());
						connection3 = (httpsCon3.getURLConnection(urlStr_3, true));
						
						if(connection3 != null)
						{
							log.info(MessageQueue.WORK_ORDER + ": " + "Waiting for response of url: " + urlStr_3.toString());
							connection3.setConnectTimeout(60000 * 6);
							connection3.setReadTimeout(60000 * 6);
						}
						if(connection3 == null)
						{
							System.out.println("XML compare : API connection failed");
							log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed - " +  urlStr_3.toString());
							System.out.println("Road Runner not received any response - connection time out");
						}
						else
						{
							System.out.println("XML compare : " + connection3.getResponseCode());
							log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "+ MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id")  +" - response : " + connection3.getResponseCode());
						}
						
						
						if(connection3 != null)
							connection3.disconnect();
						}
						catch(java.net.SocketTimeoutException ex3)
						{
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString()  + " Http response time out: " + (String)ex3.getMessage());
							System.out.println("XML compare : Road Runner not received any response: " +  (String) ex3.getMessage());
						}
						catch (IOException ex3)
						{
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString()   + " Http IO exception: " + ex3.getMessage());
							System.out.println("XML compare : Road Runner not received any response: " +  (String) ex3.getMessage());
						}
						catch (Exception ex3) 
						{
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString()   + " Error Http connection: " + (String) ex3.getMessage());
							System.out.println("XML compare : Road Runner not received any response: " +  (String) ex3.getMessage());
						}
						
					}
					else
					{
						System.out.println("XML compare: " + connection.getResponseCode());
						//	log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API response : " + connection.getResponseCode());
							log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "+ MessageQueue.TORNADO_HOST_LIVE_2 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id")  +" - response : " + connection.getResponseCode());
							
					}
					
					
					if(connection != null)
						connection.disconnect();
					}
					catch(java.net.SocketTimeoutException ex2)
					{
						
						if(MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_2) && MessageQueue.TORNADO_ENV.equals("production"))
						{
							URL urlStr_3 = new URL(
									MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id"));
							try
							{
							HttpsConnection httpsCon3= new HttpsConnection();
							HttpURLConnection connection3;
							
							log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_3.toString());
							connection3 = (httpsCon3.getURLConnection(urlStr_3, true));
							if(connection3 != null)
							{
								log.info(MessageQueue.WORK_ORDER + ": " + "wating for response - " + urlStr_3.toString());
								connection3.setConnectTimeout(60000 * 6);
								connection3.setReadTimeout(60000 * 6);
							}
							if(connection3 == null)
							{
								System.out.println("XML compare : API connection failed");
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed");
								System.out.println("XML compare : Road Runner not received any response " + "'connection not established'");
								
							}
							else
							{
								System.out.println("XML compare : " + connection3.getResponseCode());
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "+ MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id")  +" - response : " + connection3.getResponseCode());
							}
							
							
							if(connection3 != null)
								connection3.disconnect();
							}
							catch(java.net.SocketTimeoutException ex3)
							{
								log.error(MessageQueue.WORK_ORDER + ": " + MessageQueue.TORNADO_HOST_LIVE_3 + " Http response time out: " + (String)ex3.getMessage());
								System.out.println("XML compare : Road Runner not received any response: " +  (String) ex3.getMessage());
							}
							catch (IOException ex3)
							{
								log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString()   + " Http IO exception: " + ex3.getMessage());
								System.out.println("XML compare : Road Runner not received any response: " +  (String) ex3.getMessage());
								
							}
							catch (Exception ex3) 
							{
								log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString()   + " Error Http connection: " + (String) ex3.getMessage());
								System.out.println("XML compare : Road Runner not received any response: " +  (String) ex3.getMessage());
								
							}
						
						}
						else
						{
						
						log.error(MessageQueue.WORK_ORDER + ": " +  MessageQueue.TORNADO_HOST_LIVE_2 + " Http response time out: " + (String)ex2.getMessage());
						System.out.println("Http response time out: " + (String)ex2.getMessage());
						}
					
					}
					catch (IOException ex2)
					{
						log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString()   + " Http IO exception: " + ex2.getMessage());
						System.out.println("XML compare : Road Runner not received any response: " +  (String) ex2.getMessage());
						
					}
					catch (Exception ex2) 
					{
						log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString()   + " Error Http connection: " + (String) ex2.getMessage());
						System.out.println("XML compare: Road Runner not received any response: " +  (String) ex2.getMessage());
						
					}

				}
				else
				{
					System.out.println("XML compare: " + connection.getResponseCode());
				//	log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API response : " + connection.getResponseCode());
					log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "+ MessageQueue.TORNADO_HOST + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id")  +" - response : " + connection.getResponseCode());
					
				}

			if(connection != null)
			connection.disconnect();
			
		}
		catch (java.net.SocketTimeoutException ex)
		{
			//Connect1 catch
			
			if(MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_1) && MessageQueue.TORNADO_ENV.equals("production"))
			{

					HttpsConnection httpsCon2 = new HttpsConnection();
					HttpURLConnection connection2;
		
					URL urlStr_2 = new URL(
							MessageQueue.TORNADO_HOST_LIVE_2 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id"));
					try
					{
					
					log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_2.toString());
					connection2 = (httpsCon2.getURLConnection(urlStr_2, true));
					
					if(connection2 != null)
					{
						log.info(MessageQueue.WORK_ORDER + ": " + "Waiting for response of url: " + urlStr_2.toString());
						connection2.setConnectTimeout(60000 * 6);
						connection2.setReadTimeout(60000 * 6);
					}
					if(connection2 == null)
					{
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed - " + urlStr_2.toString());
					}
					if((connection2.getResponseCode() != HttpURLConnection.HTTP_OK || connection2 == null) && MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_2) && MessageQueue.TORNADO_ENV.equals("production"))
					{
						if(connection2 != null)
							connection2.disconnect();
						URL urlStr_3 = new URL(
								MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id"));
						try
						{
						HttpsConnection httpsCon3= new HttpsConnection();
						HttpURLConnection connection3;
						
						log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_3.toString());
						connection3 = (httpsCon3.getURLConnection(urlStr_3, true));
						
						if(connection3 != null)
						{
							log.info(MessageQueue.WORK_ORDER + ": " + "Waiting for response of url: " + urlStr_3.toString());
							connection3.setConnectTimeout(60000 * 6);
							connection3.setReadTimeout(60000 * 6);
						}
						if(connection3 == null)
						{
							System.out.println("XML compare : API connection failed");
							log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed - " +  urlStr_3.toString());
							System.out.println( "XML compare: Road Runner not received any response - connection time out");
						}
						else
						{
							System.out.println("XML compare : " + connection3.getResponseCode());
							log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "+ MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id")  +" - response : " + connection3.getResponseCode());
						}
						
						
						if(connection3 != null)
							connection3.disconnect();
						}
						catch(java.net.SocketTimeoutException ex3)
						{
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString()  + " Http response time out: " + (String)ex3.getMessage());
							System.out.println("XML compare: Road Runner not received any response: " +  (String) ex3.getMessage());
						}
						catch (IOException ex3)
						{
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString()   + " Http IO exception: " + ex3.getMessage());
							System.out.println("XML compare: Road Runner not received any response: " +  (String) ex3.getMessage());
						}
						catch (Exception ex3) 
						{
							log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString()   + " Error Http connection: " + (String) ex3.getMessage());
							System.out.println("XML compare: Road Runner not received any response: " +  (String) ex3.getMessage());
						}
						
					}
					else
					{
						System.out.println("XML compare: " + connection2.getResponseCode());
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "+ MessageQueue.TORNADO_HOST_LIVE_2 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id")  +" - response : " + connection2.getResponseCode());
					}

					}
					catch(java.net.SocketTimeoutException ex2)
					{
						
						if(MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_2) && MessageQueue.TORNADO_ENV.equals("production"))
						{
							URL urlStr_3 = new URL(
									MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id"));
							try
							{
							HttpsConnection httpsCon3= new HttpsConnection();
							HttpURLConnection connection3;
							
							log.info(MessageQueue.WORK_ORDER + ": " + "Before calling url: " + urlStr_3.toString());
							connection3 = (httpsCon3.getURLConnection(urlStr_3, true));
							if(connection3 != null)
							{
								log.info(MessageQueue.WORK_ORDER + ": " + "wating for response - " + urlStr_3.toString());
								connection3.setConnectTimeout(60000 * 6);
								connection3.setReadTimeout(60000 * 6);
							}
							if(connection3 == null)
							{
								System.out.println("XML compare : API connection failed");
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed");
								System.out.println("XML compare: Road Runner not received any response " + "'connection not established'");
								
							}
							else
							{
								System.out.println("XML compare : " + connection3.getResponseCode());
								log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API: "+ MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id")  +" - response : " + connection3.getResponseCode());
							}
							
							
							if(connection3 != null)
								connection3.disconnect();
							}
							catch(java.net.SocketTimeoutException ex3)
							{
								log.error(MessageQueue.WORK_ORDER + ": " + MessageQueue.TORNADO_HOST_LIVE_3 + " Http response time out: " + (String)ex3.getMessage());
								System.out.println("XML compare: Road Runner not received any response: " +  (String) ex3.getMessage());
							}
							catch (IOException ex3)
							{
								log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString()   + " Http IO exception: " + ex3.getMessage());
								System.out.println("XML compare: Road Runner not received any response: " +  (String) ex3.getMessage());
								
							}
							catch (Exception ex3) 
							{
								log.error(MessageQueue.WORK_ORDER + ": " + urlStr_3.toString()   + " Error Http connection: " + (String) ex3.getMessage());
								System.out.println("XML compare: Road Runner not received any response: " +  (String) ex3.getMessage());
								
							}
						
						}
						else
						{
						
						log.error(MessageQueue.WORK_ORDER + ": " +  MessageQueue.TORNADO_HOST_LIVE_2 + " Http response time out: " + (String)ex2.getMessage());
						System.out.println("XML compare: Road Runner not received any response: " +  (String) ex2.getMessage());
						}
					
					}
					catch (IOException ex2)
					{
						log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString()   + " Http IO exception: " + ex2.getMessage());
						System.out.println("XML compare: Road Runner not received any response: " +  (String) ex2.getMessage());
						
					}
					catch (Exception ex2) 
					{
						log.error(MessageQueue.WORK_ORDER + ": " + urlStr_2.toString()   + " Error Http connection: " + (String) ex2.getMessage());
						System.out.println("XML compare: Road Runner not received any response: " +  (String) ex2.getMessage());
						
					}

				}
				else
				{
					log.error(MessageQueue.WORK_ORDER + ": Error Http connection 'Failed' " + MessageQueue.TORNADO_HOST_LIVE_1 + " <> " +  MessageQueue.TORNADO_HOST );
					System.out.println("XML Compare connection not able to establish " );
				}

		}
		catch (IOException ex1)
		{
			log.error(MessageQueue.WORK_ORDER + ": " + "Http IO exception: " + ex1.getMessage());
			System.out.println("XML compare: Road Runner not received any response: " +  (String) ex1.getMessage());
		}
		catch (Exception ex1) 
		{
			log.error(MessageQueue.WORK_ORDER + ": " + "Error Http connection: " + (String) ex1.getMessage());
			System.out.println("XML compare: Road Runner not received any response: " +  (String) ex1.getMessage());
		}
	}
	
	
/*
	public static void UpdateToServer(JSONObject jsonObj, String actionStr) throws IOException {
		try {
			HttpsConnection httpsCon = new HttpsConnection();
			HttpURLConnection connection;
			log.info(MessageQueue.WORK_ORDER + ": " + "Before calling update to server post method");
			URL urlStr = new URL(
					MessageQueue.TORNADO_HOST + "/rest/pub/aaw/" + actionStr + "?mqid=" + (String) jsonObj.get("Id"));
			connection = (httpsCon.getURLConnection(urlStr, true));
			if (connection != null) {
				connection.setConnectTimeout(60000 * 2);
				connection.setReadTimeout(60000 * 2);
			}
			if (connection == null) {
				System.out.println("XML compare : API connection failed");
				log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed");
			} else if ((connection.getResponseCode() != HttpURLConnection.HTTP_OK || connection == null)
					&& MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_1)
					&& MessageQueue.TORNADO_ENV.equals("production")) {

				try {
					log.info(MessageQueue.WORK_ORDER + ": " + "Before calling update to server post method");
					URL urlStr_2 = new URL(MessageQueue.TORNADO_HOST_LIVE_2 + "/rest/pub/aaw/" + actionStr + "?mqid="
							+ (String) jsonObj.get("Id"));
					connection = (httpsCon.getURLConnection(urlStr_2, true));
					if (connection != null) {
						connection.setConnectTimeout(60000 * 2);
						connection.setReadTimeout(60000 * 2);
					}
					if (connection == null) {
						System.out.println("XML compare : API connection failed");
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed");
					} else {
						System.out.println("XML compare : " + connection.getResponseCode());
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API response : "
								+ connection.getResponseCode());
					}

					if (connection != null)
						connection.disconnect();
				} catch (java.net.SocketTimeoutException e) {
					log.error(MessageQueue.WORK_ORDER + ": " + "Http response time out: " + (String) e.getMessage());
				}

			} else {
				System.out.println("XML compare: " + connection.getResponseCode());
				log.error(
						MessageQueue.WORK_ORDER + ": " + "XML compare API response : " + connection.getResponseCode());
			}

			if (connection != null)
				connection.disconnect();

		} catch (java.net.SocketTimeoutException ex) {

			if (MessageQueue.TORNADO_HOST.equals(MessageQueue.TORNADO_HOST_LIVE_2)
					&& MessageQueue.TORNADO_ENV.equals("production")) {
				try {
					HttpsConnection httpsCon = new HttpsConnection();
					HttpURLConnection connection;
					log.info(MessageQueue.WORK_ORDER + ": " + "Before calling update to server post method");
					URL urlStr = new URL(MessageQueue.TORNADO_HOST_LIVE_3 + "/rest/pub/aaw/" + actionStr + "?mqid="
							+ (String) jsonObj.get("Id"));
					connection = (httpsCon.getURLConnection(urlStr, true));
					if (connection != null) {
						connection.setConnectTimeout(60000 * 2);
						connection.setReadTimeout(60000 * 2);
					}
					if (connection == null) {
						System.out.println("XML compare : API connection failed");
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare : API connection failed");
					} else {
						System.out.println("XML compare : " + connection.getResponseCode());
						log.error(MessageQueue.WORK_ORDER + ": " + "XML compare API response : "
								+ connection.getResponseCode());
					}

					if (connection != null)
						connection.disconnect();
				} catch (java.net.SocketTimeoutException e) {
					log.error(MessageQueue.WORK_ORDER + ": " + "Http response time out: " + (String) e.getMessage());
				}

			} else {

				log.error(MessageQueue.WORK_ORDER + ": " + "Http response time out: " + (String) ex.getMessage());
				// log.info(MessageQueue.WORK_ORDER + ": " + "Sent error status id : 26 - Road
				// runner not received any response");
			}
		} catch (IOException ex) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Http IO exception: " + ex.getMessage());
		} catch (Exception ex) {
			log.error(MessageQueue.WORK_ORDER + ": " + "Error Http connection: " + (String) ex.getMessage());
		}
	}
*/
	public static void UpdateReport(JSONObject jsonObj, String reportStr) throws IOException {
		try {
			HttpsConnection httpsCon = new HttpsConnection();
			httpsCon.excuteHttpJsonPost(MessageQueue.TORNADO_HOST + "/rest/pub/aaw/finalreport",
					(String) jsonObj.get("Id"), reportStr);
		} catch (Exception ex) {
			log.error((String) ex.getMessage());
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
		rowList = utls.ReadXLSXFile(utls.GetPathFromResource("smb.xlsx"), "sheet1");
		for (String[] row : rowList) {
			int noOfShareFolder = row.length - 3;
			for (int inc = 0; inc < noOfShareFolder; inc++) {
				SEng.MountVolume(row[0], row[1], row[2], row[row.length - inc - 1]);
			}
		}
	}

	public static void Mount() throws Exception {
		String[] arg = null;
		Utils utl = new Utils();
		arg = utl.ReadFromExcel("SMB.xls", true, 0, false, 0, false);
		int noOfShareFolder = arg.length - 3;
		for (int inc = 0; inc < noOfShareFolder; inc++) {
			SEng.MountVolume(arg[0], arg[1], arg[2], arg[arg.length - inc - 1]);
		}

	}

	public static void main(String args[]) throws Exception {
		String str = "//Users//yuvaraj//TEST FILES//AAW//446512A//100 XML//A02//dummy.xml";
		System.out.println(str.split("\\.")[0]);

	}

}
