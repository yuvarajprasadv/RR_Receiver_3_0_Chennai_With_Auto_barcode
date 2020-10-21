
main(arguments);

function main(argv)
{      
    try
    {
    
   
    		app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;
        sourceDoc = app.activeDocument;
        return ExportDocumentAsTIFF(sourceDoc, argv[0], argv[1].slice());
        
    }catch(exception){
  //  $.writeln( "ex " + exception);
        var errorString;
        errorString = "TIFF Export failed. Document is not active " +  exception;
        return errorString;
    }
}

function ExportDocumentAsTIFF(docSource, docTargetPath, tiffProperties)
{
	//tiffProperties = {"resolution", "antiAliazing", "IZWCompression"};
	if(app.documents.length > 0)
	{
	try{

		var docFileSpec = new File ( docTargetPath );
		var exportOptions = new ExportOptionsTIFF();
		var type = ExportType.TIFF;
		
		exportOptions.resolution = tiffProperties[0];
		exportOptions.antiAliazing = tiffProperties[1];
		exportOptions.IZWCompression = tiffProperties[2];
		
		docSource.exportFile(docFileSpec, type, exportOptions);
		
		
		return "Success";
		}
		catch(e)
		{
		// $.writeln("exrc " + e);
			return "Error: Tiff export failed - " + e;
		}
		
	}
}

