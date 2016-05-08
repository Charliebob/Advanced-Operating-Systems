package com.charliezhangsde;
import java.util.Random;

	/**
	 *  <i>Standard random</i>. This class provides methods for generating
	 *  random number from various distributions.
	 *  <p>
	 *  For additional documentation, see <a href="http://introcs.cs.princeton.edu/22library">Section 2.2</a> of
	 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by Robert Sedgewick and Kevin Wayne.
	 *
	 *  @author Robert Sedgewick
	 *  @author Kevin Wayne
	 */
public class myExpRandom {
	    private static Random random;    // pseudo-random number generator
	    private static long seed;        // pseudo-random number generator seed

	    // static initializer
	    static {
	        // this is how the seed was set in Java 1.4
	        seed = System.currentTimeMillis();
	        random = new Random(seed);
	    }

	    // don't instantiate
	    private myExpRandom() { }

	    /**
	     * Sets the seed of the psedurandom number generator.
	     */
	    public static void setSeed(long s) {
	        seed   = s;
	        random = new Random(seed);
	    }

	    /**
	     * Returns the seed of the psedurandom number generator.
	     */
	    public static long getSeed() {
	        return seed;
	    }

	    /**
	     * Return real number uniformly in [0, 1).
	     */
	    public static double uniform() {
	        return random.nextDouble();
	    }

	    /**
	     * Returns an integer uniformly between 0 (inclusive) and N (exclusive).
	     * @throws IllegalArgumentException if <tt>N <= 0</tt>
	     */
	    public static int uniform(int N) {
	        if (N <= 0) throw new IllegalArgumentException("Parameter N must be positive");
	        return random.nextInt(N);
	    }

	    ///////////////////////////////////////////////////////////////////////////
	    //  STATIC METHODS BELOW RELY ON JAVA.UTIL.RANDOM ONLY INDIRECTLY VIA
	    //  THE STATIC METHODS ABOVE.
	    ///////////////////////////////////////////////////////////////////////////

	    /**
	     * Returns a real number uniformly in [0, 1).
	     * @deprecated clearer to use {@link #uniform()}
	     */
	    public static double random() {
	        return uniform();
	    }

	    /**
	     * Returns an integer uniformly in [a, b).
	     * @throws IllegalArgumentException if <tt>b <= a</tt>
	     * @throws IllegalArgumentException if <tt>b - a >= Integer.MAX_VALUE</tt>
	     */
	    public static int uniform(int a, int b) {
	        if (b <= a) throw new IllegalArgumentException("Invalid range");
	        if ((long) b - a >= Integer.MAX_VALUE) throw new IllegalArgumentException("Invalid range");
	        return a + uniform(b - a);
	    }

	    /**
	     * Returns a real number uniformly in [a, b).
	     * @throws IllegalArgumentException unless <tt>a < b</tt>
	     */
	    public static double uniform(double a, double b) {
	        if (!(a < b)) throw new IllegalArgumentException("Invalid range");
	        return a + uniform() * (b-a);
	    }

	////////////**************Exponential Distribution using Uniform Distribution************
	    /**
	     * Returns a real number from an exponential distribution with rate lambda.
	     * @throws IllegalArgumentException unless <tt>lambda > 0.0</tt>
	     */
	    public static double exp(double lambda) {
	        if (!(lambda > 0.0))
	            throw new IllegalArgumentException("Rate lambda must be positive");
	        return -Math.log(1 - uniform()) / lambda;
	    }
	////////////****************uniform above*******
	    
	    public static void csEnter(){
	    	//Mean duration of critical section
	    	for(int i=0; i<1000; i++){
		    	try {
		    		long timeCS = (long)(exp(100)*1000);
					Thread.sleep(timeCS);
					System.out.println(i+" cs " + timeCS +"ms");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	
		    	//Mean delay between two consecutive critical
		    	try {
		    		long timeDelay = (long)(exp(20)*1000);
					Thread.sleep(timeDelay);
					System.out.println(i+" delay " + timeDelay + "ms");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    	
	    }
	    
	    
	    /**
	     * Unit test.
	     */
	    public static void main(String[] args) {

	          csEnter();

	        
	    }

		

}
