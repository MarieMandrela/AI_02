package AI;

import java.util.Random;

import lenz.htw.yakip.ColorChange;
import lenz.htw.yakip.net.NetworkClient;

public class Client implements Runnable {

	private String teamName;
	private int playerNum;
	/**
	 * -1 = empty field
	 * -2 = wall
	 *  0 = colored by player zero
	 *  1 = colored by player one
	 *  2 = colored by player two
	 *  3 = colored by player three
	 */
	private int[][] board = new int[31][31];
	private float[][][] tokens = new float[4][3][2];
	private NetworkClient network;
	
	
	public Client(String teamName) {
		super();
		this.teamName = teamName;
	}

	@Override
	public void run() {
		network = new NetworkClient(null, teamName);
		Random rnd = new Random();
		rnd.setSeed(123);
		playerNum = network.getMyPlayerNumber();
        initWalls();

		while (network.isAlive()) {
	        
		    for (int i = 0; i < 3; ++i) {
		        network.setMoveDirection(i, rnd.nextFloat() - 0.5f, rnd.nextFloat() - 0.5f);
		        
		        checkAllTokens();
		        colorChange();
		    }
		}
	}
	
	private void colorChange() {
		ColorChange cc;
        while ((cc = network.getNextColorChange()) != null) {
            board[cc.x][cc.y] = cc.newColor;
        }
	}
	
	private void initWalls() {
        for (int x = 0; x < 31; ++x) {
        	for (int y = 0; y < 31; ++y) {
        		board[x][y] = network.isWall(x, y) ? -2 : -1;
        	}
        }
	}
	
	private void checkAllTokens() {
		for (int i = 0; i < 4; ++i) {
			for (int j = 0; j < 3; ++j) {
				tokens[i][j][0] = network.getX(i, j);
				tokens[i][j][1] = network.getY(i, j);
			}
		}
	}

}
