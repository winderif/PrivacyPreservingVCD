package Utils;

import DataStructure.ShotInVideo;
import DataStructure.VideoInfo;

public class VideoLinkage {
	private static double PruneThs = 0.0; 
	
	public boolean compareVideo(VideoInfo vBase, VideoInfo vCopy) {
		double maxBMValue = 0.0;
		int m = 0;
		int n = 0;		
		
    	while(vBase.shot[m] != null){    		
    		while(vCopy.shot[n] != null){
    			if(compareFrame(vBase.shot[m], vCopy.shot[n])) {  	    			
    				return true;
    			}
    			n++;
    		}
    		n = 0;
    		m++;
    	}    	
    	return false;	
	}
	
	public boolean compareFrame(ShotInVideo baseShot, ShotInVideo copyShot) {
    	if(compareCombine(baseShot, copyShot)) {
    		return true;    	
    	}
    	else {
    		return false;
    	}
    }
	
	public boolean compareCombine(ShotInVideo baseShot, ShotInVideo copyShot) {
    	int rows = baseShot.YCbCrAverage.length; 
    	int cols = copyShot.YCbCrAverage.length;
    	double[][] matrix = new double[rows][cols];
    	
    	// If Ture, then stop comparison.
    	// If False, then find the same signature in two different videos.
    	if(filterShot(baseShot.YCbCrSignature, copyShot.YCbCrSignature)){
    		return false;
    	}
    	
    	simFrame(matrix, 
    			baseShot.YCbCrAverage, copyShot.YCbCrAverage, 
    			baseShot.HSVHistogram, copyShot.HSVHistogram, 
    			baseShot.MV, copyShot.MV);
    	
		double sum = 0;
		for(int i=0; i<rows; i++){
			for(int j=0; j<cols; j++){
				sum = sum + matrix[i][j];
			}
		}
		if(sum < 0.00001) {
			return false;
		}
		
    	return false;
    }
	
	public boolean filterShot(boolean[][] baseSignature, boolean[][] copySignature) {
    	for(int i=0; i<baseSignature.length; i++){
    		for(int j=0; j<copySignature.length; j++){    			
    			if(baseSignature[i][0] == copySignature[j][0] && baseSignature[i][1] == copySignature[j][1] &&
    				baseSignature[i][2] == copySignature[j][2] && baseSignature[i][3] == copySignature[j][3] &&
    				baseSignature[i][4] == copySignature[j][4] && baseSignature[i][5] == copySignature[j][5] &&
    				baseSignature[i][6] == copySignature[j][6] && baseSignature[i][7] == copySignature[j][7] &&
    				baseSignature[i][8] == copySignature[j][8] && baseSignature[i][9] == copySignature[j][9] &&
    				baseSignature[i][10] == copySignature[j][10] && baseSignature[i][11] == copySignature[j][11] &&
    				baseSignature[i][12] == copySignature[j][12] && baseSignature[i][13] == copySignature[j][13] &&
    				baseSignature[i][14] == copySignature[j][14] && baseSignature[i][15] == copySignature[j][15] &&
    				baseSignature[i][16] == copySignature[j][16] && baseSignature[i][17] == copySignature[j][17] &&
    				baseSignature[i][18] == copySignature[j][18] && baseSignature[i][19] == copySignature[j][19] &&
    				baseSignature[i][20] == copySignature[j][20] && baseSignature[i][21] == copySignature[j][21] &&
    				baseSignature[i][22] == copySignature[j][22] && baseSignature[i][23] == copySignature[j][23] &&
    				baseSignature[i][24] == copySignature[j][24]) {
    				return false; // stop filtering and compare shots using video features.
    			}
    		}
    	}
    	return true; // do not need to compare shots.
    }
	
    public void simFrame(double[][] matrix, double[][] baseYCC, double[][] copyYCC, int[][] baseHist, int[][] copyHist, int[][] baseMV, int[][] copyMV){
    	int rows = baseYCC.length; 
    	int cols = copyYCC.length;    			
//		double matchWeigh;
    	/**
		for(int i=0; i<rows; i++){
			for(int j=0; j<cols; j++){
				if(i==0 || j==0){
					matrix[i][j] = 2.0/3.0*compareYCCFrame(baseYCC[i],copyYCC[j]) + 
							1.0/3.0*compareHSVFrame(baseHist[i],copyHist[j]);
					if(matrix[i][j] <= PruneThs) {
						matrix[i][j] = 0;
					}
				}
				else{
					matrix[i][j] = 2.0/3.0*compareYCCFrame(baseYCC[i],copyYCC[j]) +
							1.0/6.0*compareHSVFrame(baseHist[i],copyHist[j]) +
							1.0/6.0*compareMVhistFrame(baseMV[i-1],copyMV[j-1]);
					if(matrix[i][j] <= PruneThs) {
						matrix[i][j] = 0;
					}
				}
			}
		}
		*/
    }
}
