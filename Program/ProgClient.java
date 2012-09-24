// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.io.*;
import java.net.*;
import java.util.Vector;
import DataStructure.VideoInfo;

public abstract class ProgClient extends Program {

    public static String serverIPname = "localhost";             // server IP name
    private final int    serverPort   = 23456;                   // server port number
    private Socket       sock         = null;                    // Socket object for communicating
    
    public static String queryDirName = null;
    protected File[] queryFiles = null;	
    protected VideoInfo queryVideoInfo = null;
    
    public void run() throws Exception {
    	create_socket_and_connect();

    	super.run();
    	
    	cleanup();
    }

    protected void init() throws Exception {
    	super.init();
    }        

    private void create_socket_and_connect() throws Exception {
    	sock = new java.net.Socket(serverIPname, serverPort);          // create socket and connect
    	ProgCommon.oos  = new java.io.ObjectOutputStream(sock.getOutputStream());  
    	ProgCommon.ois  = new java.io.ObjectInputStream(sock.getInputStream());
    }
    
    protected void initialize() throws Exception {
    	loadQuery();
    }
    
    
    private void loadQuery() {
		File dirFile = new File(queryDirName);
		if(!dirFile.isDirectory()) {
			System.err.println("[C][ERROR]\tNot a dictionary.");
			System.exit(0);
		}
		else {
			System.out.println("[C][START]\tLoad query data.");
				
			// Filter out files which the file name has ".dat".
			FilenameFilter filter = new FilenameFilter() {  
	    		public boolean accept(File file, String name) {  
	    			boolean ret = name.endsWith(".dat");   
	    			return ret;  
	    		}
	    	};			
			queryFiles = dirFile.listFiles(filter);
			/**
			// Load all ".dat" file.
			for(int i=0; i<queryFiles.length; i++) {
				queryVideoInfo = LoadVideoInfoFile(queryFiles[i]);
			}
			*/
			File queryFile = new File("query/video_new_ST1Query1.mpg.dat");
			queryVideoInfo = LoadVideoInfoFile(queryFile);
		}			
    }
    
    private VideoInfo LoadVideoInfoFile(File queryFile) {    	
    	try {
    		ObjectInputStream oisQuery = 
    				new ObjectInputStream(new FileInputStream(queryFile));
    		VideoInfo tmpVideoFile = (VideoInfo)oisQuery.readObject();
    		oisQuery.close();
    		return tmpVideoFile;    		
    	} catch(IOException e) {
    		e.printStackTrace();
    	} catch(ClassNotFoundException e2) {
    		e2.printStackTrace();
    	}
    	return null;
    }
    
    protected void execute() throws Exception {
    	super.execute();
    }        
    
    private void cleanup() throws Exception {
    	ProgCommon.oos.close();                                                   // close everything
		ProgCommon.ois.close();
		sock.close();
    }
}