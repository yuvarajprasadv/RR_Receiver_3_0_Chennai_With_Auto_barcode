
main(arguments);

function main(argv)
{      
    try
    {
  //  $.writeln( argv[1]);
        	app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;
        sourceDoc = app.activeDocument;
        return ExportDocumentAsPNG(sourceDoc, argv[0], argv[1].slice());
 
    }catch(exception){
  //  $.writeln(exception);
        var errorString;
        errorString = "PDF Export failed. Document is not active";
        return errorString;
    }
}

function ExportDocumentAsPNG(docSource, docTargetPath, pdfProperties)
{
	//png24Properties = {"type", "antiAliasing", "artBoardClipping", "horizontalScale", "verticalScale", "transparency", "saveAsHTML"};
	if(app.documents.length > 0)
	{
		var docFileSpec = new File ( docTargetPath );
		var exportOptions = new ExportOptionsPNG24();
		var type = ExportType.PNG24;
		
		if(pdfProperties[0] == 24)
		{
			type = ExportType.PNG24;
			exportOptions = new ExportOptionsPNG24();
		}
		else if(pdfProperties[0] == 8)
		{
			type = ExportType.PNG8;
			exportOptions = new ExportOptionsPNG8();
		}
		else
		{
			type = ExportType.PNG24;
			exportOptions = new ExportOptionsPNG24();
		}
			
		
    		exportOptions.antiAliasing = pdfProperties[1];
    		exportOptions.artBoardClipping = pdfProperties[2];
        	exportOptions.horizontalScale = pdfProperties[3];
        	exportOptions.verticalScale = pdfProperties[4];
    		exportOptions.transparency = pdfProperties[5];
    		exportOptions.saveAsHTML = pdfProperties[6];
        		
		docSource.exportFile(docFileSpec, type, exportOptions);
		
	}
}
