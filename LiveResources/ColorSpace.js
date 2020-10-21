#target illustrator  
   
var arrHidden = [];
var arrLock = [];
var arrVisible = [];
  var docRef = app.activeDocument;  
function outlineDocText(  ) {  
  
          if ( app.documents.length == 0 ) return;  
          recurseLayers( docRef.layers );  
    
};  
  
outlineDocText();   
ApplySwatch();

function ApplySwatch()
{
    
var colorToKill = 'PANTONE 583 C';
var colorToLive = 'PANTONE 143 C'; 

with (docRef) {  
    var replaceColor = swatches.getByName(colorToLive).color;  
    for (var i = 0; i < pathItems.length; i++) {  
    with (pathItems[i]) {  
        if (filled == true && fillColor instanceof SpotColor) {  
            if (fillColor.spot.name == colorToKill) fillColor = replaceColor;  
        }  
        if (stroked == true && strokeColor instanceof SpotColor) {  
            if (strokeColor.spot.name == colorToKill) strokeColor = replaceColor;  
            }  
        }  
    }  
      
    for (var j = 0; j < stories.length; j++) {  
        with (stories[j]) {  
            for (var k = 0; k < characters.length; k++) {  
                with (characters[k].characterAttributes) {  
                    if (fillColor instanceof SpotColor) {  
                        if (fillColor.spot.name == colorToKill) fillColor = replaceColor;  
                    }  
                    if (strokeColor instanceof SpotColor) {  
                        if (strokeColor.spot.name == colorToKill) strokeColor = replaceColor;  
                    }  
                }  
            }  
        }  
    }  
docRef.swatches.getByName(colorToKill).remove();  
} 

}


for ( var i = arrHidden.length-1; i >= 0; i-- )
{
    arrHidden[i].hidden = true;
    }
for ( var i = arrLock.length-1; i >= 0; i-- )
{
    arrLock[i].locked = true;
    }
for ( var i = arrVisible.length-1; i >= 0; i-- )
{
    arrVisible[i].visible = false;
    }

function recurseLayers( objArray ) {  
    
          for ( var i = 0; i < objArray.length; i++ ) {  
    
                    // Record previous value with conditional change  
                    var l = objArray[i].locked;  
                    if ( l )
                    {
                        arrLock.push(objArray[i]);
                        objArray[i].locked = false;
                     }
    
                    // Record previous value with conditional change  
                    var v = objArray[i].visible;  
                    if ( !v ) 
                    {
                        arrVisible.push(objArray[i]);
                        objArray[i].visible = true; 
                        }
    
                                         outlineText( objArray[i].pathItems);  
                     outlineText( objArray[i].placedItems);  
                     outlineText( objArray[i].textFrames);  
                     outlineText( objArray[i].compoundPathItems); 
    
                    // Recurse the contained layer collection  
                    if ( objArray[i].layers.length > 0 ) {  
                              recurseLayers( objArray[i].layers)  
                    }  
    
                   // Recurse the contained group collection  
                    if ( objArray[i].groupItems.length > 0 ) {  
                              recurseGroups( objArray[i].groupItems)  
                    }   
                    
    
                    // Return to previous values  
                //    objArray[i].locked = l;  
                 //   objArray[i].visible = v;  
          }  
};  
  
function recurseGroups( objArray ) {  
    
          for ( var i = 0; i < objArray.length; i++ ) {  
    
                    // Record previous value with conditional change  
                /*    var l = objArray[i].locked;  
                    if ( l ) objArray[i].locked = false;  
    
                    // Record previous value with conditional change  
                    var h = objArray[i].hidden;  
                    if ( h ) objArray[i].hidden = false;  
                    */
                    
                    var l = objArray[i].locked;  
                    if ( l ) 
                    {
                        arrLock.push(objArray[i]);
                    objArray[i].locked = false;  
                    }
    
                    // Record previous value with conditional change  
                    var h = objArray[i].hidden;  
                    if ( h ) 
                    {
                        arrHidden.push(objArray[i]);
                        objArray[i].hidden = false;  
                     }
    
    
              //      outlineText( objArray[i].textFrames);  
           //         outlineText( objArray[i]);  
                     outlineText( objArray[i].pathItems);  
                     outlineText( objArray[i].placedItems);  
                     outlineText( objArray[i].textFrames);  
                     outlineText( objArray[i].compoundPathItems); 
    
                    // Recurse the contained group collection  
                    if ( objArray[i].groupItems.length > 0 ) {  
                              recurseGroups( objArray[i].groupItems ) ;
                    }   
    
                    // Return to previous values  
             //       objArray[i].locked = l;  
             //       objArray[i].hidden = h;  
          }  
};  
  
  
function outlineText( objArray ) {  
    
          // Reverse this loop as it brakes the indexing  
          for ( var i = objArray.length-1; i >= 0; i-- ) {  
              
              
    
                    // Record previous value with conditional change  
                    var l = objArray[i].locked;  
                    if ( l ) 
                    {
                        arrLock.push(objArray[i]);
                    objArray[i].locked = false;  
                    }
    
                    // Record previous value with conditional change  
                    var h = objArray[i].hidden;  
                    if ( h ) 
                    {
                        arrHidden.push(objArray[i]);
                        objArray[i].hidden = false;  
                     }
    
          //          var g = objArray[i].createOutline(  );  
    
                    // Return new group to previous Text Frame values  
              //      g.locked = l;  
              //      g.hidden = h;  
    
          }  
  
};