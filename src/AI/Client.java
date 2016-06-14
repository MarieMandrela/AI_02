package AI;

import lenz.htw.yakip.ColorChange;
import lenz.htw.yakip.net.NetworkClient;

public class Client implements Runnable {

	private String teamName;
	private int playerNum;
	
	private String[] ais;
	
	private int[][] board = new int[31][31];
	private float[][][] tokens = new float[4][3][2];
	private float[][][] directions = new float[4][3][2];
	private NetworkClient network;
	
	public Client(String teamName, String[] ais) {
		super();
		this.teamName = teamName;
		this.ais = ais;
	}

	@Override
	public void run() {
		network = new NetworkClient(null, teamName);
		playerNum = network.getMyPlayerNumber();
        initWalls();
        checkAllTokens();
        colorChange();
        initTokenAI();

		while (network.isAlive()) { 	        
	        checkAllTokens();
	        colorChange();
	        for (int i = 0; i < 3; i++) {
	        	network.setMoveDirection(i, directions[this.playerNum][i][0], directions[this.playerNum][i][1]);
	        }
		}
	}
	
	private void initTokenAI() {
		TokenAI one = TokenAI.getTokenAI(this.ais[0], playerNum, 0, board, tokens, directions);
		TokenAI two = TokenAI.getTokenAI(this.ais[1], playerNum, 1, board, tokens, directions);
		TokenAI three =  TokenAI.getTokenAI(this.ais[2], playerNum, 2, board, tokens, directions);
		Thread t_one = new Thread(one);
		Thread t_two = new Thread(two);
		Thread t_three = new Thread(three);
		t_one.start();
		t_two.start();
		t_three.start();
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
        		board[x][y] = network.isWall(x, y) ? Constants.WALL : Constants.EMPTY;
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
