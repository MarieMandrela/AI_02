package main;

import java.io.IOException;

import AI.Client;
import AI.Constants;

public class Application {

	public static void main(String[] args) throws InterruptedException, IOException
	{		
		ClientRunner.runClients();
	}
	
	public static class ClientRunner {
		
		public static void runClients () throws InterruptedException {
			String[] random = {Constants.RNG, Constants.RNG, Constants.RNG};
			String[] lookahead = {Constants.LOOK, Constants.LOOK, Constants.LOOK};
			Client one = new Client("one", random);
			Client two = new Client("two", random);
			Client three = new Client("three", random);
			Client four = new Client("lookahead", lookahead);
			
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
