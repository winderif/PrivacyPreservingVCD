package Program;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;
import Matching.FastHungarianAlgorithm;
import Matching.Hungarian;
import Score.Distance;
import Score.DistanceL2square;

import DataStructure.ShotInVideo;
import DataStructure.VideoInfo;

public class VCDSystemServer extends ProgServer {
	// 16 x 4 x 4 = 256-bin, 16 in H (hue), 4 in S (saturation), 4 in V (value). 
	private static final int BinHSVHistogram = 256;
	// Motion vector falls into 9-bin.
	private static final int BinMVHistogram = 9;
	// # of block in each frame
	private static final int BinYCbCrAverage = 396;
	
	private static final int BinYCbCrSignature = 25;
	
	private static final int BinAllFeatures = BinHSVHistogram + BinMVHistogram + BinYCbCrAverage;
	
	private static final double n = 352*288;
	private static final double nBlock = n/16/16;
	
	private static final double lambda_YCbCr 		= 2.0/3.0;
	private static final double lambda_HSVHist_NoMV = 1.0/3.0;
	private static final double lambda_HSVHist 		= 1.0/6.0;
	private static final double lambda_MV 			= 1.0/6.0;
	
	private VideoInfo queryVideoInfo = null;
	private Vector<File> copiedVideoFile = new Vector<File>();
	
	private Distance mDistance = null;
	
	protected void init() throws Exception {    	
    	super.init();
    	
    	this.mDistance = new DistanceL2square();
    }
	
	protected void execQueryTransfer() throws Exception {		
		queryVideoInfo = (VideoInfo)VCDSystemCommon.ois.readObject();	
	}
    
    protected void execVideoCopyDetection() throws Exception {
    	for(int i=0; i<databaseFiles.length; i++) {
    		VideoInfo databaseVideoInfo = LoadVideoInfoFile(databaseFiles[i]);
    		if(compareVideo(queryVideoInfo, databaseVideoInfo)) {
//    			System.out.println(databaseFiles[i].getName() + " 1");
    			 
    			copiedVideoFile.add(databaseFiles[i]);
    		}
    		else {
//    			System.out.println(databaseFiles[i].getName() + " 0");
    		}
    	}
    }
    
	public boolean compareVideo(VideoInfo vBase, VideoInfo vCopy) {
		int m = 0;
		int n = 0;
		while(vBase.shot[m] != null){
    		while(vCopy.shot[n] != null){
    			if(compareFrame(vBase.shot[m], vCopy.shot[n])){  	    			
    				return true;
    			}
    			n++;
    		}
    		n=0;    		
    		m++;
    	}   
    	return false;		
	}
	
	private boolean compareFrame(ShotInVideo baseShot, ShotInVideo copyShot) {
		double[][] mHungarianMatrix = null;
		double distance = 0.0;
		
		if(filterShot(baseShot.YCbCrSignature, copyShot.YCbCrSignature)) {
			return false;
		}
		
		mHungarianMatrix = execBuildBipartiteGraph(baseShot, copyShot);
		
		distance = execFindBestMatching(mHungarianMatrix);
		
		if(distance < 0.04) {
//			System.out.println("distance: " + distance);
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean filterShot(boolean[][] baseSignature, boolean[][] copySignature) {
		for(int i=0; i<baseSignature.length; i++) {
			for(int j=0; j<copySignature.length; j++) {   
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
					return false;
				}
			}
		}
		return true;
	}
    
    private double[][] execBuildBipartiteGraph(ShotInVideo baseShot, ShotInVideo copyShot) {    	
    	int nRow = baseShot.HSVHistogram.length;
    	int nColumn = copyShot.HSVHistogram.length;    	
    	double[][] mHungarianMatrix = new double[nRow][nColumn];
    	for(int i=0; i<nRow; i++) {
    		for(int j=0; j<nColumn; j++) {
    			// The similarity between two frames
    			if(i==0 || j==0) {
    				mHungarianMatrix[i][j] = 
    						lambda_YCbCr * compareYCbCrAverage(baseShot.YCbCrAverage[i], copyShot.YCbCrAverage[j]) + 
    						lambda_HSVHist_NoMV * compareHSVHistogram(baseShot.HSVHistogram[i], copyShot.HSVHistogram[j]);
    			}
    			else {
    				/**
    				System.out.println("YCC\t" + compareYCbCrAverage(baseShot.YCbCrAverage[i], copyShot.YCbCrAverage[j])/nBlock);
    				System.out.println("HSV\t" + compareHSVHistogram(baseShot.HSVHistogram[i], copyShot.HSVHistogram[j]));
    				System.out.println("MV\t" + compareMotionVector(baseShot.MV[i-1], copyShot.MV[j-1]));
    				*/
    				mHungarianMatrix[i][j] =
    						lambda_YCbCr * compareYCbCrAverage(baseShot.YCbCrAverage[i], copyShot.YCbCrAverage[j]) + 
    						lambda_HSVHist * compareHSVHistogram(baseShot.HSVHistogram[i], copyShot.HSVHistogram[j]) + 
    						lambda_MV * compareMotionVector(baseShot.MV[i-1], copyShot.MV[j-1]);
    			}
    		}
    	}
    	return mHungarianMatrix;
    }
    
    private double compareYCbCrAverage(double[] baseYCbCr, double[] copyYCbCr) {
//    	return mDistance.evaluate(baseYCbCr, copyYCbCr) / nBlock;
    	return mDistance.evaluate(baseYCbCr, copyYCbCr);
    }
    
    private double compareHSVHistogram(int[] baseHSVHist, int[] copyHSVHist) {
    	return mDistance.evaluate(
    			IntArrayToDoubleArray(baseHSVHist, n), IntArrayToDoubleArray(copyHSVHist, n));
    }
    
    private double compareMotionVector(int[] baseMotionVector, int[] copyMotionVector) {
    	return mDistance.evaluate(
    			IntArrayToDoubleArray(baseMotionVector, nBlock), IntArrayToDoubleArray(copyMotionVector, nBlock));
    	
    }
    
    private double[] IntArrayToDoubleArray(int[] intArray, double norm) {
    	double[] doubleArray = new double[intArray.length];
    	for(int i=0; i<intArray.length; i++) {
    		doubleArray[i] = (double)intArray[i] / norm;
    	}
    	return doubleArray;
    }
    
    private double execFindBestMatching(double[][] mHungarianMatrix) {
    	String sumType = "min";
		int[][] assignment = new int[mHungarianMatrix.length][2];
		
		if(mHungarianMatrix.length > mHungarianMatrix[0].length) {
			mHungarianMatrix = FastHungarianAlgorithm.transpose(mHungarianMatrix);
		}	
		
		assignment = FastHungarianAlgorithm.hgAlgorithm(mHungarianMatrix, sumType);
		
		double distance = 0.0;
		for (int i=0; i<assignment.length; i++) {
			distance = distance + 
					mHungarianMatrix[assignment[i][0]][assignment[i][1]];			
		}						
		
		int denominator = Math.max(mHungarianMatrix.length, mHungarianMatrix[0].length);
		
//		return distance;
		return distance / denominator;
    }
    
    protected void execResultTransfer() throws Exception {
    	 for(File dFile : copiedVideoFile) {
    		 System.out.println(dFile.getName().replace("video_new_", "").replace(".mpg.dat", ""));
    	 }
    }
}
