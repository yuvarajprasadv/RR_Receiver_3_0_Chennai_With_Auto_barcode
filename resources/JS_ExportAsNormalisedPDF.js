
main(arguments);

function main(argv)
{      
    try
    {
        sourceDoc = app.activeDocument;
        //return SaveDocAsPDF(sourceDoc, argv[0], argv[1].slice());
        
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

 		if(pdfProperties[0].toString() === 'true')
        		scripter.embedImages  = true;    
        	else  if(pdfProperties[0].toString() === 'false')
        		scripter.embedImages  = false;  
        		
        	if(pdfProperties[1].toString() === 'true')
        		scripter.addPreview   = true;
        	else if(pdfProperties[1].toString() === 'false')
        		scripter.addPreview   = false;
        	
        	if(pdfProperties[2].toString() === 'true')
        		scripter.copyImages   = true;
        	else if(pdfProperties[2].toString() === 'false')
        		scripter.copyImages   = false;
        		
        	if(pdfProperties[3].toString() === 'true')
        		scripter.copyImagesNotOnServers = true;
        	else if(pdfProperties[3].toString() === 'false')
        		scripter.copyImagesNotOnServers = false;
        		
        	if(pdfProperties[4].toString() === 'true')
        		scripter.fitMediaBoxToArtwork = true;
        	else if(pdfProperties[4].toString() === 'false')
        		scripter.fitMediaBoxToArtwork = false;
        
        	if(pdfProperties[5].toString() === 'true')
        		scripter.expandPatterns = true;
        	else if(pdfProperties[5].toString() === 'false')
        		scripter.expandPatterns = false;
        
        	if(pdfProperties[6].toString() === 'true')
        		scripter.contourizeBitmaps = true;
        	else if(pdfProperties[6].toString() === 'false')
        		scripter.contourizeBitmaps = false;
        		
        	if(pdfProperties[7].toString() === 'true')
        		scripter.outlineText = true;
        	else if(pdfProperties[7].toString() === 'false')
        		scripter.outlineText = false;
        		
        	if(pdfProperties[8].toString() === 'true')
        		scripter.includeHiddenObjectsAndLayers = true;
        	else if(pdfProperties[8].toString() === 'false')
        		scripter.includeHiddenObjectsAndLayers = false;        		
      
        	if(pdfProperties[9].toString() === 'true')
        		scripter.includeNotes = true;
        	else if(pdfProperties[9].toString() === 'false')
        		scripter.includeNotes = false;         
        		
        	scripter.blendResolution = pdfProperties[10].valueOf();  // default 600
        	scripter.borderMode = pdfProperties[11].valueOf();  // default 3         	
        														// kBordersModeArtworkBoundingBox = 1,
                                             				//kBordersModeCurrentArtboard = 2,
                                             				//kBordersModeTrimBox = 3	 

        //scripter.exportHiddenObjects  = false;        // Obsolete
        
        
        var pdfFilePathToSave= pdfTargetPath + ".pdf";
        scripter.outputPath = pdfFilePathToSave;

        scripter.exportPDF();
            
        var ErrorCode      = scripter.errorCode;
        var ErrorMessage   = scripter.errorMessage;
        var AllMessages    = scripter.formatedExportMessage;
	//	$.writeln("\n\n ERRCODE:  " + ErrorCode + "\n\n ERRMESSAGE:  " + ErrorMessage + "\n\nALLMESSAGE:   " +  AllMessages );
        
        if(ErrorCode > 0)
            return "Normalised PDF failed:" + ErrorMessage;
        else
        		return 'Success Normalised PDF';
         
       }
       catch (e)
       {
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

