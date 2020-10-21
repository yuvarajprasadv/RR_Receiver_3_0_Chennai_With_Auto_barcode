
main(arguments);

function main(argv)
{      
    try
    {
    app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;
        sourceDoc = app.activeDocument;
     //   $.writeln("IN");
        return SaveNormalizedPDF(sourceDoc, argv[0], argv[1].slice(), argv[2]);
        
    }catch(exception){
        var errorString;
        errorString = "PDF Export failed. Document is not active";
        return errorString;
    }
}


function SaveNormalizedPDF(sourceDoc, pdfTargetPath, pdfProperties, illustratorVersion)
{
	try
	{
		var externalSearchFolder = ExternalObject.searchFolder;
		var eskoPluginFolderPath = "/Applications/Adobe Illustrator "+ illustratorVersion +"/Plug-ins.localized/Esko/Data Exchange/PDF Export;" + externalSearchFolder;
		
        ExternalObject.searchFolders=eskoPluginFolderPath;
        var dw;
        if(illustratorVersion == "CS6")
        		dw = new ExternalObject("lib:PDFExport_MAI16r.aip");
        	else if(illustratorVersion == "CC")
        		dw = new ExternalObject("lib:PDFExport_MAI17r.aip");
        	else if(illustratorVersion == "CC 2014")
        		dw = new ExternalObject("lib:PDFExport_MAI18r.aip");
        	else if(illustratorVersion == "CC 2015")
        		dw = new ExternalObject("lib:PDFExport_MAI20r.aip");
        else if(illustratorVersion == "CC 2015.3")
        		dw = new ExternalObject("lib:PDFExport_MAI20r.aip");
        	else if(illustratorVersion == "CC 2017")
        		dw = new ExternalObject("lib:PDFExport_MAI21r.aip");
        	else if(illustratorVersion == "CC 2018")
        		dw = new ExternalObject("lib:PDFExport_MAI22r.aip");
        else if(illustratorVersion == "CC 2019")
        		dw = new ExternalObject("lib:PDFExport_MAI23r.aip");
        	

        var scripter = new NormalizedPDFExport();

        	scripter.embedImages = pdfProperties[0];
        	scripter.addPreview   = pdfProperties[1];
    		scripter.copyImages   = pdfProperties[2];
    		scripter.copyImagesNotOnServers = pdfProperties[3];
    		scripter.fitMediaBoxToArtwork = pdfProperties[4];
    		scripter.expandPatterns = pdfProperties[5];
    		scripter.contourizeBitmaps = pdfProperties[6];
    		scripter.outlineText = pdfProperties[7];
    		scripter.includeHiddenObjectsAndLayers = pdfProperties[8];
    		scripter.includeNotes = pdfProperties[9];
        	scripter.blendResolution = pdfProperties[10];  // default 600
        	scripter.borderMode = pdfProperties[11];  // default 3         	
        														// kBordersModeArtworkBoundingBox = 1,
                                             				//kBordersModeCurrentArtboard = 2,
                                             				//kBordersModeTrimBox = 3	 

        //scripter.exportHiddenObjects  = false;        // Obsolete
        
        
        var pdfFilePathToSave= pdfTargetPath + "_Normalized.pdf";
        scripter.outputPath = pdfFilePathToSave;

        scripter.exportPDF();
            
        var ErrorCode      = scripter.errorCode;
        var ErrorMessage   = scripter.errorMessage;
        var AllMessages    = scripter.formatedExportMessage;
	//	$.writeln("\n\n ERRCODE:  " + ErrorCode + "\n\n ERRMESSAGE:  " + ErrorMessage + "\n\nALLMESSAGE:   " +  AllMessages );
        
        if(ErrorCode > 0)
            return "Normalized PDF failed:" + ErrorMessage;
        else
        		return 'Success Normalized PDF';
         
       }
       catch (e)
       {
    //   $.writeln(e);
       	return "Normalised PDF failed: " + e.description;
       }
       
}





function SaveDocAsPDF (sourceDoc, pdfTargetPath, pdfProperties)
{
	//String[] pdfProperties = {"acrobatLayers", "optimization"};
	
    if ( app.documents.length > 0 ) 
    {
        var pdfFileName = new File ( pdfTargetPath );
        savePdfOpts = new PDFSaveOptions();
        
        
        savePdfOpts.compatibility = PDFCompatibility.ACROBAT6;
        
        if(pdfProperties[0].toString() === 'true')
        {
        		savePdfOpts.acrobatLayers = true;
        	}
        	else if (pdfProperties[0].toString() == 'false')
        		savePdfOpts.acrobatLayers = false;
        
        if(pdfProperties[1].toString() === "true")
        		savePdfOpts.optimization = true;
        	else if (pdfProperties[1].toString() === "false")
        		savePdfOpts.optimization = false;
        
        sourceDoc.saveAs(pdfFileName, savePdfOpts );
        return 'Success Normal PDF';
    }
    else
    {
        var errorString;
        errorString = "PDF Export failed. Document is not loaded";
        return errorString;
    }
}

