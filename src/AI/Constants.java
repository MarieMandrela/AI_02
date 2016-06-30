package AI;

public class Constants {
	public static int WALL = -2;
	public static int EMPTY = -1;
	public static String RNG = "RANDOM";
	public static String LOOK = "LOOKAHEAD";
	public static String DIJK = "DIJKSTRA";
	public static int DIM = 31;
	public static int DIRNUM = 8;
	public static int[][] DIRS = {
		{0, 1}, 
		{1, 1}, 
		{1, 0},
		{1, -1},
		{0, -1},
		{-1, -1},
		{-1, 0},
		{-1, 1}
	};
	
	// sigma 1.0, kernel size 5
	public static float[][] GAUSS = {
		{0.003765f, 0.015019f, 0.023792f, 0.015019f, 0.003765f},
		{0.015019f, 0.059912f, 0.094907f, 0.059912f, 0.015019f},
		{0.023792f, 0.094907f, 0.150342f, 0.094907f, 0.023792f},
		{0.015019f, 0.059912f, 0.094907f, 0.059912f, 0.015019f},
		{0.003765f, 0.015019f, 0.023792f, 0.015019f, 0.003765f}
	};
	public static float[][] GAUSSKERNEL = {
		{2, 7, 12, 7, 2},
		{7, 31, 52, 31, 7},
		{12, 52, 127, 52, 12},
		{7, 31, 52, 31, 7},
		{2, 7, 12, 7, 2}
	};
	public static float MAXGAUSS = 0.150342f;
	public static int GAUSSSIZE = 5;
}
