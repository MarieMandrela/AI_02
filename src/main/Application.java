package main;

import java.io.IOException;

import AI.Client;

public class Application {

	public static void main(String[] args) throws InterruptedException, IOException
	{		
		ClientRunner.runClients();
	}
	
	public static class ClientRunner {
		
		public static void runClients () throws InterruptedException {
			Client one = new Client("one");
			Client two = new Client("two");
			Client three = new Client("three");
			Client four = new Client("four");
			
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
