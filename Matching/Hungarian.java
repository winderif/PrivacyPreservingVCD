package Matching;
/**
 * This work is non-copyrightable
 * @author Myriam Abramson
 * myriam.abramson@nrl.navy.mil
 */

//package adprey;

import java.util.*;
import java.text.*;
import java.io.*;

/**
 * Adapted from the website http://216.249.163.93/bob.pilgrim/445/munkres.html
 * roles x agents
 * find best role allocation
 * the coefficient of the matrix are the role preferences of the agents
 */
public class Hungarian {
    public double [][] matrix;
    int [] rCov;
    int [] cCov;
    int [][] stars;
    int rows;
    int cols;
    int dim;
    int solutions;
    Random rand = new Random();
    static int FORBIDDEN_VALUE = 9999;

    //columns = agents
    //rows = roles

    public Hungarian (int rows, int columns) {
		this.rows = rows;
		this.cols = columns;
		dim = Math.max(rows,columns);
		solutions = dim;
		matrix = new double[dim][dim];
		stars = new int[dim][dim];
		rCov = new int[dim];
		cCov = new int[dim];	
    }

    /**
     * converts x,y to one dimension
     */
    public int two2one (int x, int y) {
		return (x * dim) + y;
    }

    public int one2col (int n) {
		return (n % dim);
    }

    public int one2row (int n) {
		return (int) (n / dim);
    }

    // step 0 transform the matrix from maximization to minimization
    public void max2min () {
		double maxVal=Double.MIN_VALUE;
		for (int i=0;i<rows;i++) {
		    for (int j=0;j<cols;j++) {
			if (matrix[i][j] > maxVal)
			    maxVal = matrix[i][j];
		    }
		}
		for (int i=0;i<rows;i++){		
		    for (int j=0;j<cols;j++) {
			matrix[i][j] = maxVal - matrix[i][j];
		    }
	    }
    }

    // step1 find the minimum in each row and subtract it
    public void rowMin () {
		for (int i=0;i<dim;i++) {
		    double minVal = matrix[i][0];
		    for (int j=1;j<dim;j++) {
				if (minVal > matrix[i][j]) minVal = matrix[i][j];
		    }
		    for (int j=0;j<dim;j++)	matrix[i][j] -= minVal;
		}
    }

    public void colMin () {
		for (int j=0;j<dim;j++) {
		    double minVal = matrix[0][j];
		    for (int i=1;i<dim;i++) {
				if (minVal > matrix[i][j]) minVal = matrix[i][j];
		    }
		    for (int i=0;i<dim;i++)	matrix[i][j] -= minVal;
		}	
    }

    // step2 star the zeros

    public void starZeros () {
		for (int i=0;i<dim;i++)
		    for (int j=0;j<dim;j++) {
				if (matrix [i][j] == 0 && cCov[j] == 0 && rCov[i] == 0) {
				    stars[i][j] = 1;
			    	cCov[j] = 1;
			    	rCov[i] = 1;
				}
		    }
		clearCovers();
    }

    /**
     * step 3 -- check for solutions
     */
    public int coveredColumns() {

    	int k=0;
    	for (int i=0;i<dim;i++) { 
    	    for (int j=0;j<dim;j++) {
    	    	if (stars[i][j] == 1) {
    	    		cCov[j] = 1;
    	    	}
    	    }
    	}
    	for (int j=0;j<dim;j++) {
    	    k += cCov[j];
    	//	printIt();
    	//	printStars();
    	}
    	return k;
    }
		    

    /**
     * returns -1 if no uncovered zero is found
     * a zero whose row or column is not covered 
     */
    public int findUncoveredZero() {
	for (int i=0;i<dim;i++)
	    for (int j=0;j<dim;j++) {
		if (matrix[i][j] == 0 && rCov[i] == 0 && cCov[j] == 0) {

		    return two2one(i,j);
		}
	    }

	return -1;
    }

    /**
     * returns -1 if not found
     * returns the column if found
     */
    public int foundStarInRow(int zeroY) {
	for (int j=0;j<dim;j++) {
	    if (stars[zeroY][j] == 1)
		return j;
	}
	return -1;
    }

    /**
     * returns -1 if not found
     * returns the row if found
     */

    public int foundStarInCol(int zeroX) {
	for (int i=0;i<dim;i++) {
	    if (stars[i][zeroX] == 1)
		return i;
	}
	return -1;
    }

    /**
     * step 4
     * Cover all the uncovered zeros one by one until no more
     * cover the row and uncover the column
     */

    public boolean coverZeros () {

	int zero = findUncoveredZero();
	while (zero >= 0) {
	    int zeroCol = one2col(zero);
	    int zeroRow = one2row(zero);
	    stars[zeroRow][zeroCol] = 2; //prime it
	    int starCol = foundStarInRow(zeroRow);

	    if (starCol >= 0) {
		rCov[zeroRow] = 1;
		cCov[starCol] = 0;
	    }
	    else {
		//		printStars();
		starZeroInRow(zero); //step 5
		return false;
	    }
	    zero = findUncoveredZero();
	}
	//	printIt();
	//	printStars();
	return true;
    }

    
    public int findStarInCol(int col) {
	if (col < 0) {
	    System.err.println ("Invalid column index " + col);
	}
	for (int i=0;i<dim;i++) {
	    if (stars[i][col] == 1)
		return i;
	}
	return -1;
    }

    public void clearCovers () {
	for (int i=0;i<dim;i++) {
	    rCov[i] = 0;
	    cCov[i] = 0;
	}
    }

    /**
     * unstar stars
     * star primes
     */

    public void erasePrimes () {
	for (int i=0;i<dim;i++)
	    for (int j=0;j<dim;j++) {
		if (stars[i][j] == 2)		    
		    stars[i][j] = 0;
	    }
    }
		

    public void convertPath (int [][] path, int kount) {	
		for (int i=0;i<=kount;i++) {
	    	int x = path[i][0];
		    int y = path[i][1];
		    if (stars[x][y] == 1)
			stars[x][y] = 0;
		    else
			if (stars[x][y] == 2)
			    stars[x][y] = 1;
		}
    }

    /**
     * returns the column where a prime was found for a given row
     */

    public int findPrimeInRow (int row) {
	for (int j=0;j<dim;j++) 
	    if (stars[row][j] == 2)
		return j;
	System.err.println("No prime in row " + row + " found");	
	return -1;
    }
    
    /**
     * step 5
     * augmenting path algorithm 
     * go back to step 3
     */
    public void starZeroInRow (int zero) {
	boolean done = false;
	int zeroRow = one2row (zero); //row
	int zeroCol = one2col (zero); //column

	int kount = 0;
	int [][] path = new int[1000][2]; //how to dimension that?
	path[kount][0] = zeroRow;
	path[kount][1] = zeroCol;
	while (!done) {
	    int r = findStarInCol(path[kount][1]);
	    if (r >=0) {
		kount++;
		path[kount][0]=r;
		path[kount][1]=path[kount-1][1];
	    }
	    else {
		done = true;
		break;
	    }
	    int c = findPrimeInRow(path[kount][0]);

	    kount++;
	    //System.out.println("kount"+kount);
	    path[kount][0] = path[kount-1][0];
	    path[kount][1] = c;
	}
	convertPath(path, kount);
	clearCovers();
	erasePrimes();
    }

    public void solve() {	
    	max2min();
	    rowMin(); //step 1
		colMin();
		starZeros(); //step 2 
		boolean done = false;
		while (!done) {
		    int covCols = coveredColumns();//step 3	    
		    if (covCols >= solutions)break;
	
		    done = coverZeros(); //step 4 (calls step 5)
		    while (done) {
				double smallest = findSmallestUncoveredVal();
				uncoverSmallest(smallest); //step 6
				done = coverZeros();
		    }
		}
    }

    boolean freeRow(int row, int col) {
	for (int i=0;i<dim;i++) 
	    if (i != row && stars[i][col] == 1)
		return false;
	return true;
    }

    boolean freeCol (int row, int col) {
	for (int j=0;j<dim;j++) 
	    if (j != col && stars[row][j] == 1)
		return false;
	return true;
    }

    // read from left to right:
    // Role i is assigned to agent j
    public void printStarZeros(int[] iBM, int[] jBM){    	
    	int k=0;
		for (int i=0;i<rows;i++) 
	    	for (int j=0;j<cols;j++) {
			// check for independence
			if (stars[i][j] == 1 && (freeRow(i,j) || freeCol(i,j))){
//				System.out.println (i + " assigned to " + j + " is a solution");
		    	iBM[k]=i; jBM[k]=j; k++;		    	
			}
		    	
	    }
    }

    // get the assignments for the agents
    // the matrix is roles x agents
    public int [] getSolutions () {
		int [] solutions = new int[cols];
		for (int j=0;j<cols;j++) {
		    solutions[j] = -1;
		    for (int i=0;i<rows;i++) {
			// test for independence
			// should not be necessary
			if (stars[i][j] == 1 && (freeRow(i,j) || freeCol(i,j)))
			    solutions[j] = i;
				}
		}
		return solutions;
    }

    public double findSmallestUncoveredVal () {
    	double minVal = Double.MAX_VALUE;
    	for (int i=0;i<dim;i++)
    	    for (int j=0;j<dim;j++) {
    			if (rCov[i] == 0 && cCov[j] == 0) {
    			    if (minVal > matrix[i][j]) {
    			    	minVal = matrix[i][j];
    			    }
    			}
    	    }
    	return minVal;
    }
    /**
     * step 6
     * modify the matrix
     * if the row is covered, add the smallest value
     * if the column is not covered, subtract the smallest value
     */ 
    public void uncoverSmallest(double smallest) {
	for (int i=0;i<dim;i++) 
	    for (int j=0;j<dim;j++) {
		if (rCov[i] == 1) 
		    matrix[i][j] += smallest;
		if (cCov[j] == 0)
		    matrix[i][j] -= smallest;
	    }	
    }
    
    public static void main(String[] args) 
	{
		//Below enter "max" or "min" to find maximum sum or minimum sum assignment.
		String sumType = "max";		
				
		int numOfRows = readInput("How many rows for the matrix? ");
		int numOfCols = readInput("How many columns for the matrix? ");
		double[][] array = new double[numOfRows][numOfCols];
		generateRandomArray(array, "random");	//All elements within [0,1].
		/**
		double[][] array = {
				{100, 105, 100},
				{100, 105, 105},
				{105, 100, 50}};
		//<COMMENT> TO AVOID PRINTING THE MATRIX FOR WHICH THE ASSIGNMENT IS CALCULATED
		System.out.println("\n(Printing out only 2 decimals)\n");
		System.out.println("The matrix is:");
		for (int i=0; i<array.length; i++)
		{
			for (int j=0; j<array[i].length; j++)
				{System.out.printf("%.2f\t", array[i][j]);}
			System.out.println();
		}
		System.out.println();
		//</COMMENT>*/

		int rows = array.length;
		int cols = array[0].length;
		int[] iBM = new int[Math.min(rows,cols)];
		int[] jBM = new int[Math.min(rows,cols)];		
		Hungarian alg = new Hungarian (rows, cols);
		
		for(int i=0;i<rows;i++){
			for(int j=0;j<cols;j++){
				alg.matrix[i][j] = array[i][j];
			}
		}		
		alg.solve(); // solve Maximum bipartite matching		
		alg.printStarZeros(iBM, jBM); //  Obtain indices of BM Graph
		
		double sum = BMsim(array, rows, cols, iBM, jBM);	
		
		System.out.printf("\nThe %s is: %.2f\n", sumType, sum);
		
		int[][] assignment = new int[array.length][2];
		assignment = FastHungarianAlgorithm.hgAlgorithm(array, sumType);	//Call Hungarian algorithm.
	}
    
    public static double BMsim(double[][] matrix, int rows, int cols, int[] iBM, int[] jBM){
		double numerator = 0;
		int denominator = Math.max(rows,cols);
		for(int i=0; i<iBM.length; i++){
			if(matrix[iBM[i]][jBM[i]] == 0) {
				denominator++;
			}
			else {
				numerator = numerator + matrix[iBM[i]][jBM[i]];
			}
		}		
		System.out.println(numerator + " " + denominator);
//		return (double)numerator/denominator;
		return (double)numerator;
	}
    
    public static int readInput(String prompt)	//Reads input,returns double.
	{
		Scanner in = new Scanner(System.in);
		System.out.print(prompt);
		int input = in.nextInt();
		return input;
	}
    
    public static void generateRandomArray	//Generates random 2-D array.
	(double[][] array, String randomMethod)	
	{
		Random generator = new Random();
		for (int i=0; i<array.length; i++)
		{
			for (int j=0; j<array[i].length; j++)
			{
				if (randomMethod.equals("random"))
					{array[i][j] = generator.nextDouble();}
				if (randomMethod.equals("gaussian"))
				{
						array[i][j] = generator.nextGaussian()/4;		//range length to 1.
						if (array[i][j] > 0.5) {array[i][j] = 0.5;}		//eliminate outliers.
						if (array[i][j] < -0.5) {array[i][j] = -0.5;}	//eliminate outliers.
						array[i][j] = array[i][j] + 0.5;				//make elements positive.
				}
			}
		}			
	}
}
