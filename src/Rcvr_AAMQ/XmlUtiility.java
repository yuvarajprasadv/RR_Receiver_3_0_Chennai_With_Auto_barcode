package Rcvr_AAMQ;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;
import org.apache.log4j.Logger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import Rcvr_AAMQ.Utils;

import org.w3c.dom.*;

public class XmlUtiility
{
	static Logger log = LogMQ.monitor("Rcvr_AAMQ.XmlUtiility");
		
	 public static boolean validateXMLSchema(String xsdPath, String xmlPath) throws Exception
	 {
	      try {
	         SchemaFactory factory = 
	            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	         Schema schema = factory.newSchema(new File(xsdPath));
	            Validator validator = schema.newValidator();
	            validator.validate(new StreamSource(new File(xmlPath)));
	      } catch (IOException ioEx){    
	    	  log.error(ioEx.getMessage());
	    	  throw ioEx;
	      }catch(SAXException saxEx){
	    	  log.error(saxEx.getMessage());
	    	  throw saxEx;
	      }
	      return true;
	   }
	 
	   public static Document parseXmlFile(String filePath) throws Exception{
				//get the factory
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

				try {

					//Using factory get an instance of document builder
					DocumentBuilder db = dbf.newDocumentBuilder();

					//parse using builder to get DOM representation of the XML file
					Document dom = db.parse(filePath);
					return dom;

				}catch(Exception Ex) {
					log.error(Ex.getMessage());
					throw Ex;
				}
			}
	   
	   public static Boolean IsValidXML(String filePath) throws Exception
	   {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			FileSystem fls = new FileSystem();
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				db.parse(filePath);
				return true;

			}
			catch(java.io.FileNotFoundException Ex)
			{
				log.error(Ex.getMessage());
				MessageQueue.ERROR += "\n XML File or file path not found: "+ filePath;
				fls.AppendFileString("\nXML File or file path not found :"+ filePath.toString()+"\n");
				ThrowException.CustomExit(Ex, "File or File path Invalid");
				throw Ex;
			}
			catch(org.xml.sax.SAXParseException Ex)
			{
				log.error(Ex.getMessage());
				MessageQueue.ERROR += "\n Invalid XML: "+ filePath;
				fls.AppendFileString("\nInvalid XML file path: " + filePath.toString()+"\n");
				ThrowException.CustomExit(Ex, "Invalid xml");
				throw Ex;
			}
			catch(Exception Ex) 
			{
				log.error(Ex.getMessage());
				MessageQueue.ERROR += "\n Invalid XML: "+ filePath;
				fls.AppendFileString("\nInvalid XML file: "+ filePath.toString()+"\n");
				ThrowException.CustomExit(Ex, "Invalid file");
				throw Ex;
			} 
	   }
	   
	   public static Boolean multipleJobIsValidXml(String filePath) throws Exception
	   {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			FileSystem fls = new FileSystem();
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				db.parse(filePath);
				return true;

			}
			catch(java.io.FileNotFoundException Ex)
			{
				log.error(Ex.getMessage());
				MessageQueue.ERROR += "\n XML File or file path not found: "+ filePath;
				fls.AppendFileString("\nXML File or file path not found :"+ filePath.toString()+"\n");
			}
			catch(org.xml.sax.SAXParseException Ex)
			{
				log.error(Ex.getMessage());
				MessageQueue.ERROR += "\n Invalid XML: " + Ex.getMessage() + " ---" + filePath;
				fls.AppendFileString("\nInvalid XML file path: " + filePath.toString()+"\n");
			}
			catch(Exception Ex) 
			{
				log.error(Ex.getMessage());
				MessageQueue.ERROR += "\n Invalid XML: "+ filePath;
				fls.AppendFileString("\nInvalid XML file: "+ filePath.toString()+"\n");
			} 
			return false;
	   }
	   
	   public String parsePrivateElement(Document dom, String privateElmTypeCode)
	   {
		   try{
			   String[] styleArr = new String[92];
			   String privateElements = "";
			   int inc = 0;
			   NodeList ld = dom.getElementsByTagName("privateElements").item(0).getChildNodes();
			   for (int tmp=1; tmp < ld.getLength(); tmp++)
			   {
				   Node nd = ld.item(tmp);
				   if(nd.getNodeType() == 1)
				   {
					  NodeList cld = nd.getChildNodes();
					  String cpyElement = "";
					  for(int eachChild=1; eachChild< cld.getLength(); eachChild++)
					  {
						  Node cnd = cld.item(eachChild);
						  if (cnd.getNodeType()==1)
						  {
							  if (cnd.getNodeName() == "privateElementTypeCode")
								  cpyElement = cnd.getFirstChild().getNodeValue();
							  else if(cnd.getNodeName() == "instanceSequence")
								  cpyElement += "-" + cnd.getFirstChild().getNodeValue();// + "...1";
							  else if(cnd.getNodeName() == "localeSequence")
							  {
								  String[] arrStr = cpyElement.split("-");
							  	  cpyElement =  arrStr[0] +  cnd.getFirstChild().getNodeValue() + "-" + arrStr[1];
							  }
							//  System.out.println("copy elem : " + cpyElement + "\n");
						  if(cpyElement.equals(privateElmTypeCode))
						  {
							  if(cnd.getNodeType()==1 && cnd.getChildNodes().getLength() > 1)
							  {
								  NodeList bdCldList = cnd.getChildNodes().item(1).getChildNodes().item(1).getChildNodes();
								  for (int bdClCnt=0; bdClCnt < bdCldList.getLength(); bdClCnt++)
								 //  for (int bdClCnt=0; bdClCnt <4; bdClCnt++)
								   {
									   Node bdChild = bdCldList.item(bdClCnt);
									   if(bdChild.getNodeType() == 1)
									   {
										   styleArr[inc] = bdChild.getNodeName();
										   privateElements = privateElements + styleArr[inc];
										   
										   if (bdChild.hasChildNodes())
										   {
											   inc += 1;
											   styleArr[inc] = bdChild.getFirstChild().getNodeValue();
											   privateElements += ":" + styleArr[inc] + ",";
											   inc += 1;
										   }
										   else
										   {
											   inc += 1;
											   styleArr[inc] = "None";
											   privateElements += ":" + styleArr[inc] + ",";
											   inc += 1;
										   }
										   
										   
									   }
									   
								   }
								  return (privateElements);
							  }
						  }
	
					  } 
	
				   }
				 }
			   }
			    
	      } catch (Exception ex) {
	         
	    	  log.error("No private elements " + ex.getMessage());
	    	  throw ex;
	    	
	      }
		//   System.out.println("gs1 parser");
		return null;
	   }
	   
	   public void WriteFile(String prvString)
	   {
		   Utils utl = new Utils();
		   
		   List<String> lines = Arrays.asList(prvString);
		   Path file = Paths.get(utl.ConvertToAbsolutePath("/Applications/Adobe Illustrator "+MessageQueue.VERSION+"/Plug-ins.localized/Sgk/Configuration/PrivateElements.txt"));
		   try {
			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
	   
	   
	   public void WriteFont(String prvString)
	   {
		   Utils utl = new Utils();
		   
		   List<String> lines = Arrays.asList(prvString);
		   Path file = Paths.get(utl.ConvertToAbsolutePath("/Applications/Adobe Illustrator "+MessageQueue.VERSION+"/Plug-ins.localized/Sgk/Configuration/PrivateElementFont.txt"));
		   try {
			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
	   
	   
	   public void SplitBy(String elementString)
	   {
		   String[] pvlElement;
		   String[] stringNewLine;
		   String otherThanFont = "";
		   String fontString = "";
		   stringNewLine = elementString.split("\n");
		   int j = 0;
		   while(j < stringNewLine.length)
		   {
			   pvlElement = stringNewLine[j].split(":");
			   for(int i=0; i<pvlElement.length; i++)
			   {
				   if(i!=1)
				   {
					   otherThanFont +=pvlElement[i].trim() + " ";
				   }
				   else
				   {
					   fontString += pvlElement[i].trim() + "\n";
				   }
			   }
			   otherThanFont +="\n";
		   j++;
		   }
		   WriteFile(otherThanFont.trim());
		   WriteFont(fontString.trim());
		  
	   }
	   
	   public String parsePrivateAllElement(Document dom, String privateElmTypeCode)
	   {
		   try{
			   String privateElements = "";
			   int inc = 0;
			   NodeList ld = dom.getElementsByTagName("privateElements").item(0).getChildNodes();
			   for (int tmp=1; tmp < ld.getLength(); tmp++)
			   {
				   Node nd = ld.item(tmp);
				   if(nd.getNodeType() == 1)
				   {
					  NodeList cld = nd.getChildNodes();
					  String cpyElement = "";
					  for(int eachChild=1; eachChild< cld.getLength(); eachChild++)
					  {
						  Node cnd = cld.item(eachChild);
						  if (cnd.getNodeType()==1)
						  {
							  if (cnd.getNodeName() == "privateElementTypeCode")
								  cpyElement = cnd.getFirstChild().getNodeValue();
							  else if(cnd.getNodeName() == "instanceSequence")
								  cpyElement += "-" + cnd.getFirstChild().getNodeValue() + "...1";
							  else if(cnd.getNodeName() == "localeSequence")
							  {
								  String[] arrStr = cpyElement.split("-");
							  	  cpyElement =  arrStr[0] +  cnd.getFirstChild().getNodeValue() + "-" + arrStr[1];
							  }
							 
						  if(cpyElement.equalsIgnoreCase(privateElmTypeCode))
						  {
							  if(cnd.getNodeType()==1 && cnd.getChildNodes().getLength() > 1)
							  {
								  NodeList bdCldList = cnd.getChildNodes().item(1).getChildNodes().item(1).getChildNodes();
								  
								  for (int bdClCnt=0; bdClCnt < bdCldList.getLength(); bdClCnt++)
								   {
									   Node bdChild = bdCldList.item(bdClCnt);
									   if((bdChild.getNodeName()).matches("Font|Sizemin|SizeIteration|Sizemax|Leadingmin|LeadingIteration|Leadingmax|HorizontalScale|verticalScale|TextFit"))
									   if(bdChild.getNodeType() == 1)
									   {
										   if (bdChild.hasChildNodes())
										   {
											   inc += 1;
											   privateElements += ":" + bdChild.getFirstChild().getNodeValue().trim();
											   inc += 1;
										   }
										   else
										   {
											   inc += 1;
											   privateElements += ":" + "None";
											   inc += 1;
										   } 
									   }
								   }
								  return (privateElements);
							  }
						  }
	
					  } 
	
				   }
				 }
			   }
			    
	      } catch (Exception ex) {
	         
	    	  log.error("No private elements " + ex.getMessage());
	    	  throw ex;
	    	
	      }
		return (null);
	   }

	   
	   public String GS1XmlParseAllElement(String xmlFilePath) throws Exception
	   {
		   XmlUtiility xmlUtl = new XmlUtiility();
			   try{
				   String prvElementsString= "";
				   Document dom = parseXmlFile(xmlFilePath);
				   String privateElements=""; 
				   
				   NodeList ldd= dom.getElementsByTagName("artworkContentPieceOfArt").item(0).getChildNodes();
				   for (int tmp=1; tmp < ldd.getLength(); tmp++)
				   {
					   
					   Node nds = ldd.item(tmp);
					   if(nds.getNodeType() == 1 && nds.getNodeName() == "artworkContentCopyElement")
					   { 
						   for(int cpyElInc=1; cpyElInc < nds.getChildNodes().getLength(); cpyElInc++)
						   {
							   String linkID = "";
							   String orgLinkID = "";
							   if(nds.getChildNodes().item(cpyElInc).getNodeName() == "copyElementTypeCode")
							   {
								   orgLinkID =  nds.getChildNodes().item(cpyElInc).getFirstChild().getTextContent();
								   linkID = (nds.getChildNodes().item(cpyElInc).getFirstChild().getTextContent()) + "_RULES";
								   int tmp1 = cpyElInc + 4;

								   if(nds.getChildNodes().item(tmp1).getNodeName() == "localeSequence") 
								   {
									   orgLinkID+=(nds.getChildNodes().item(tmp1).getFirstChild().getTextContent());
									   linkID+=(nds.getChildNodes().item(tmp1).getFirstChild().getTextContent());
								   }
								   if(nds.getChildNodes().item(tmp1-2).getNodeName() == "instanceSequence")
								   {
									   orgLinkID+=("-"+(nds.getChildNodes().item(tmp1-2).getFirstChild().getTextContent())+"...1");
								   	   linkID+=("-"+(nds.getChildNodes().item(tmp1-2).getFirstChild().getTextContent())+"...1");
								   }
								 // if(pvEl.equals(linkID))
								   {
									   privateElements = xmlUtl.parsePrivateAllElement(dom, linkID);
									   if(privateElements != null)
									   {
										   
										   prvElementsString += orgLinkID+privateElements+"\n";
									   }
									  // return privateElements;	
								   }
							   }
							 
							   
						   }
						  
					
					   }
					 
					   
				   }   

				   SplitBy(prvElementsString);
				   return privateElements;
			   }  
			   catch (Exception ex)
			   {
				   System.out.println("err " + ex.getMessage());
				   log.error(ex.getMessage());  
				   throw ex;
			   }
		  // return null;
	   }
	   
	   
	 
	   
	   public String GS1XmlParseElement(String xmlFilePath, String privateElementFromAI) throws Exception
	   {
		   XmlUtiility xmlUtl = new XmlUtiility();
			   try{
				   Document dom = parseXmlFile(xmlFilePath);
				   String privateElements="";
				   NodeList ldd= dom.getElementsByTagName("artworkContentPieceOfArt").item(0).getChildNodes();
				   for (int tmp=1; tmp < ldd.getLength(); tmp++)
				   {
					 String linkID = "";
					   Node nds = ldd.item(tmp);
					   if(nds.getNodeType() == 1 && nds.getNodeName() == "artworkContentCopyElement")
					   { 
						   for(int cpyElInc=1; cpyElInc < nds.getChildNodes().getLength(); cpyElInc++)
						   {
							  
							   if(nds.getChildNodes().item(cpyElInc).getNodeName() == "copyElementTypeCode")
							   {
								   linkID = nds.getChildNodes().item(cpyElInc).getFirstChild().getTextContent();
								   int tmp1 = cpyElInc + 4;

								   if(nds.getChildNodes().item(tmp1).getNodeName() == "localeSequence") 
									   linkID+=(nds.getChildNodes().item(tmp1).getFirstChild().getTextContent());
								   if(nds.getChildNodes().item(tmp1-2).getNodeName() == "instanceSequence")
									   linkID+=("-"+(nds.getChildNodes().item(tmp1-2).getFirstChild().getTextContent()));

								   String pvEl = (privateElementFromAI.split("::"))[1].toString();
								 // if(pvEl.equals(linkID))
								   {
									   privateElements += xmlUtl.parsePrivateElement(dom, linkID);
									  // return privateElements;	
								   }
							   } 
						   }
					   }
					   return privateElements;
					   
				   }   
				
			   }  
			   catch (Exception ex)
			   {
				   System.out.println("err " + ex.getMessage());
				   log.error(ex.getMessage());  
				   throw ex;
			   }

		   return null;
	   }
	   public static URI GetFileFromPath(String filePath) throws URISyntaxException
	   {
		   
		   int index = filePath.lastIndexOf("/");
		   filePath = filePath.substring(0, index);
		   
		   File file = new File(new URI(filePath));
		   File[] listOfFiles = file.listFiles();
		   
		   for(int i=0; i < listOfFiles.length; i++ )
		   {
			   if(listOfFiles[i].toString().endsWith(".pdf")) //changed from .eps to .pdf
				   return listOfFiles[i].toURI();
		   }
		   
		   return null;
	   }
	   
	   public static URI GetFileFromPathString(String filePath) throws URISyntaxException
	   {

		   filePath = "file:///" + filePath;
		   File file = new File(new URI(filePath.replace(" ", "%20")));
		   File[] listOfFiles = file.listFiles();
		   
		   for(int i=0; i < listOfFiles.length; i++ )
		   {
			   if(listOfFiles[i].toString().endsWith(".pdf")) //changed from .eps to .pdf
			   {
				   return listOfFiles[i].toURI();
			   }
		   }
		   
		   return null;
	   }

	   
	   public static String GS1XmlAppendGraphicsElement(String xmlFilePath, String barcodeUriString) throws Exception
	   {
		   XmlUtiility xmlUtl = new XmlUtiility();
		   try{
			   
				  barcodeUriString = barcodeUriString.replace("file:/", "file:////");
				  barcodeUriString = barcodeUriString.replace("%20", " ");
			   
			   
			   Document dom = parseXmlFile(xmlFilePath);

			   
			   NodeList ldd= dom.getElementsByTagName("artworkContentPieceOfArt");
			   Node artworkContentPieceOfArt = ldd.item(0);
			   
			   Element artworkContentGraphicElement = dom.createElement("artworkContentGraphicElement");
			   artworkContentPieceOfArt.appendChild(artworkContentGraphicElement);
			   
			   Element graphicElementTypeCode = dom.createElement("graphicElementTypeCode");
			   graphicElementTypeCode.appendChild(dom.createTextNode("ESG-barcodeimages-BA-0001"));
			   artworkContentGraphicElement.appendChild(graphicElementTypeCode);
			   
			   Element instanceSequence = dom.createElement("instanceSequence");
			   instanceSequence.appendChild(dom.createTextNode("1"));
			   artworkContentGraphicElement.appendChild(instanceSequence);
			   
			   Element localeSequence = dom.createElement("localeSequence");
			   localeSequence.appendChild(dom.createTextNode("1"));
			   artworkContentGraphicElement.appendChild(localeSequence);
			   
			   Element isContentApproved = dom.createElement("isContentApproved");
			   isContentApproved.appendChild(dom.createTextNode("TRUE"));
			   artworkContentGraphicElement.appendChild(isContentApproved);
			   
			   Element forPlacementOnly = dom.createElement("forPlacementOnly");
			   forPlacementOnly.appendChild(dom.createTextNode("FALSE"));
			   artworkContentGraphicElement.appendChild(forPlacementOnly);
			   
			   Element graphicElementDescription = dom.createElement("graphicElementDescription");
			   graphicElementDescription.setAttribute("languageCode", "en");
			   graphicElementDescription.appendChild(dom.createTextNode(barcodeUriString));
			   artworkContentGraphicElement.appendChild(graphicElementDescription);
			   
			   Element optionSequence = dom.createElement("optionSequence");
			   optionSequence.appendChild(dom.createTextNode("1"));
			   artworkContentGraphicElement.appendChild(optionSequence);
			   
			   Element prioritySequence = dom.createElement("prioritySequence");
			   prioritySequence.appendChild(dom.createTextNode("1"));
			   artworkContentGraphicElement.appendChild(prioritySequence);
			   
			   Element sourceReference = dom.createElement("sourceReference");
			   artworkContentGraphicElement.appendChild(sourceReference);
			   
			   Element referenceURI = dom.createElement("referenceURI");
			   referenceURI.appendChild(dom.createTextNode(barcodeUriString));
			   sourceReference.appendChild(referenceURI);
			   
			   Element versionIdentifier = dom.createElement("versionIdentifier");
			   versionIdentifier.appendChild(dom.createTextNode("1"));
			   sourceReference.appendChild(versionIdentifier);
			   

			   
			   
			   // write the DOM object to the file
	            TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            
	            Transformer transformer = transformerFactory.newTransformer();
	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	          //  transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	            DOMSource domSource = new DOMSource(dom);
	 
	            StreamResult streamResult = new StreamResult(new File(xmlFilePath));
	            transformer.transform(domSource, streamResult);
		   }
		   catch (Exception ex)
		   {
			   
		   }
		   
		   
		   
		   
		return "";
	   }
	   
	   
	   public static boolean CheckGraphicsElementExist(String xmlFilePath)
	   {
		   XmlUtiility xmlUtl = new XmlUtiility();
		   try{
			   Document dom = parseXmlFile(xmlFilePath);
			   
			   NodeList artworkContentGraphicElement = dom.getElementsByTagName("artworkContentGraphicElement");
			   if (artworkContentGraphicElement.getLength() > 0)
				   return true;
			   }
			   catch (Exception ex)
			   {
				   System.out.println("err " + ex.getMessage());
				   log.error(ex.getMessage());  
			   }
		   return false;
	   }
	   
	   
	   
	   public static String GS1XmlParseGraphicsElement(String xmlFilePath,  String barcodeUriString) throws Exception
	   {
		   XmlUtiility xmlUtl = new XmlUtiility();
			   try{
				   Document dom = parseXmlFile(xmlFilePath);
				   String privateElements="";
				   String lSequence="";
				   String iSequence="";
				   String pSequence="";
				   String oSequence="";
				   String graphicElmDesc = "";
				   String valueToChange = "";
				   
				   NodeList ldd= dom.getElementsByTagName("artworkContentPieceOfArt").item(0).getChildNodes();
				   for (int tmp=1; tmp < ldd.getLength(); tmp++)
				   {
					   Node nds = ldd.item(tmp);
					   if(nds.getNodeType() == 1 && nds.getNodeName() == "artworkContentGraphicElement")
					   { 
						   int graphicDecInt = 0;
						   int sourceReferenceInt = 0;
						   for(int cpyElInc=1; cpyElInc < nds.getChildNodes().getLength()-2; cpyElInc++)
						   {
							   if(nds.getChildNodes().item(1).getNodeName() == "graphicElementTypeCode")
							   {
								   int tmpInc = cpyElInc + 2;
								   if(nds.getChildNodes().item(tmpInc).getNodeName() == "instanceSequence")
								   {
									   iSequence = (nds.getChildNodes().item(tmpInc).getFirstChild().getTextContent());
									   continue;
								   }
								   if(nds.getChildNodes().item(tmpInc).getNodeName() == "localeSequence") 
								   {
									   lSequence = (nds.getChildNodes().item(tmpInc).getFirstChild().getTextContent());
									   continue;
								   }
								   if(nds.getChildNodes().item(tmpInc).getNodeName() == "prioritySequence") 
								   {
									   pSequence = (nds.getChildNodes().item(tmpInc).getFirstChild().getTextContent());
									   continue;
								   }
								   if(nds.getChildNodes().item(tmpInc).getNodeName() == "optionSequence")
								   {
									   oSequence = (nds.getChildNodes().item(tmpInc).getFirstChild().getTextContent());
									   continue;
								   }
								   
								   if(nds.getChildNodes().item(tmpInc).getNodeName() == "graphicElementDescription")
								   {
									   
									   graphicElmDesc = (nds.getChildNodes().item(tmpInc).getFirstChild().getTextContent());
									   graphicDecInt = tmpInc;
									   continue;
								   }
								   
								   if(nds.getChildNodes().item(tmpInc).getNodeName() == "sourceReference")
								   {
									   sourceReferenceInt = tmpInc;
									   Node srNode = nds.getChildNodes().item(tmpInc);
									   continue;
								   }

							   } 
							   
							   if(nds.getChildNodes().getLength()-3 == cpyElInc)
							   {
								   if(lSequence.equals("1") && iSequence.equals("1") && oSequence.equals("1") && pSequence.equals("1") )
								   {
									   
									   barcodeUriString = barcodeUriString.replace("file:/", "file:////");
									   valueToChange = barcodeUriString.replace("%20", " ");
									   
									//   valueToChange = GetFileFromPath(graphicElmDesc).toString();
									//   valueToChange = valueToChange.replace("file:/", "file:////");
									//   valueToChange = valueToChange.replace("%20", " ");

									   Node graphicsElm = nds.getChildNodes().item(graphicDecInt).getFirstChild();
									   graphicsElm.setTextContent(valueToChange);
									   
									   Node srNode = nds.getChildNodes().item(sourceReferenceInt);
									   Node srefNode = srNode.getChildNodes().item(1).getFirstChild();
									   srefNode.setTextContent(valueToChange);
									   
									   // write the DOM object to the file
							            TransformerFactory transformerFactory = TransformerFactory.newInstance();
							 
							            Transformer transformer = transformerFactory.newTransformer();
							            DOMSource domSource = new DOMSource(dom);
							 
							            StreamResult streamResult = new StreamResult(new File(xmlFilePath));
							            transformer.transform(domSource, streamResult);
							            

							 
								   }
							   }
							   
						   }
					   } 
				   }   
				
			   }  
			   catch (Exception ex)
			   {
				   System.out.println("err " + ex.getMessage());
				   log.error(ex.getMessage());  
				   throw ex;
			   }

		   return null;
	   }
	   
	   
	   public String  getFileNameFromElement(String xmlFilePath)
	   {
		   try
		   {
			   Document dom = parseXmlFile(xmlFilePath);
			   NodeList nodePrivateList = dom.getElementsByTagName("privateElementTypeCode");
			   for(int eachNode = nodePrivateList.getLength() - 1; eachNode >= 0; eachNode--)
			   {
				   Node ldd = nodePrivateList.item(eachNode);
				   if(ldd.getTextContent().equalsIgnoreCase("FILE_NAME"))
				   {
					   Node nxtSibling = ldd.getNextSibling();
					   while (nxtSibling != null)
					   {
						   if(nxtSibling.getNodeName().equalsIgnoreCase("textContent"))
							   return nxtSibling.getTextContent().trim();
						   nxtSibling = nxtSibling.getNextSibling();
					   } 
				   }
			   }
		   }
		   catch (Exception ex)
		   {
			   MessageQueue.ERROR += "Issue on getting file name to save from xml \n";
		   }
		return null;
	   }
	   
		 public static void main(String[] args) throws Exception
		 {
			//System.out.println(CheckGraphicsElementExist("/Users/yuvaraj/Desktop/RR 3.0 CHENNAI/101219518/401060817/100_XML/A01/GS1_401060817A01_39.xml"));

			
			Utils utils = new Utils();
			if(!XmlUtiility.CheckGraphicsElementExist("/Volumes/TORNADO/TORNADO_TESTING/101221125/401061297/100_XML/A01/GS1_40106129701_9.xml"))
			{
				String barcodePath =  "/Volumes/TORNADO/TORNADO_TESTING/101221125/401061297/" + "030_Barcodes/";
				if(!utils.FileExists(barcodePath))
				{
					barcodePath ="/Volumes/TORNADO/TORNADO_TESTING/101221125/401061297/" + "030 Barcodes/";
				}
				XmlUtiility.GS1XmlAppendGraphicsElement("/Volumes/TORNADO/TORNADO_TESTING/101221125/401061297/100_XML/A01/GS1_40106129701_9.xml", XmlUtiility.GetFileFromPathString(barcodePath).toString());
			}
			else
			{
				String barcodePath =  "/Volumes/TORNADO/TORNADO_TESTING/101221125/401061297/" + "030_Barcodes/";
				if(!utils.FileExists(barcodePath))
				{
					barcodePath ="/Volumes/TORNADO/TORNADO_TESTING/101221125/401061297/" + "030 Barcodes/";
				}
				XmlUtiility.GS1XmlParseGraphicsElement("/Volumes/TORNADO/TORNADO_TESTING/101221125/401061297/100_XML/A01/GS1_40106129701_9.xml", XmlUtiility.GetFileFromPathString(barcodePath).toString());
			}
			
			
		 }
}
