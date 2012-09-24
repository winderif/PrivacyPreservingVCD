// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.net.*;
import java.util.Vector;
import java.io.*;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.input.CountingInputStream;

import DataStructure.VideoInfo;

public abstract class ProgServer extends Program {

    private final int  		serverPort   = 23456;             // server port number
    private ServerSocket  	sock         = null;              // original server socket
    private Socket         	clientSocket = null;              // socket created by accept
	
    public static String databaseDirName = null;
    protected File[] databaseFiles = null;
	
    public void run() throws Exception {
    	create_socket_and_listen();

    	super.run();

    	cleanup();    	
    }

    protected void init() throws Exception {    	    	    	
    	super.init();
    }    	    	

    private void create_socket_and_listen() throws Exception {
    	sock = new ServerSocket(serverPort);            // create socket and bind to port
//		System.out.println("waiting for client to connect");
		clientSocket = sock.accept();                   // wait for client to connect
//		System.out.println("client has connected");

		CountingOutputStream cos = new CountingOutputStream(clientSocket.getOutputStream());
		CountingInputStream  cis = new CountingInputStream(clientSocket.getInputStream());
	
		ProgCommon.oos = new ObjectOutputStream(cos);
		ProgCommon.ois = new ObjectInputStream(cis);
    }
    
    protected void initialize() throws Exception {
    	loadDatabase();        
    }
    
    private void loadDatabase() {
		File dirFile = new File(databaseDirName);
		if(!dirFile.isDirectory()) {
			System.out.println("\t[S][ERROR]\tNot a dictionary.");
			System.exit(0);
		}
		else {			
			System.out.println("\t[S][START]\tLoad database data.");
			
			// Filter out files which the file name has ".dat".
			FilenameFilter filter = new FilenameFilter() {  
	    		public boolean accept(File file, String name) {  
	    			boolean ret = name.endsWith(".dat");   
	    			return ret;  
	    		}
	    	};			
			databaseFiles = dirFile.listFiles(filter);						
		}
    }
    
    protected VideoInfo LoadVideoInfoFile(File databaseFile) {    	
    	try {
    		ObjectInputStream oisQuery = 
    				new ObjectInputStream(new FileInputStream(databaseFile));
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
    
    private void cleanup() throws Exception {
    	ProgCommon.oos.close();                          // close everything
    	ProgCommon.ois.close();
    	clientSocket.close();
    	sock.close();
    }        
}