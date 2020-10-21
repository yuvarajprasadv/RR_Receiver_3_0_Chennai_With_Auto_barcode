
main(arguments);

function main(argv)
{      
    try
    {
    		app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;
        sourceDoc = app.activeDocument;
        return ExportDocumentAsGIF(sourceDoc, argv[0], argv[1].slice());
        
    }catch(exception){
        var errorString;
        errorString = "PDF Export failed. Document is not active";
        return errorString;
    }
}

function ExportDocumentAsGIF(docSource, docTargetPath, gifProperties)
{
	//gifProperties = {"antiAliasing", "artBoardClipping", "horizontalScale", "verticalScale", "transparency", "saveAsHTML"};
	if(app.documents.length > 0)
	{
		var docFileSpec = new File ( docTargetPath );
		var exportOptions = new ExportOptionsGIF();
		var type = ExportType.GIF;

		exportOptions.antiAliasing = gifProperties[0];
		exportOptions.artBoardClipping = gifProperties[1];
		exportOptions.horizontalScale = gifProperties[2];
		exportOptions.verticalScale = gifProperties[3];
		exportOptions.transparency = gifProperties[4];
		exportOptions.saveAsHTML = gifProperties[5];
		
		docSource.exportFile(docFileSpec, type, exportOptions);
		
	}
}

