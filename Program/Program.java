package Program;

public abstract class Program {
	public void run() throws Exception {
		init();
		
		execute();
	}
	
    protected void init() throws Exception {
    	initialize();    	    	
    }
    
    abstract protected void initialize() throws Exception;
    
    protected void execute() throws Exception {
    	execQueryTransfer();    	   
    	
    	execVideoCopyDetection();
    	
    	execResultTransfer();
    }
    
    abstract protected void execQueryTransfer() throws Exception;
    
    abstract protected void execVideoCopyDetection() throws Exception;    	
    
    abstract protected void execResultTransfer() throws Exception;
}
