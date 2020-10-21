
var docRef = app.activeDocument;  


main(arguments);
function main(argv)
{ 
app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;

swatchObj =  docRef.swatches.getByName("White");

cmykColor = new CMYKColor();
cmykColor.black = 0;
cmykColor.cyan = 0;
cmykColor.magenta = 0;
cmykColor.yellow = 0;


var newSpot =  docRef.spots.getByName("White");
newSpot.color = cmykColor;

var newSpotColor = new SpotColor();
newSpotColor.spot = newSpot;
swatchObj.color = newSpotColor;


swatchObj1 =  docRef.swatches.getByName("Silver");
var newSpot =  docRef.spots.getByName("Silver");
newSpot.color = cmykColor;

var newSpotColor = new SpotColor();
newSpotColor.spot = newSpot;
swatchObj1.color = newSpotColor; 



outlineDocText();


var layerList = ["Heat Seal/Coatings", "Barcodes", "Varnish", "Legend"];

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
}





function outlineDocText(  ) {  
  
 if ( app.documents.length == 0 ) return;  

var docRef = app.activeDocument;  
recurseLayers( docRef.layers );  
    
};  
  
 
  
function recurseLayers( objArray ) {  
    
          for ( var i = 0; i < objArray.length; i++ ) {  
    
                    // Record previous value with conditional change  
                    var l = objArray[i].locked;  
                    if ( l ) objArray[i].locked = false;  
    
                    // Record previous value with conditional change  
                    var v = objArray[i].visible;  
                    if ( !v ) objArray[i].visible = true;  
    
                    outlineText( objArray[i].textFrames );  
    
                    // Recurse the contained layer collection  
                    if ( objArray[i].layers.length > 0 ) {  
                              recurseLayers( objArray[i].layers )  
                    }  
    
                    // Recurse the contained group collection  
                    if ( objArray[i].groupItems.length > 0 ) {  
                              recurseGroups( objArray[i].groupItems )  
                    }   
    
                    // Return to previous values  
                    objArray[i].locked = l;  
                    objArray[i].visible = v;  
          }  
};  
  
function recurseGroups( objArray ) {  
    
          for ( var i = 0; i < objArray.length; i++ ) {  
    
                    // Record previous value with conditional change  
                    var l = objArray[i].locked;  
                    if ( l ) objArray[i].locked = false;  
    
                    // Record previous value with conditional change  
                    var h = objArray[i].hidden;  
                    if ( h ) objArray[i].hidden = false;  
    
                    outlineText( objArray[i].textFrames );  
    
                    // Recurse the contained group collection  
                    if ( objArray[i].groupItems.length > 0 ) {  
                              recurseGroups( objArray[i].groupItems )  
                    }   
    
                    // Return to previous values  
                    objArray[i].locked = l;  
                    objArray[i].hidden = h;  
          }  
};  
  
  
function outlineText( objArray ) {  
    
          // Reverse this loop as it brakes the indexing  
          for ( var i = objArray.length-1; i >= 0; i-- ) {  
    
                    // Record previous value with conditional change  
                    var l = objArray[i].locked;  
                    if ( l ) objArray[i].locked = false;  
    
                    // Record previous value with conditional change  
                    var h = objArray[i].hidden;  
                    if ( h ) objArray[i].hidden = false;  
    
                    var g = objArray[i].createOutline();  
    
                    // Return new group to previous Text Frame values  
                    g.locked = l;  
                    g.hidden = h;  
    
          }  
  
};  