package AI;

import java.util.Random;

public class DijkstraTokenAI extends TokenAI {
	
	private int playerNum;
	private int tokenNum;
	
	private boolean[][][][] adjacency = new boolean[Constants.DIM][Constants.DIM][Constants.DIM][Constants.DIM];
	private float[][] influence = new float[Constants.DIM][Constants.DIM];
	private long targetUpdateRate = 10000;
	private float[][] value = new float[Constants.DIM][Constants.DIM];
	private boolean[][] reachable = new boolean[Constants.DIM][Constants.DIM];
	
	private long gameStart;
	private long startegySwitchTime = 60000;
	
	private Random rnd;
	
	volatile int[] scores = new int[4];
	volatile int[][] board = new int[Constants.DIM][Constants.DIM];
	volatile float[][][] tokens = new float[4][3][2];
	volatile float[][] directions = new float[3][2];
	volatile int[][] targets = new int[3][2];
	
	
	public DijkstraTokenAI(int playerNum, int tokenNum, int[][] board, float[][][] tokens, 
			float[][] directions, int[] scores, boolean[][][][] adjacency, int[][] targets) {
		super();
		this.playerNum = playerNum;
		this.tokenNum = tokenNum;
		this.board = board;
		this.tokens = tokens;
		this.directions = directions;
		this.scores = scores;
		this.adjacency = adjacency;
		this.targets = targets;
		this.rnd = new Random();
		this.rnd.setSeed(10 + this.playerNum);
		
		initReachable();
		setInfluence();
		setValues();
	}
	
	private void setInfluence() {
		float newVal = 0;
		float enemyFieldVal = System.currentTimeMillis() - this.gameStart < this.startegySwitchTime ? 1 : 2;
		
		for (int i = 0; i < Constants.DIM; i++) {
			for (int j = 0; j < Constants.DIM; j++) {
				float field = getBoard(i, j);
				if (!this.reachable[i][j] || field == Constants.WALL) {
					newVal = -2;
				} else if (field == this.playerNum) {
					newVal = -1;
				} else if (field >= 0 && field <= 3) {
					newVal = enemyFieldVal;
				} else if (field == Constants.EMPTY) {
					newVal = 1;
				}
				
				this.influence[i][j] = newVal;
			}
		}
		
		// Avoid other tokens
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				if (this.tokenNum == j) {
					continue;
				}
				applyGaussInfluence((int)getX(i, j), (int)getY(i, j));
			}
		}
		
		// Avoid the targets of allied tokens
		for (int i = 0; i < 3; i++) {
			if (this.tokenNum == i) {
				continue;
			}
			applyGaussInfluence(getTargetX(i), getTargetY(i));
		}
	}
	
	private void applyGaussInfluence(int x, int y) {
		int gaussRadius = (int)(Constants.GAUSSSIZE / 2);
		int gaussX, gaussY;
		float gauss;
		
		for (int i = -gaussRadius; i <= gaussRadius; i++) {
			for (int j = -gaussRadius; j <= gaussRadius; j++) {
				gaussX = x + i;
				gaussY = y + j;
				if (inLimit(gaussX, gaussY) &&
					getBoard(gaussX, gaussY) != Constants.WALL) {
					gauss = Constants.GAUSS[gaussRadius + i][gaussRadius + j];
					this.influence[gaussX][gaussY] = this.influence[gaussX][gaussY] - gauss;
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
	
	private void setGameTimer() {
		if (gameStart == 0 && scores[0] > 0) {
			gameStart = System.currentTimeMillis();
		}
	}
	
	private void sleep() {
		try {
			if (this.tokenNum == 1) {
				Thread.sleep(400);
	    	}
			if (this.tokenNum == 2) {
				Thread.sleep(800);
	    	}
	    	if (this.tokenNum == 0) {
	    		Thread.sleep(150);
	    		setDirections(0, 0);
	    		Thread.sleep(100);
	    	}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		float[] dir = new float[2];
		int[] target = new int[2];
		long lastTarget = 0;
		
		while (true) {
			setGameTimer();

			if (System.currentTimeMillis() - lastTarget >= this.targetUpdateRate ||	hitTarget()) {
				findTarget(target);
				setTarget(target[0], target[1]);
				lastTarget = System.currentTimeMillis();
			}
				
			findDirVector(limit(getX()), limit(getY()), target[0], target[1], dir);
			setDirections(dir[0], dir[1]);
			
			sleep();
		}
	}
	
	private void setDirections(float x, float y) {
		directions[this.tokenNum][0] = x;
		directions[this.tokenNum][1] = y;
	}
	
	private void setTarget(int x, int y) {
		this.targets[this.tokenNum][0] = x;
		this.targets[this.tokenNum][1] = y;
	}
	
	private int getTargetX(int tokenNum) {
		return this.targets[tokenNum][0];
	}
	
	private int getTargetX() {
		return this.targets[this.tokenNum][0];
	}
	
	private int getTargetY(int tokenNum) {
		return this.targets[tokenNum][1];
	}
	
	private int getTargetY() {
		return this.targets[this.tokenNum][1];
	}
	
	private boolean hitTarget() {
		return (Math.abs(getTargetX() - getX()) + Math.abs(getTargetY() - getY())) < 2;
	}
	
	private void findTarget(int[] xy) {
		setInfluence();
		getMaxInfluence(xy);
	}
	
	private void getMaxInfluence(int[] xy) {
		float max = -100;
		int scale = 3 + this.playerNum;
		float sum = 0;
		int x = -1;
		int y = -1;
		
		for (int i = 0; i < Constants.DIM - scale; i++) {
			for (int j = 0; j < Constants.DIM - scale; j++) {
				
				for (int l = 0; l < scale; l++) {
					for (int k = 0; k < scale; k++) {
						sum += this.influence[i + l][j + k];
					}
				}
	
				if (this.reachable[i][j] &&
					(sum > max ||
					(sum == max && rnd.nextBoolean()))) {
					max = sum;
					x = i;
					y = j;
				}
				
				sum = 0;
			}
		}
		
		max = -100;
		
		for (int l = 0; l < scale; l++) {
			for (int k = 0; k < scale; k++) {
				if (this.reachable[x + l][y + k] &&
					(this.influence[x + l][y + k] > max ||
					(this.influence[x + l][y + k] == max && rnd.nextBoolean()))) {
					max = this.influence[x + l][y + k];
					xy[0] = x + l;
					xy[1] = y + k;
				}
			}
		}
	}
	
	private void findDirVector(int x, int y, int toX, int toY, float[] vector) {
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
		
		int currentX = (int) getX();
		int currentY = (int) getY();
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
		
		while (inLimit(i, j) &&
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
	
	private boolean inLimit(int num) {
		return num >= 0 && num < Constants.DIM;
	}
	
	private boolean inLimit(float num) {
		return num >= 0 && num < Constants.DIM;
	}
	
	private boolean inLimit(int x, int y) {
		return inLimit(x) && inLimit(y);
	}
	
	private boolean inLimit(float x, float y) {
		return inLimit(x) && inLimit(y);
	}
	
	private int limit(float value) {
	    return (int)Math.max(0, Math.min(value, Constants.DIM));
	}
	
	private float getX() {
		return getX(this.playerNum, this.tokenNum);
	}
	
	private float getY() {
		return getY(this.playerNum, this.tokenNum);
	}
	
	private float getX(int playerNum, int tokenNum) {
		return this.tokens[playerNum][tokenNum][0];
	}
	
	private float getY(int playerNum, int tokenNum) {
		return this.tokens[playerNum][tokenNum][1];
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
				
				if ((int)getX(i, j) == (int)x  &&
				    (int)getY(i, j) == (int)y) {
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
