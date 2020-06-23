
var docRef = app.activeDocument;  


main(arguments);
function main(argv)
{ 


var layerListMakeVisible = ["Dimensions", "Dimension", "Cutter", "Print Marks", "Barcode", "Barcodes"];
var layerList = ["Legend"];


for(var i = 0; i < layerListMakeVisible.length; i++)
{
	try
	{

		layerObj = app.activeDocument.layers.getByName(layerListMakeVisible[i]);
		layerObj.visible = true;
	

	}
	catch(e)
	{
		errString = "Layer visiblity set false failed: " + e.description;
	}
}

for(var i = 0; i < layerList.length; i++)
{
	try
	{

		layerObj = app.activeDocument.layers.getByName(layerList[i]);
		layerObj.visible = false;
	

	}
	catch(e)
	{
		errString = "Layer visiblity set false failed: " + e.description;
	}
}

for(var j = 0; j < app.activeDocument.layers.length; j++)
{
	try
	{
		layerObj  = app.activeDocument.layers[j];
		var layerName = layerObj.name;
		if(layerName.lastIndexOf(".ARD") > 0)
		{
			layerObj.visible = false;
			break;
		}
		else if(layerName.lastIndexOf(".ard") > 0)
		{
			layerObj.visible = false;
			break;
		}
	}
	catch(e)
	{
		errString = "Layer visiblity set false failed: " + e.description;
	}
}


 return true;
};  