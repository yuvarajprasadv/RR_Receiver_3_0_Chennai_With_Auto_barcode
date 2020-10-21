
main(arguments);
function main(argv)
{      
        sourceDoc = app.activeDocument;
        ResetLayers(sourceDoc);
    //    SaveSourceDoc(sourceDoc);
    //    SaveDocAsPDF(sourceDoc, argv[0]);
    //    sourceDoc.close(SaveOptions.DONOTSAVECHANGES);  
    //    sourceDoc.close(SaveOptions.SAVECHANGES); 
  }


function SaveDocAsPDF (sourceDoc, pdfTargetPath)
{
    if ( app.documents.length > 0 ) 
    {
        var pdfFileName = new File ( pdfTargetPath );
        savePdfOpts = new PDFSaveOptions();
        savePdfOpts.optimization = true;
        savePdfOpts.compatibility = PDFCompatibility.ACROBAT5;
        sourceDoc.saveAs( pdfFileName, savePdfOpts );
    }
}

function SaveSourceDoc(docSource)
{
    docSource.save();
}

function ResetLayers(docSource)
{
	
 for (var eachTxt=0; eachTxt < docSource.textFrames.length; eachTxt++)
 {
	eachTextFrame = docSource.textFrames[eachTxt];
    var arr = (eachTextFrame.name).split("::");
    if(arr.length > 1)
    {
        eachTextFrame.name = arr[0];
    } 
  }
}