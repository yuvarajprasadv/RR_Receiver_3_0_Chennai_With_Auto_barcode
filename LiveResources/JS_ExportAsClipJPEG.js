

main(arguments);
function main(argv)
{      
    try
    {
        sourceDoc = app.activeDocument;
        return ExportDocumentAsJPEG(sourceDoc, argv[0], argv[1].slice());
    }catch(exception){
        var errorString;
        errorString = "PDF Export failed. Document is not active";
        return errorString;
    }
}


function ExportDocumentAsJPEG(docSource, docTargetPath, jpegProperties)
{
	//jpegProperties = {"antiAliasing", "qualitySetting", "artBoardClipping", "horizontalScale", "verticalScale"};
		 
	try{
    	if(app.documents.length > 0)
    	{
    		var docFileSpec = new File ( docTargetPath );
    		var exportOptions = new ExportOptionsJPEG();
    		var type = ExportType.JPEG;
    		
    		 if(jpegProperties[0].toString() === 'true')
        		exportOptions.antiAliasing = true;
        	else if (jpegProperties[0].toString() == 'false')
        		exportOptions.antiAliasing = false;
        		
        	exportOptions.qualitySetting = jpegProperties[1].toString();
        		
        	if(jpegProperties[2].toString() === 'true')
        		exportOptions.artBoardClipping = true;
        	else if (jpegProperties[2].toString() == 'false')
        		exportOptions.artBoardClipping = false;
    		

        exportOptions.horizontalScale = jpegProperties[3].toString();
        exportOptions.verticalScale = jpegProperties[4].toString();
    		docSource.exportFile(docFileSpec, type, exportOptions);
    		return "JPEG exported successfully";
    		
    	}
    	else
        {
            var errorString;
            errorString = "PDF Export failed. Document is not loaded";
            return errorString;
        }
    }
    catch(exception)
    {
        return "PDF Export failed";
    }
}

