package Rcvr_AAMQ;
import org.json.simple.JSONObject;

import Rcvr_AAMQ.DUtils;

public class DJSUtils
{
	public String[] ExtractJsonToStringArray(String[] pdfProperties, String jsonString) throws Exception
	{
		String[] pdfPropertiesValue = new String[pdfProperties.length];
		
		DJsonParser jsnPr = new DJsonParser();
		JSONObject jsonObj = jsnPr.ParseJson(jsonString);
		
		for( int i = 0; i < pdfProperties.length; i++)
		{
		//	System.out.println(jsonObj.get(pdfProperties[i]).toString());
			pdfPropertiesValue[i] = jsonObj.get(pdfProperties[i]).toString();
		}
		return pdfPropertiesValue;
	}

}
