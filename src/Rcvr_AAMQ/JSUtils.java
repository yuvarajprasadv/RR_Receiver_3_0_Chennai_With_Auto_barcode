package Rcvr_AAMQ;
import org.json.simple.JSONObject;

import Rcvr_AAMQ.Utils;

public class JSUtils
{
/*
	public String[] ExtractJsonToStringArray()
	{
		String[] pdfProperties = {"acrobatLayers", "optimization"};
		String[] pdfPropertiesValue = new String[pdfProperties.length];
		JsonParser jsnPr = new JsonParser();
		
		String jsonString = "{ \"acrobatLayers\":false, \"optimization\":true}";
		JSONObject jsonObj = jsnPr.ParseJson(jsonString);
		
		for( int i = 0; i < pdfProperties.length; i++)
		{
			pdfPropertiesValue[i] = jsonObj.get(pdfProperties[i]).toString();
		}
		return pdfPropertiesValue;
	}*/
	
	
	public String[] ExtractJsonToStringArray(String[] pdfProperties, String jsonString)
	{
		String[] pdfPropertiesValue = new String[pdfProperties.length];
		
		JsonParser jsnPr = new JsonParser();
		JSONObject jsonObj = jsnPr.ParseJson(jsonString);
		
		for( int i = 0; i < pdfProperties.length; i++)
		{
			pdfPropertiesValue[i] = jsonObj.get(pdfProperties[i]).toString();
		}
		return pdfPropertiesValue;
	}

}
