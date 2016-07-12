package AI;

import java.util.Random;

public class DijkstraTokenAI extends TokenAI {
	
	private int playerNum;
	private int tokenNum;
	
	private boolean[][][][] adjacency = new boolean[Constants.DIM][Constants.DIM][Constants.DIM][Constants.DIM];
	private float[][] influence = new float[Constants.DIM][Constants.DIM];
	private float[][] half_infuence = new float[Constants.DIM / 2][Constants.DIM / 2];
	private float gamma = 0.00001f;
	private float[][] value = new float[Constants.DIM][Constants.DIM];
	private boolean[][] reachable = new boolean[Constants.DIM][Constants.DIM];
	
	private Random rnd;
	
	volatile int[] scores = new int[4];
	volatile int[][] board = new int[Constants.DIM][Constants.DIM];
	volatile float[][][] tokens = new float[4][3][2];
	volatile float[][] directions = new float[3][2];
	
	
	public DijkstraTokenAI(int playerNum, int tokenNum, int[][] board, float[][][] tokens, float[][] directions, int[] scores, boolean[][][][] adjacency) {
		super();
		this.playerNum = playerNum;
		this.tokenNum = tokenNum;
		this.board = board;
		this.tokens = tokens;
		this.directions = directions;
		this.scores = scores;
		this.adjacency = adjacency;
		this.rnd = new Random();
		this.rnd.setSeed(playerNum + tokenNum);
		
		initReachable();
		setInfluence();
		setValues();
	}
	
	private void setInfluence() {
		float newVal = 0;
		
		for (int i = 0; i < Constants.DIM; i++) {
			for (int j = 0; j < Constants.DIM; j++) {
				float field = getBoard(i, j);
				if (!this.reachable[i][j] || field == Constants.WALL) {
					newVal = -10;
				}
				else if (field == this.playerNum) {
					newVal = -1;
				} else if (field >= 0 && field <= 3) {
					newVal = 1;
				} else if (field == Constants.EMPTY) {
					newVal = 1;
				}
				
				this.influence[i][j] = newVal;
			}
		}
		
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				if (this.tokenNum == j) {
					continue;
				}
				
				applyTokenInfluence((int)this.tokens[i][j][0], (int)this.tokens[i][j][1]);
				
				if (i == this.playerNum) {
					applyTokenInfluence((int)this.tokens[i][j][0], (int)this.tokens[i][j][1]);
				}
			}
		}
		
		for (int i = 0; i < Constants.DIM - 1; i++) {
			for (int j = 0; j < Constants.DIM - 1; j++) {
				this.half_infuence[i / 2][j / 2] = 0;
			}
		}
		
		for (int i = 0; i < Constants.DIM - 1; i++) {
			for (int j = 0; j < Constants.DIM - 1; j++) {
				if (this.influence[i][j] != -10) {
					this.half_infuence[i / 2][j / 2] += this.influence[i][j];
				}
			}
		}
	}
	
	private void applyTokenInfluence(int x, int y) {
		int half = (int)(Constants.GAUSSSIZE / 2);
		int new_x, new_y;
		float newVal;
		
		for (int i = -half; i <= half; i++) {
			for (int j = -half; j <= half; j++) {
				new_x = x + i;
				new_y = y + j;
				if (new_x >= 0 && new_x < Constants.DIM &&
					new_y >= 0 && new_y < Constants.DIM &&
					getBoard(new_x, new_y) != Constants.WALL) {
					newVal = Constants.GAUSS[half + i][half + j];
					this.influence[new_x][new_y] = this.influence[new_x][new_y] - newVal;
				}
			}
		}
	}
	
	private void setValues() {
		for (int i = 0; i < Constants.DIM; i++) {
			for (int j = 0; j < Constants.DIM; j++) {
				this.value[i][j] = evaluateField(i, j);
			}
		}
	}

	@Override
	public void run() {
		int[] goal = new int[2];
		float[] dir = new float[2];
		int timeCounter = 5000;
		int updateRate = 5000;
		
		while (true) {		
			try {
				if (timeCounter >= updateRate ||
					(this.tokens[this.playerNum][this.tokenNum][0] == goal[0] &&
					 this.tokens[this.playerNum][this.tokenNum][1] == goal[1])) {
					getTarget(goal);
					timeCounter = 0;
				}
				
				goSomewhere(limit(getX()), limit(getY()), goal[0], goal[1], dir);
				directions[this.tokenNum][0] = dir[0];
				directions[this.tokenNum][1] = dir[1];
            	if (this.tokenNum == 1) {
            		Thread.sleep(400);
            		timeCounter += 400;
            	}
				if (this.tokenNum == 2) {
            		Thread.sleep(800);
            		timeCounter += 800;
            	}
            	if (this.tokenNum == 0) {
            		Thread.sleep(150);
	            	directions[this.tokenNum][0] = 0;
					directions[this.tokenNum][1] = 0;
	            	Thread.sleep(100);
	            	timeCounter += 250;
            	}
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
		}
	}
	
	private void getTarget(int[] xy) {
		setInfluence();
		getMaxInfluence(xy);
	}
	
	private void getMaxInfluence(int[] xy) {
		float max = -10;
		
		for (int i = 0; i < Constants.DIM / 2 - 1; i++) {
			for (int j = 0; j < Constants.DIM / 2 - 1; j++) {
				if (this.half_infuence[i][j] > max ||
					(this.half_infuence[i][j] == max && rnd.nextBoolean())) {
					max = this.half_infuence[i][j];
					xy[0] = i * 2;
					xy[1] = j * 2;
				}
			}
		}
		
		max = -10;
		
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				if (this.influence[xy[0] + i][xy[1] + j] > max) {
					max = this.influence[xy[0] + i][xy[1] + j];
					xy[0] += i;
					xy[1] += j;
				}
			}
		}
	}
	
	private void goSomewhere(int x, int y, int toX, int toY, float[] vector) {
		int[] dir = {-1,-1};
		setValues();
		dijkstra(x, y, toX, toY, dir);
		getDir(vector, x, y, dir[0], dir[1]);
	}
	
	private void initReachable() {
		boolean[][] visited = new boolean[Constants.DIM][Constants.DIM];
		float[][] distances = new float[Constants.DIM][Constants.DIM];
		
		for (int i = 0; i < Constants.DIM; i++) {
			for (int j = 0; j < Constants.DIM; j++) {
				this.reachable[i][j] = false;
				distances[i][j] = Float.MAX_VALUE;
			}
		}
		
		int currentX = (int) this.tokens[this.playerNum][this.tokenNum][0];
		int currentY = (int) this.tokens[this.playerNum][this.tokenNum][1];
		this.reachable[currentX][currentY] = true;
		distances[currentX][currentY] = 0;
		float min = 0;
		
		while (min != Float.MAX_VALUE) {
			min = Float.MAX_VALUE;
			
			for (int i = 0; i < Constants.DIM; i++) {
				for (int j = 0; j < Constants.DIM; j++) {
					float tentativeDistance = distances[currentX][currentY] + 1;
					if (this.adjacency[currentX][currentY][i][j]&& 
							!visited[i][j] && 
							tentativeDistance < distances[i][j]) {
						distances[i][j] = tentativeDistance;
					}
				}
			}
			
			this.reachable[currentX][currentY] = true;

			for (int i = 0; i < Constants.DIM; i++) {
				for (int j = 0; j < Constants.DIM; j++) {
					if (!this.reachable[i][j] && distances[i][j] <= min) {
						min = distances[i][j];
						currentX = i;
						currentY = j;
					}
				}
			}
			
		}
	}
	
	private void dijkstra(int x, int y, int toX, int toY, int[] dir) {
		
		boolean[][] visited = new boolean[Constants.DIM][Constants.DIM];
		float[][] distances = new float[Constants.DIM][Constants.DIM];
		int[][][] previous = new int [Constants.DIM][Constants.DIM][2];
		int[] xy = {-1,-1};

		for (int i = 0; i < Constants.DIM; i++) {
			for (int j = 0; j < Constants.DIM; j++) {
				visited[i][j] = false;
				distances[i][j] = Float.MAX_VALUE;
				previous[i][j][0] = -1;
				previous[i][j][1] = -1;
			}
		}
		visited[x][y] = true;
		distances[x][y] = 0;
		int currentX = x;
		int currentY = y;
		
		while (!visited[toX][toY]) {
			
			updateNeighbours(distances, visited, previous, currentX, currentY);
			visited[currentX][currentY] = true;
			
			minDistance(distances, visited, currentX, currentY, xy);
			currentX = xy[0];
			currentY = xy[1];
		}
		
		getFirstDir(previous, x, y, toX, toY, dir);
	}
	
	private void updateNeighbours(float[][] distances, boolean[][] visited, int[][][] previous, int currentX, int currentY) {
		
		for (int i = 0; i < Constants.DIM; i++) {
			for (int j = 0; j < Constants.DIM; j++) {
				float tentativeDistance = distances[currentX][currentY] + this.value[i][j];
				if (this.adjacency[currentX][currentY][i][j]&& 
						!visited[i][j] && 
						tentativeDistance < distances[i][j]) {
					distances[i][j] = tentativeDistance;
					previous[i][j][0] = currentX;
					previous[i][j][1] = currentY;
				}
			}
		}
	}
	
	private void minDistance(float[][] distances, boolean[][] visited, int currentX, int currentY, int[] xy) {
		float min = Float.MAX_VALUE;
		
		for (int i = 0; i < Constants.DIM; i++) {
			for (int j = 0; j < Constants.DIM; j++) {
				if (!visited[i][j] && distances[i][j] <= min) {
					min = distances[i][j];
					xy[0] = i;
					xy[1] = j;
				}
			}
		}
	}
	
	private void getFirstDir(int[][][] previous, int x, int y, int toX, int toY, int[] xy) {
		int i = toX;
		int j = toY;
		
		while ( i >= 0 && i < Constants.DIM &&
				j >= 0 && j < Constants.DIM &&
				!(previous[i][j][0] == x && previous[i][j][1] == y)) {
			int new_i = previous[i][j][0];
			int new_j = previous[i][j][1];
			i = new_i;
			j = new_j;
		};
		
		xy[0] = i;
		xy[1] = j;
	}
	
	private int getBoard(float x, float y) {
		return this.board[limit(x)][limit(y)];
	}
	
	private int limit(float value) {
	    return (int)Math.max(0, Math.min(value, Constants.DIM));
	}
	
	private float getX() {
		return this.tokens[this.playerNum][this.tokenNum][0];
	}
	
	private float getY() {
		return this.tokens[this.playerNum][this.tokenNum][1];
	}
	
	private void getDir(float[] xy, float xFrom, float yFrom, float xTo, float yTo) {
		xy[0] = xTo - xFrom;
		xy[1] = yTo - yFrom;
	}
	
	private boolean fieldOccupied(float x, float y) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				if (i == this.playerNum && j == this.tokenNum) {
					continue;
				}
				
				if ((int)this.tokens[i][j][0] == (int)x  &&
				    (int)this.tokens[i][j][1] == (int)y) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Evaluate how good its is to step on the given field.
	 * Prefer painting over enemy color > empty > my color > wall
	 * Try to avoid running into other tokens
	 */
	private float evaluateField(float x, float y) {
		int field = getBoard(x, y);
		float val = 2;
		
		if (field >= 0 && field <= 3 && field != this.playerNum) {
			val = 1;
		}
		if (field == this.playerNum) {
			val = 6;
		}
		if (fieldOccupied(x, y)) {
			val += 1;
		}
		
		return val;
	}
}
