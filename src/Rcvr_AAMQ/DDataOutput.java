package Rcvr_AAMQ;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.org.apache.xml.internal.security.utils.XMLUtils;


public class DDataOutput 
{
	static Logger log = DLogMQ.monitor("Rcvr_AAMQ.DDataOutput");

	public static String MAIN_PATH = "";
	public static String MASTER_ART_PATH = "";
	public static String XML_PATH = "";
	public static String QC_PATH = "";
	public static String QC_EXPORT_XML_PATH = "";
	public static String MASTER_TEMPLATE_PATH = null;
	public static String FILE_NAME_TO_SAVE = "";
	public static boolean IS_RR3DXML = false;
	
	public static String OUTPUT_AI = "";
	public static String OUTPUT_PDF = "";
	public static String OUTPUT_PSD = "";
	public static String OUTPUT_JPEG = "";
	public static String OUTPUT_PNG = "";
	public static String OUTPUT_BMP = "";
	public static String OUTPUT_GIF = "";
	public static String OUTPUT_TIFF = "";
	public static String OUTPUT_FLASH = "";
	public static String OUTPUT_NORMALISED_PDF = "";
	
	public static boolean SWATCH_ENABLE = false;
	public static String SWATCH_NAME = "";
	public static String SWATCH_ELEMENT = "";
	public static boolean SWATCH_FT_ENABLE = false;
	public static String SWATCH_FROM = "";
	public static String SWATCH_TO = "";
	
	public static boolean SOURCE_FILE_MOVE_ENABLE = false;
	public static String SOURCE_FILE_DESTINATION_MOVE_PATH = "";
	
	public static String RR3DXML_SAVE_PATH = "";
	public static String RENDER_SAVE_PATH = "";
	
	
	JSONObject outputJsonObj = null;
	String mainPath = "";
	public  void GetAllPath(JSONObject jsonObj) throws Exception
	{
		DUtils utils = new DUtils();
		DJsonParser jsonPars = new DJsonParser();
		
		JSONArray array = (JSONArray)jsonObj.get("aaw");
	  	JSONObject jsonRegionArr = (JSONObject)array.get(0);
	  	mainPath = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path"));
	  	MAIN_PATH = mainPath;
	  	if(jsonRegionArr.get("MasterTemplate") != null)
	  		MASTER_TEMPLATE_PATH = utils.RemoveForwardSlash((String) jsonRegionArr.get("MasterTemplate"));
	  	MASTER_ART_PATH = mainPath + utils.RemoveForwardSlash((String) jsonRegionArr.get("Master"));
	  	XML_PATH = mainPath + utils.RemoveForwardSlash((String) jsonRegionArr.get("XMLFile"));
	  	QC_PATH = mainPath + "080_QC/";
	  	QC_EXPORT_XML_PATH = (XML_PATH.replace("100_XML", "080_QC")).replace(".xml", "_export.xml");
	  	
	  	IS_RR3DXML = jsonPars.getJsonBooleanValueForKey(jsonObj, "region", "RR3DXML");
	  	
	  	GetFileNameToSave(jsonObj);
	
	}
	
	public void GetCustomizedOutput(JSONObject jsonObj)
	{
		DUtils utils = new DUtils();
		DJsonParser jsonPars = new DJsonParser();
		
		JSONArray array = (JSONArray)jsonObj.get("customizedOutput");
		JSONObject jsonCustomizedArr = (JSONObject)array.get(0);
		MessageQueue.PDF_PROPERTIES = jsonCustomizedArr.get("PDF").toString();
	}
	
	public static String GetLastIndex(String filePath) {
		int index = filePath.lastIndexOf("/");
		return filePath.substring(0, index);
	}
	
	public void GetFileNameToSave(JSONObject jsonObj) throws Exception
	{
		String masterProductionFilePath = MASTER_ART_PATH;
		DUtils utils = new DUtils();
		DXmlUtiility xmlUtils = new DXmlUtiility();
		String fileNameToSave = xmlUtils.getFileNameFromElement(XML_PATH);
		if (fileNameToSave != null) {
			FILE_NAME_TO_SAVE = fileNameToSave;
		}
		else if (fileNameToSave == null) {
			FILE_NAME_TO_SAVE = MessageQueue.WORK_ORDER;
		}
		// Turned off file naming appending with number when exists
		/*
		masterProductionFilePath = utils.GetNewNameIfFileExists(GetLastIndex(masterProductionFilePath) + "/" + FILE_NAME_TO_SAVE + ".ai");
		if(masterProductionFilePath != "" | !masterProductionFilePath.isEmpty())
		{
		int index = masterProductionFilePath.lastIndexOf("/");
		masterProductionFilePath = masterProductionFilePath.substring(index + 1, masterProductionFilePath.length());
		FILE_NAME_TO_SAVE = masterProductionFilePath.split("\\.")[0];
		}
		*/
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
	
	
	public void ExportData(String fileName) throws Exception
	{
		DUtils utils = new DUtils();
		DJsonParser jspr = new DJsonParser();
		Path file = Paths.get(utils.ConvertToAbsolutePath("/Applications/Adobe Illustrator "+ MessageQueue.VERSION +"/Plug-ins.localized/Sgk/Configuration/Data_Output.json"));

		outputJsonObj = jspr.ParseJsonFile(file.toString());
		
		JSONObject jsonOutputObj = (JSONObject)outputJsonObj.get("Output");
		
		if((boolean)((JSONObject)jsonOutputObj.get("AI")).get("enable") == true)
		{
			JSONArray aiArray = (JSONArray)((JSONObject)jsonOutputObj.get("AI")).get("Path");
			for(int i = 0; i < aiArray.size(); i++)
			{
				JSONObject jsonPng8PathArr = (JSONObject)aiArray.get(i);
				OUTPUT_AI = utils.RemoveForwardSlash((String) jsonPng8PathArr.get("Path"));
				if(!OUTPUT_AI.equalsIgnoreCase(""))
				DSEng.Export_DocumentAI(mainPath + OUTPUT_AI + "/" + fileName, jsonOutputObj.get("AI").toString());
			}
		}
		
		if((boolean)((JSONObject)jsonOutputObj.get("PNG")).get("enable") == true)
		{
			JSONArray png24Array = (JSONArray)((JSONObject)jsonOutputObj.get("PNG")).get("Path");
			for(int i = 0; i < png24Array.size(); i++)
			{
				JSONObject jsonPng24PathArr = (JSONObject)png24Array.get(i);
				OUTPUT_PNG = utils.RemoveForwardSlash((String) jsonPng24PathArr.get("Path"));
				if(!OUTPUT_PNG.equalsIgnoreCase(""))
				DSEng.ExportAsPNG(mainPath + OUTPUT_PNG + "/" + fileName, jsonOutputObj.get("PNG").toString());
			}
		}
		if((boolean)((JSONObject)jsonOutputObj.get("PSD")).get("enable") == true)
		{
			JSONArray psdArray = (JSONArray)((JSONObject)jsonOutputObj.get("PSD")).get("Path");
			for(int i = 0; i < psdArray.size(); i++)
			{
				JSONObject jsonPsdPathArr = (JSONObject)psdArray.get(i);
				OUTPUT_PSD = utils.RemoveForwardSlash((String) jsonPsdPathArr.get("Path"));
				if(!OUTPUT_PSD.equalsIgnoreCase(""))
				DSEng.ExportAsPSD(mainPath + OUTPUT_PSD + "/" + fileName, jsonOutputObj.get("PSD").toString());
			}
		}
		if((boolean)((JSONObject)jsonOutputObj.get("JPEG")).get("enable") == true)
		{
			JSONArray jpgArray = (JSONArray)((JSONObject)jsonOutputObj.get("JPEG")).get("Path");
			for(int i = 0; i < jpgArray.size(); i++)
			{
				JSONObject jsonJpegPathArr = (JSONObject)jpgArray.get(i);
				OUTPUT_JPEG = utils.RemoveForwardSlash((String) jsonJpegPathArr.get("Path"));
				if(!OUTPUT_JPEG.equalsIgnoreCase(""))
				DSEng.ExportAsNormalJPEG(mainPath + OUTPUT_JPEG + "/" + fileName, jsonOutputObj.get("JPEG").toString());
			}
		}
		if((boolean)((JSONObject)jsonOutputObj.get("GIF")).get("enable") == true)
		{
			JSONArray gifArray = (JSONArray)((JSONObject)jsonOutputObj.get("GIF")).get("Path");
			for(int i = 0; i < gifArray.size(); i++)
			{
				JSONObject jsonGifPathArr = (JSONObject)gifArray.get(i);
				OUTPUT_GIF = utils.RemoveForwardSlash((String) jsonGifPathArr.get("Path"));
				if(!OUTPUT_GIF.equalsIgnoreCase(""))
				DSEng.ExportAsGIF(mainPath + OUTPUT_GIF + "/" + fileName, jsonOutputObj.get("GIF").toString());
			}
		}
		if((boolean)((JSONObject)jsonOutputObj.get("TIFF")).get("enable") == true)
		{
			JSONArray tiffArray = (JSONArray)((JSONObject)jsonOutputObj.get("TIFF")).get("Path");
			for(int i = 0; i < tiffArray.size(); i++)
			{
				JSONObject jsonTiffPathArr = (JSONObject)tiffArray.get(i);
				OUTPUT_TIFF = utils.RemoveForwardSlash((String) jsonTiffPathArr.get("Path"));
				if(!OUTPUT_TIFF.equalsIgnoreCase(""))
				DSEng.ExportAsTIFF(mainPath + OUTPUT_TIFF + "/" + fileName, jsonOutputObj.get("TIFF").toString());
			}
		}
		if((boolean)((JSONObject)jsonOutputObj.get("FLASH")).get("enable") == true)
		{
			JSONArray flashArray = (JSONArray)((JSONObject)jsonOutputObj.get("FLASH")).get("Path");
			for(int i = 0; i < flashArray.size(); i++)
			{
				JSONObject jsonFlashPathArr = (JSONObject)flashArray.get(i);
				OUTPUT_FLASH = utils.RemoveForwardSlash((String) jsonFlashPathArr.get("Path"));
				if(!OUTPUT_FLASH.equalsIgnoreCase(""))
				DSEng.ExportAsFLASH(mainPath + OUTPUT_FLASH + "/" + fileName, jsonOutputObj.get("FLASH").toString());
			}
		}
		if((boolean)((JSONObject)jsonOutputObj.get("PDF")).get("enable") == true)
		{
			JSONArray flashArray = (JSONArray)((JSONObject)jsonOutputObj.get("PDF")).get("Path");
			for(int i = 0; i < flashArray.size(); i++)
			{
				JSONObject jsonPdfPathArr = (JSONObject)flashArray.get(i);
				OUTPUT_PDF = utils.RemoveForwardSlash((String) jsonPdfPathArr.get("Path"));
				if(!OUTPUT_PDF.equalsIgnoreCase(""))
				DSEng.ExportAsNormalPDF(mainPath + OUTPUT_PDF + "/" + fileName, jsonOutputObj.get("PDF").toString());
			}
		}
		if((boolean)((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("enable") == true)
		{
			if(PostMultipleJobPreProcess())
			{
				String borderMode = ((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("borderMode").toString();
				if(borderMode.equalsIgnoreCase("kBordersModeArtworkBoundingBox"))
				{
					((JSONObject)jsonOutputObj.get("Normalized PDF Export")).put("border_Mode", 1);
				}
				
				else if(borderMode.equalsIgnoreCase("kBordersModeCurrentArtboard"))
					((JSONObject)jsonOutputObj.get("Normalized PDF Export")).put("border_Mode", 2);
				else
					((JSONObject)jsonOutputObj.get("Normalized PDF Export")).put("border_Mode", 3);
				
				
				OUTPUT_NORMALISED_PDF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("3DPath").toString());
				if(OUTPUT_NORMALISED_PDF != null && OUTPUT_NORMALISED_PDF != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_NORMALISED_PDF, false);
					if(!OUTPUT_NORMALISED_PDF.equalsIgnoreCase(""))
						DSEng.ExportAsNormalisedPDF(mainPath + OUTPUT_NORMALISED_PDF + "/" + fileName, jsonOutputObj.get("Normalized PDF Export").toString());
				}
			}
		}
		
		
		
		JSONObject jsonSwatchObj = (JSONObject)jsonOutputObj.get("Swatch");
		
		SWATCH_ENABLE = (boolean)(jsonSwatchObj.get("enable"));
		if(SWATCH_ENABLE == true)
		{
			SWATCH_NAME = (String)(jsonSwatchObj.get("swatchName"));
			SWATCH_ELEMENT = (String)(jsonSwatchObj.get("swatchElement"));
		}
		
		JSONObject jsonSwatchFromToObj = (JSONObject)jsonOutputObj.get("SwatchFromTo");
		SWATCH_FT_ENABLE = (boolean)(jsonSwatchFromToObj.get("enable"));
		if(SWATCH_FT_ENABLE == true)
		{
			SWATCH_FROM = (String)(jsonSwatchFromToObj.get("swatchFrom"));
			SWATCH_TO = (String)(jsonSwatchFromToObj.get("swatchTo"));
		}
		
		JSONObject jsonSourceFileMoveObj = (JSONObject)jsonOutputObj.get("MoveSourceFile");
		SOURCE_FILE_MOVE_ENABLE = (boolean)(jsonSourceFileMoveObj.get("enable"));
		if(SOURCE_FILE_MOVE_ENABLE == true)
		{
			SOURCE_FILE_DESTINATION_MOVE_PATH = (String)(jsonSourceFileMoveObj.get("destinationFolderPath"));
		}
		
		JSONObject json3DXMLObj = (JSONObject)jsonOutputObj.get("RR3DXML");
		if((boolean)(json3DXMLObj.get("enable")) == true)
		{
		  RR3DXML_SAVE_PATH = (String)(json3DXMLObj.get("savePath"));
		}
		
		if((boolean)((JSONObject)jsonOutputObj.get("renderPath")).get("enable") == true)
		{
		  RENDER_SAVE_PATH = (String)((JSONObject)jsonOutputObj.get("renderPath")).get("Path");
		}
	}
	
	
	public void ExportCustomizedData(JSONObject outputJsonObj, String fileName, boolean is3DXML) throws Exception 
	{
			try {
		DUtils utils = new DUtils();
		JSONObject jsonObj = outputJsonObj;
		DJsonParser jspr = new DJsonParser();
		/*
		
//		Path file = Paths.get(utils.ConvertToAbsolutePath("/Applications/Adobe Illustrator "+ MessageQueue.VERSION +"/Plug-ins.localized/Sgk/Configuration/Customized_Data_Output.json"));
//	
//		outputJsonObj = jspr.ParseJsonFile(file.toString());
//	
//		GetAllPath(outputJsonObj);
	   */
		
		JSONArray jsonOutputArray = (JSONArray)outputJsonObj.get("customizedOutput");
		JSONObject jsonOutputObj = (JSONObject)jsonOutputArray.get(0);
		
		if(!is3DXML && MessageQueue.RR_SOURCE.equalsIgnoreCase("TORNADO_RR"))
		{
			boolean barCodeVisible = true;
			boolean legendVisible = true;
			boolean pdfOnly = false;
			boolean bmpformat = false;
			boolean clipJPEG = false;
			
			if(jspr.getJsonValueFromGroupKey(jsonObj, "region", "barCodeVisible") != null);
				barCodeVisible = Boolean.parseBoolean(jspr.getJsonValueFromGroupKey(jsonObj, "region", "barCodeVisible"));
			if(jspr.getJsonValueFromGroupKey(jsonObj, "region", "legendVisible") != null)
				legendVisible =  Boolean.parseBoolean(jspr.getJsonValueFromGroupKey(jsonObj, "region", "legendVisible"));
			if(jspr.getJsonValueFromGroupKey(jsonObj, "region", "pdf") != null)
				pdfOnly =  Boolean.parseBoolean(jspr.getJsonValueFromGroupKey(jsonObj, "region", "pdf"));
			if(jspr.getJsonValueFromGroupKey(jsonObj, "region", "bmp") != null)
				bmpformat = Boolean.parseBoolean(jspr.getJsonValueFromGroupKey(jsonObj, "region", "bmp"));
			if(jspr.getJsonValueFromGroupKey(jsonObj, "region", "jpg") != null)
				clipJPEG = Boolean.parseBoolean(jspr.getJsonValueFromGroupKey(jsonObj, "region", "jpg"));
			
			if (barCodeVisible)
				SEng.SetLayerVisibleOff("true");
			else
				SEng.SetLayerVisibleOff("false");
			
			
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("PNG")).get("enable") == true)
			{
				OUTPUT_PNG =   utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("PNG")).get("path").toString());
				
				String pngType = ((JSONObject)jsonOutputObj.get("PNG")).get("type").toString();
				if(pngType.equalsIgnoreCase("png8"))
					((JSONObject)jsonOutputObj.get("PNG")).put("type", 8);
				else
					((JSONObject)jsonOutputObj.get("PNG")).put("type", 24);
				
				if(OUTPUT_PNG != null && OUTPUT_PNG != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_PNG, false);
					if(!OUTPUT_PNG.equalsIgnoreCase(""))
						DSEng.ExportAsPNG(mainPath + OUTPUT_PNG + "/" + fileName, jsonOutputObj.get("PNG").toString());
				}
				else
				{
					log.error("Failed to create PNG Output: not valid path - " + OUTPUT_PNG);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create PNG Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("PSD")).get("enable") == true)
			{
				OUTPUT_PSD = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("PSD")).get("path").toString());
				if(OUTPUT_PSD != null && OUTPUT_PSD != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_PSD, false);
					if(!OUTPUT_PSD.equalsIgnoreCase(""))
						DSEng.ExportAsPSD(mainPath + OUTPUT_PSD + "/" + fileName, jsonOutputObj.get("PSD").toString());
				}else
				{
					log.error("Failed to create PSD Output: not valid path - " + OUTPUT_PSD);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create PSD Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("JPEG Export")).get("enable") == true && clipJPEG)
			{
				OUTPUT_JPEG = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("JPEG Export")).get("path").toString());
				if(OUTPUT_JPEG != null && OUTPUT_JPEG != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_JPEG, false);
					if(!OUTPUT_JPEG.equalsIgnoreCase(""))
						DSEng.ExportAsNormalJPEG(mainPath + OUTPUT_JPEG + "/" + fileName, jsonOutputObj.get("JPEG Export").toString());
				}else
				{
					log.error("Failed to create JPEG Output: not valid path - " + OUTPUT_JPEG);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create JPEG Output: "+ ex.getMessage());
			}
			
			try
			{
			if((boolean)((JSONObject)jsonOutputObj.get("BMP")).get("enable") == true && bmpformat)
			{
				DImageConvertor imageConvertor = new DImageConvertor();
				
				OUTPUT_BMP = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("BMP")).get("path").toString());
				if(OUTPUT_BMP != null && OUTPUT_BMP != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_BMP, false);
					if(!OUTPUT_BMP.equalsIgnoreCase(""))
						DSEng.ExportAsNormalJPEG(mainPath + OUTPUT_BMP + fileName + "-1", jsonOutputObj.get("BMP").toString());
				
				Thread.sleep(1);
				String inputImagePath = mainPath + OUTPUT_BMP + fileName + "-1.jpg";
				String outputImagePath = mainPath + OUTPUT_BMP + fileName + "." + "bmp";
		
				imageConvertor.ConvertImageTo(inputImagePath, outputImagePath, "bmp");
		
				File jpegFile = new File( mainPath + OUTPUT_BMP + fileName + "-1.jpg");
				if(jpegFile != null)
					jpegFile.delete();
				}else
				{
					log.error("Failed to create BMP Output: not valid path - " + OUTPUT_BMP);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create BMP Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("GIF")).get("enable") == true)
			{
				OUTPUT_GIF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("GIF")).get("path").toString());
				if(OUTPUT_GIF != null && OUTPUT_GIF != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_GIF, false);
					if(!OUTPUT_GIF.equalsIgnoreCase(""))
						DSEng.ExportAsGIF(mainPath + OUTPUT_GIF + "/" + fileName, jsonOutputObj.get("GIF").toString());
				}else
				{
					log.error("Failed to create GIF Output: not valid path - " + OUTPUT_GIF);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create GIF Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("TIFF")).get("enable") == true)
			{
				OUTPUT_TIFF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("TIFF")).get("path").toString());
				if(OUTPUT_TIFF != null && OUTPUT_TIFF != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_TIFF, false);
					if(!OUTPUT_TIFF.equalsIgnoreCase(""))
						DSEng.ExportAsTIFF(mainPath + OUTPUT_TIFF + "/" + fileName, jsonOutputObj.get("TIFF").toString());
				}else
				{
					log.error("Failed to create TIFF Output: not valid path - " + OUTPUT_TIFF);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create TIFF Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("FLASH")).get("enable") == true)
			{
				OUTPUT_FLASH = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("FLASH")).get("path").toString());
				if(OUTPUT_FLASH != null && OUTPUT_FLASH != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_FLASH, false);
					if(!OUTPUT_FLASH.equalsIgnoreCase(""))
						DSEng.ExportAsFLASH(mainPath + OUTPUT_FLASH + "/" + fileName, jsonOutputObj.get("FLASH").toString());
				}else
				{
					log.error("Failed to create FLASH Output: not valid path - " + OUTPUT_FLASH);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create FLASH Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("PDF")).get("enable") == true && pdfOnly)
			{
				

				if(pdfOnly)
				{
				DSEng.OutlineText();

				if (legendVisible)
				{
					DSEng.SetLegendVisibleOff("true");
				}
				else
				{
					DSEng.SetLegendVisibleOff("false");
				}

				DSEng.EmbedPlacedItems();
				}
				
				
				
				OUTPUT_PDF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("PDF")).get("path").toString());
				if(OUTPUT_PDF != null && OUTPUT_PDF != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_PDF, false);
					if(!OUTPUT_PDF.equalsIgnoreCase(""))
						DSEng.ExportAsNormalPDF(mainPath + OUTPUT_PDF + "/" + fileName, jsonOutputObj.get("PDF").toString());
				}else
				{
					log.error("Failed to create PDF Output: not valid path - " + OUTPUT_PDF);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create PDF Output: "+ ex.getMessage());
			}
			
			if((boolean)((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("enable") == true)
			{
				try {
				if(PostMultipleJobPreProcess())
				{
					String borderMode = ((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("borderMode").toString();
					if(borderMode.equalsIgnoreCase("kBordersModeArtworkBoundingBox"))
						((JSONObject)jsonOutputObj.get("Normalized PDF Export")).put("border_Mode", 1);
					else if(borderMode.equalsIgnoreCase("kBordersModeCurrentArtboard"))
						((JSONObject)jsonOutputObj.get("Normalized PDF Export")).put("border_Mode", 2);
					else
						((JSONObject)jsonOutputObj.get("Normalised PDF Export")).put("border_Mode", 3);
					
					
					OUTPUT_NORMALISED_PDF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("path").toString());
					if(OUTPUT_NORMALISED_PDF != null && OUTPUT_NORMALISED_PDF != "")
					{
						utils.CreateNewDirectory(mainPath + OUTPUT_NORMALISED_PDF, false);
						if(!OUTPUT_NORMALISED_PDF.equalsIgnoreCase(""))
							DSEng.ExportAsNormalPDF(mainPath + OUTPUT_NORMALISED_PDF + "/" + fileName + "_Normalised", jsonOutputObj.get("Normalized PDF Export").toString());
					}else
					{
						log.error("Failed to create Normalised PDF Output: not valid path - " + OUTPUT_NORMALISED_PDF);
					}
				}
				}catch(Exception ex)
				{
					log.error("Failed to create Normalised PDF Output: "+ ex.getMessage());
				}
				
			}
			
			SWATCH_ENABLE = (boolean)((JSONObject)jsonOutputObj.get("Swatch")).get("enable");
			if(SWATCH_ENABLE == true)
			{
				SWATCH_NAME = (String)((JSONObject)jsonOutputObj.get("Swatch")).get("swatchName");
				SWATCH_ELEMENT = (String)((JSONObject)jsonOutputObj.get("Swatch")).get("swatchElement");
			}
			
			SWATCH_FT_ENABLE = (boolean)((JSONObject)jsonOutputObj.get("SwatchFromTo")).get("enable");
			if(SWATCH_FT_ENABLE == true)
			{
				SWATCH_FROM = (String)((JSONObject)jsonOutputObj.get("SwatchFromTo")).get("swatchFrom");
				SWATCH_TO = (String)((JSONObject)jsonOutputObj.get("SwatchFromTo")).get("swatchTo");
			}
			
		
		}
		else if(!is3DXML)
		{

			try {
			if((boolean)((JSONObject)jsonOutputObj.get("PNG")).get("enable") == true)
			{
				OUTPUT_PNG =   utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("PNG")).get("path").toString());
				
				String pngType = ((JSONObject)jsonOutputObj.get("PNG")).get("type").toString();
				if(pngType.equalsIgnoreCase("png8"))
					((JSONObject)jsonOutputObj.get("PNG")).put("type", 8);
				else
					((JSONObject)jsonOutputObj.get("PNG")).put("type", 24);
				
				if(OUTPUT_PNG != null && OUTPUT_PNG != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_PNG, false);
					if(!OUTPUT_PNG.equalsIgnoreCase(""))
						DSEng.ExportAsPNG(mainPath + OUTPUT_PNG + "/" + fileName, jsonOutputObj.get("PNG").toString());
				}
				else
				{
					log.error("Failed to create PNG Output: not valid path - " + OUTPUT_PNG);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create PNG Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("PSD")).get("enable") == true)
			{
				OUTPUT_PSD = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("PSD")).get("path").toString());
				if(OUTPUT_PSD != null && OUTPUT_PSD != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_PSD, false);
					if(!OUTPUT_PSD.equalsIgnoreCase(""))
						DSEng.ExportAsPSD(mainPath + OUTPUT_PSD + "/" + fileName, jsonOutputObj.get("PSD").toString());
				}else
				{
					log.error("Failed to create PSD Output: not valid path - " + OUTPUT_PSD);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create PSD Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("JPEG Export")).get("enable") == true)
			{
				OUTPUT_JPEG = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("JPEG Export")).get("path").toString());
				if(OUTPUT_JPEG != null && OUTPUT_JPEG != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_JPEG, false);
					if(!OUTPUT_JPEG.equalsIgnoreCase(""))
						DSEng.ExportAsNormalJPEG(mainPath + OUTPUT_JPEG + "/" + fileName, jsonOutputObj.get("JPEG Export").toString());
				}else
				{
					log.error("Failed to create JPEG Output: not valid path - " + OUTPUT_JPEG);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create JPEG Output: "+ ex.getMessage());
			}
			
			try
			{
			if((boolean)((JSONObject)jsonOutputObj.get("BMP")).get("enable") == true)
			{
				DImageConvertor imageConvertor = new DImageConvertor();
				
				OUTPUT_BMP = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("BMP")).get("path").toString());
				if(OUTPUT_BMP != null && OUTPUT_BMP != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_BMP, false);
					if(!OUTPUT_BMP.equalsIgnoreCase(""))
						DSEng.ExportAsNormalJPEG(mainPath + OUTPUT_BMP + fileName + "-1", jsonOutputObj.get("BMP").toString());
				
				Thread.sleep(1);
				String inputImagePath = mainPath + OUTPUT_BMP + fileName + "-1.jpg";
				String outputImagePath = mainPath + OUTPUT_BMP + fileName + "." + "bmp";
		
				imageConvertor.ConvertImageTo(inputImagePath, outputImagePath, "bmp");
		
				File jpegFile = new File( mainPath + OUTPUT_BMP + fileName + "-1.jpg");
				if(jpegFile != null)
					jpegFile.delete();
				}else
				{
					log.error("Failed to create BMP Output: not valid path - " + OUTPUT_BMP);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create BMP Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("GIF")).get("enable") == true)
			{
				OUTPUT_GIF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("GIF")).get("path").toString());
				if(OUTPUT_GIF != null && OUTPUT_GIF != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_GIF, false);
					if(!OUTPUT_GIF.equalsIgnoreCase(""))
						DSEng.ExportAsGIF(mainPath + OUTPUT_GIF + "/" + fileName, jsonOutputObj.get("GIF").toString());
				}else
				{
					log.error("Failed to create GIF Output: not valid path - " + OUTPUT_GIF);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create GIF Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("TIFF")).get("enable") == true)
			{
				OUTPUT_TIFF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("TIFF")).get("path").toString());
				if(OUTPUT_TIFF != null && OUTPUT_TIFF != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_TIFF, false);
					if(!OUTPUT_TIFF.equalsIgnoreCase(""))
						DSEng.ExportAsTIFF(mainPath + OUTPUT_TIFF + "/" + fileName, jsonOutputObj.get("TIFF").toString());
				}else
				{
					log.error("Failed to create TIFF Output: not valid path - " + OUTPUT_TIFF);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create TIFF Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("FLASH")).get("enable") == true)
			{
				OUTPUT_FLASH = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("FLASH")).get("path").toString());
				if(OUTPUT_FLASH != null && OUTPUT_FLASH != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_FLASH, false);
					if(!OUTPUT_FLASH.equalsIgnoreCase(""))
						DSEng.ExportAsFLASH(mainPath + OUTPUT_FLASH + "/" + fileName, jsonOutputObj.get("FLASH").toString());
				}else
				{
					log.error("Failed to create FLASH Output: not valid path - " + OUTPUT_FLASH);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create FLASH Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("PDF")).get("enable") == true)
			{
				OUTPUT_PDF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("PDF")).get("path").toString());
				if(OUTPUT_PDF != null && OUTPUT_PDF != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_PDF, false);
					if(!OUTPUT_PDF.equalsIgnoreCase(""))
						DSEng.ExportAsNormalPDF(mainPath + OUTPUT_PDF + "/" + fileName, jsonOutputObj.get("PDF").toString());
				}else
				{
					log.error("Failed to create PDF Output: not valid path - " + OUTPUT_PDF);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create PDF Output: "+ ex.getMessage());
			}
			
			if((boolean)((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("enable") == true)
			{
				try {
				if(PostMultipleJobPreProcess())
				{
					String borderMode = ((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("borderMode").toString();
					if(borderMode.equalsIgnoreCase("kBordersModeArtworkBoundingBox"))
						((JSONObject)jsonOutputObj.get("Normalized PDF Export")).put("border_Mode", 1);
					else if(borderMode.equalsIgnoreCase("kBordersModeCurrentArtboard"))
						((JSONObject)jsonOutputObj.get("Normalized PDF Export")).put("border_Mode", 2);
					else
						((JSONObject)jsonOutputObj.get("Normalised PDF Export")).put("border_Mode", 3);
					
					
					OUTPUT_NORMALISED_PDF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("path").toString());
					if(OUTPUT_NORMALISED_PDF != null && OUTPUT_NORMALISED_PDF != "")
					{
						utils.CreateNewDirectory(mainPath + OUTPUT_NORMALISED_PDF, false);
						if(!OUTPUT_NORMALISED_PDF.equalsIgnoreCase(""))
							DSEng.ExportAsNormalPDF(mainPath + OUTPUT_NORMALISED_PDF + "/" + fileName + "_Normalised", jsonOutputObj.get("Normalized PDF Export").toString());
					}else
					{
						log.error("Failed to create Normalised PDF Output: not valid path - " + OUTPUT_NORMALISED_PDF);
					}
				}
				}catch(Exception ex)
				{
					log.error("Failed to create Normalised PDF Output: "+ ex.getMessage());
				}
				
			}
			
			SWATCH_ENABLE = (boolean)((JSONObject)jsonOutputObj.get("Swatch")).get("enable");
			if(SWATCH_ENABLE == true)
			{
				SWATCH_NAME = (String)((JSONObject)jsonOutputObj.get("Swatch")).get("swatchName");
				SWATCH_ELEMENT = (String)((JSONObject)jsonOutputObj.get("Swatch")).get("swatchElement");
			}
			
			SWATCH_FT_ENABLE = (boolean)((JSONObject)jsonOutputObj.get("SwatchFromTo")).get("enable");
			if(SWATCH_FT_ENABLE == true)
			{
				SWATCH_FROM = (String)((JSONObject)jsonOutputObj.get("SwatchFromTo")).get("swatchFrom");
				SWATCH_TO = (String)((JSONObject)jsonOutputObj.get("SwatchFromTo")).get("swatchTo");
			}
			
		}
		else if(is3DXML)
		{
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("PNG")).get("3D") == true)
			{
				OUTPUT_PNG =   utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("PNG")).get("3DPath").toString());
	
				String pngType = ((JSONObject)jsonOutputObj.get("PNG")).get("type").toString();
				if(pngType.equalsIgnoreCase("png8"))
					((JSONObject)jsonOutputObj.get("PNG")).put("type", 8);
				else
					((JSONObject)jsonOutputObj.get("PNG")).put("type", 24);
				
				if(OUTPUT_PNG != null && OUTPUT_PNG != "")
				{
					String customizedDataPath = utils.RemoveForwardSlash(mainPath + OUTPUT_PNG);
					if (!DUtils.IsFolderExists(customizedDataPath)) {
						utils.CreateNewDirectory(customizedDataPath, false);
					}
					
					if(!OUTPUT_PNG.equalsIgnoreCase(""))
						DSEng.ExportAsPNG(customizedDataPath + "/" + fileName, jsonOutputObj.get("PNG").toString());
				}else
				{
					log.error("Failed to create PNG Output: not valid path - " + OUTPUT_PNG);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create PNG Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("PSD")).get("3D") == true)
			{
				OUTPUT_PSD = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("PSD")).get("3DPath").toString());
				if(OUTPUT_PSD != null && OUTPUT_PSD != "")
				{
					String customizedDataPath = utils.RemoveForwardSlash(mainPath + OUTPUT_PSD);
					if (!DUtils.IsFolderExists(customizedDataPath)) {
						utils.CreateNewDirectory(customizedDataPath, false);
					}
					
					if(!OUTPUT_PSD.equalsIgnoreCase(""))
						DSEng.ExportAsPSD(customizedDataPath + "/" + fileName, jsonOutputObj.get("PSD").toString());
				}else
				{
					log.error("Failed to create PSD Output: not valid path - " + OUTPUT_PSD);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create PSD Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("JPEG Export")).get("3D") == true)
			{
				OUTPUT_JPEG = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("JPEG Export")).get("3DPath").toString());
				if(OUTPUT_JPEG != null && OUTPUT_JPEG != "")
				{
					String customizedDataPath = utils.RemoveForwardSlash(mainPath + OUTPUT_JPEG);
					if (!DUtils.IsFolderExists(customizedDataPath)) {
						utils.CreateNewDirectory(customizedDataPath, false);
					}
					
					if(!OUTPUT_JPEG.equalsIgnoreCase(""))
						DSEng.ExportAsNormalJPEG(customizedDataPath + "/" + fileName, jsonOutputObj.get("JPEG Export").toString());
				}else
				{
					log.error("Failed to create JPEG Output: not valid path - " + OUTPUT_JPEG);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create JPEG Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("BMP")).get("3D") == true)
			{
				DImageConvertor imageConvertor = new DImageConvertor();
				
				OUTPUT_BMP = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("BMP")).get("3DPath").toString());
				if(OUTPUT_BMP != null && OUTPUT_BMP != "")
				{
					String customizedDataPath = utils.RemoveForwardSlash(mainPath + OUTPUT_BMP);
					if (!DUtils.IsFolderExists(customizedDataPath)) {
						utils.CreateNewDirectory(customizedDataPath, false);
					}
					
					if(!OUTPUT_BMP.equalsIgnoreCase(""))
						DSEng.ExportAsNormalJPEG(customizedDataPath + fileName + "-1", jsonOutputObj.get("BMP").toString());
				
				Thread.sleep(1);
				String inputImagePath = customizedDataPath + fileName + "-1.jpg";
				String outputImagePath = customizedDataPath + fileName + "." + "bmp";
		
				imageConvertor.ConvertImageTo(inputImagePath, outputImagePath, "bmp");
		
				File jpegFile = new File( customizedDataPath + fileName + "-1.jpg");
				if(jpegFile != null)
					jpegFile.delete();
				}else
				{
					log.error("Failed to create BMP Output: not valid path - " + OUTPUT_BMP);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create BMP Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("GIF")).get("3D") == true)
			{
				OUTPUT_GIF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("GIF")).get("3DPath").toString());
				if(OUTPUT_GIF != null && OUTPUT_GIF != "")
				{
					String customizedDataPath = utils.RemoveForwardSlash(mainPath + OUTPUT_GIF);
					if (!DUtils.IsFolderExists(customizedDataPath)) {
						utils.CreateNewDirectory(customizedDataPath, false);
					}
					
					if(!OUTPUT_GIF.equalsIgnoreCase(""))
						DSEng.ExportAsGIF(customizedDataPath + "/" + fileName, jsonOutputObj.get("GIF").toString());
				}else
				{
					log.error("Failed to create GIF Output: not valid path - " + OUTPUT_GIF);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create GIF Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("TIFF")).get("3D") == true)
			{
				OUTPUT_TIFF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("TIFF")).get("3DPath").toString());
				if(OUTPUT_TIFF != null && OUTPUT_TIFF != "")
				{
					String customizedDataPath = utils.RemoveForwardSlash(mainPath + OUTPUT_TIFF);
					if (!DUtils.IsFolderExists(customizedDataPath)) {
						utils.CreateNewDirectory(customizedDataPath, false);
					}
					
					if(!OUTPUT_TIFF.equalsIgnoreCase(""))
						DSEng.ExportAsTIFF(customizedDataPath + "/" + fileName, jsonOutputObj.get("TIFF").toString());
				}else
				{
					log.error("Failed to create TIFF Output: not valid path - " + OUTPUT_TIFF);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create TIFF Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("FLASH")).get("3D") == true)
			{
				OUTPUT_FLASH = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("FLASH")).get("3DPath").toString());
				if(OUTPUT_FLASH != null && OUTPUT_FLASH != "")
				{
					String customizedDataPath = utils.RemoveForwardSlash(mainPath + OUTPUT_FLASH);
					if (!DUtils.IsFolderExists(customizedDataPath)) {
						utils.CreateNewDirectory(customizedDataPath, false);
					}
					
					if(!OUTPUT_FLASH.equalsIgnoreCase(""))
						DSEng.ExportAsFLASH(customizedDataPath + "/" + fileName, jsonOutputObj.get("FLASH").toString());
				}else
				{
					log.error("Failed to create FLASH Output: not valid path - " + OUTPUT_FLASH);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create FLASH Output: "+ ex.getMessage());
			}
			
			try {
			if((boolean)((JSONObject)jsonOutputObj.get("PDF")).get("3D") == true)
			{
				OUTPUT_PDF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("PDF")).get("3DPath").toString());
				if(OUTPUT_PDF != null && OUTPUT_PDF != "")
				{
					utils.CreateNewDirectory(mainPath + OUTPUT_PDF, false);
					if(!OUTPUT_PDF.equalsIgnoreCase(""))
						DSEng.ExportAsNormalPDF(mainPath + OUTPUT_PDF + "/" + fileName, jsonOutputObj.get("PDF").toString());
				}else
				{
					log.error("Failed to create PDF Output: not valid path - " + OUTPUT_PDF);
				}
			}
			}catch(Exception ex)
			{
				log.error("Failed to create PDF Output: "+ ex.getMessage());
			}
			
			if((boolean)((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("3D") == true)
			{
				try {
				if(PostMultipleJobPreProcess())
				{
					JSONObject newJsonKeyPair;
					String borderMode = ((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("borderMode").toString();
					if(borderMode.equalsIgnoreCase("kBordersModeArtworkBoundingBox"))
					{
						((JSONObject)jsonOutputObj.get("Normalized PDF Export")).put("border_Mode", 1);
					}
					
					else if(borderMode.equalsIgnoreCase("kBordersModeCurrentArtboard"))
						((JSONObject)jsonOutputObj.get("Normalized PDF Export")).put("border_Mode", 2);
					else
						((JSONObject)jsonOutputObj.get("Normalized PDF Export")).put("border_Mode", 3);
					
					
					OUTPUT_NORMALISED_PDF = utils.RemoveForwardSlash(((JSONObject)jsonOutputObj.get("Normalized PDF Export")).get("3DPath").toString());
					if(OUTPUT_NORMALISED_PDF != null && OUTPUT_NORMALISED_PDF != "")
					{
						utils.CreateNewDirectory(mainPath + OUTPUT_NORMALISED_PDF, false);
						if(!OUTPUT_NORMALISED_PDF.equalsIgnoreCase(""))
							DSEng.ExportAsNormalisedPDF(mainPath + OUTPUT_NORMALISED_PDF + "/" + fileName, jsonOutputObj.get("Normalized PDF Export").toString());
					}else
					{
						log.error("Failed to create Normalised PDF Output: not valid path - " + OUTPUT_NORMALISED_PDF);
					}
				}
				}catch(Exception ex)
				{
					log.error("Failed to create Normalised PDF Output: "+ ex.getMessage());
				}
			}
		}
	}
	catch (Exception ex)	
	{
		log.error("Exception on generating customized ouput: " + ex.getMessage());
	}		
}
	
	
	   public static void main(String[] args) throws Exception {
		
		   MessageQueue.TORNADO_ENV = "development";
		   MessageQueue.VERSION = "CC 2018";
		   DDataOutput DO = new DDataOutput();
		   //DO.ExportData("TEST");
		   DO.ExportCustomizedData(null, "TEST", true);
	   }
	
}
