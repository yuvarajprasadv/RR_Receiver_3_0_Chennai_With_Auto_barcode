main(arguments);function main(argv){              sourceDoc = app.activeDocument;        sourceDoc.cropStyle = CropOptions.Standard;            var bounds = new Array(4);        bounds[0] = 26;    bounds[1] = 97;    bounds[2] = 127;    bounds[3] = 177;        sourceDoc.cropBox = bounds;        ExportDocumentAsJPEG(docSource, argv[0]);  }function ExportDocumentAsJPEG(docSource, docTargetPath){	if(app.documents.length > 0)	{		var docFileSpec = new File ( docTargetPath );		var exportOptions = new ExportOptionsJPEG();		var type = ExportType.JPEG;		exportOptions.antiAliasing = false;		exportOptions.qualitysetting = 35;		exportOptions.optimization = true;		exportOptions.artBoardClipping = false;		docSource.exportFile(docFileSpec, type, exportOptions);		alert ( "Crop box set" );			}}