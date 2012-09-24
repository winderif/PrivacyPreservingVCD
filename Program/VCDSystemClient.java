package Program;

public class VCDSystemClient extends ProgClient {
	
	
	protected void execQueryTransfer() throws Exception {
		VCDSystemCommon.oos.writeObject(queryVideoInfo);
		VCDSystemCommon.oos.flush();	
	}
    
    protected void execVideoCopyDetection() throws Exception {
    	 
    }
    
    protected void execBuildBipartiteGraph() throws Exception {
    	 
    }
    
    protected void execFindBestMatching() throws Exception {
    	 
    }
    
    protected void execResultTransfer() throws Exception {
    	 
    }
}
