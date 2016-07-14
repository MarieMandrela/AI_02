package main;

import java.io.IOException;

import AI.Client;
import AI.Constants;
import AI.DijkstraTest;

public class Application {

	public static void main(String[] args) throws InterruptedException, IOException
	{	
		ClientRunner.runClients();
	}
	
	public static class ClientRunner {
		
		public static void runClients () throws InterruptedException {
			String[] random = {Constants.RNG, Constants.RNG, Constants.RNG};
			String[] lookahead = {Constants.LOOK, Constants.LOOK, Constants.LOOK};
			String[] dijkstra = {Constants.DIJK, Constants.DIJK, Constants.DIJK};
			Client one = new Client("one", lookahead);
			Client two = new Client("two", lookahead);
			Client three = new Client("dijkstra", dijkstra);
			Client four = new Client("three", lookahead);
			
			Thread t_one = new Thread(one);
			Thread t_two = new Thread(two);
			Thread t_three = new Thread(three);
			Thread t_four = new Thread(four);
			
			t_one.start();
			t_two.start();
			t_three.start();
			t_four.start();
		}
	}
}
