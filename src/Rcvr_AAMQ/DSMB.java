package Rcvr_AAMQ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import jcifs.Config;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;



import java.util.Arrays;
import java.util.List;

	 
public class DSMB{

	    static final String USER_NAME = "chautoscript";
	    static final String PASSWORD = "TeamI#di@";
	    static final String NETWORK_FOLDER = "smb://10.33.7.14/RMQ/LogBackup/";
	    
	   
	   
	    public void SetProp() throws Exception
	    {
	    	try
	    	{
	    		
	    		 Config.setProperty("jcifs.smb.SmbTransport.soTimeout", "180000");
	    		System.out.println(jcifs.smb.SmbTransport.SO_TIMEOUT);
	    		Config.setProperty( "jcifs.smb.SmbFile.soTimeout", "180000" ); 
	    		System.out.println(jcifs.smb.SmbFile.SO_TIMEOUT);
	    		Config.setProperty( "jcifs.netbios.cachePolicy", "1200" ); 
	   		
	    		
	    	}
	    	catch(Exception Ie)
	    	{
	    		System.out.println(Ie.getMessage());
	    	}
	    }
	    

	 

	 
	    public boolean copyFiles(String fileContent, String fileName) {
	        boolean successful = false;
	         try{
	   
	        	 
	        	 
	                String user = USER_NAME + ":" + PASSWORD;
	                System.out.println("User: " + user);
	 
	                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);
	                String path = NETWORK_FOLDER + fileName;
	                System.out.println("Path: " +path);
	 
	                SmbFile sFile = new SmbFile(path, auth);
	                SmbFileOutputStream sfos = new SmbFileOutputStream(sFile, true);
	                sfos.write(fileContent.getBytes());
	 
	                successful = true;
	                System.out.println("Successful " + successful + " " + fileContent);
	            } catch (Exception e) {
	                successful = false;
	                e.printStackTrace();
	            }
	        return successful;
	    }
	    
	    
	    
	    
	    
	    
	    
	    public String Fetch(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException
		{

			try
			{
				final String dir = System.getProperty("user.dir");

				String server_path = args[0];
				String username = args[1];
				String password = args[2];
				String domain = args[3];
				String workorder = args[4];

				NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, username, password);

				String path = "smb:" + server_path;
				SmbFile sFile = new SmbFile(path, auth);
				if (sFile.exists())
				{
					if (sFile.isDirectory())
					{
						SmbFile[] files = sFile.listFiles();
						if (files.length != 0)
						{
							int find_flag=0;
							for (SmbFile file : files)
							{
								String fname = file.getName();
								if (fname.equals(workorder + ".xml"))
								{
									find_flag=1;
									String fullpath1 = dir + "\\xml_bank\\" + fname;
									File outoutfile = new File(fullpath1);

									if (outoutfile.exists())
									{
										outoutfile.delete();
									}
									OutputStream os = new FileOutputStream(fullpath1);

									InputStream is = file.getInputStream();
									int bufferSize = 5096;

									byte[] b = new byte[bufferSize];
									int noOfBytes = 0;
									while ((noOfBytes = is.read(b)) != -1)
									{
										os.write(b, 0, noOfBytes);
									}
									os.close();
									is.close();

								}
							}
							if (find_flag==0)
								return ("File not found");
							else
								return("File found");
						}
						else
						return ("File not found");
					} else
					{
						return "Not a Directory";
					}
				} else
				{
					return "Invalid Path";
				}
			} catch (MalformedURLException | SmbException e)
			{
				File writer = new File("checking.txt");
				if (!writer.exists())
				{
					writer.createNewFile();
				}
				((Throwable) e).printStackTrace(new PrintStream(writer));
				((Throwable) e).printStackTrace();
				return "Invalid Path";

			}
		}
	    




	        public void listFiles() {
	        	
	            try {
	                String USER     = "IMAC366";
	    	        		String PASSWORD = "Imac366123";
	    	        
	    	        		Config.setProperty("jcifs.smb.client.minVersion", "SMB202");
	    	        		Config.setProperty("jcifs.smb.client.maxVersion", "SMB202");

	    	        		 
	    	        		    
	                NtlmPasswordAuthentication authentication = new NtlmPasswordAuthentication(
	                        "asia", USER, PASSWORD);
	                SmbFile home = new SmbFile("smb://10.52.1.200/Lmac35/", authentication);

	                if(home.isDirectory()) {
	                    List<SmbFile> files = Arrays.asList(home.listFiles());
	                    for(SmbFile file: files) {
	                        if(file.isDirectory()) {
	                            System.out.println("Directory: " + file.getName());
	                        }
	                        if(file.isFile()) {
	                            System.out.println("File: " + file.getName());
	                        }
	                    }
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        
	    }
	    
	    
	    public static void main(String args[])
	    {
	    	DSMB sm = new  DSMB();
	    	sm.listFiles();
	    	
	    }
}
	    