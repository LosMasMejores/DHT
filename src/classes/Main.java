package classes;

import java.io.IOException;


public class Main {

	
	public static void main(String[] args) {
		Peer javier = new Peer("javier", "228.5.6.7", 6789);
		System.out.println("javier created");
		Peer emilio = new Peer("emilio", "228.5.6.7", 6789);
		System.out.println("emilio created");
		Peer mariano = new Peer("mariano", "228.5.6.7", 6789);
		System.out.println("mariano created");
		
		(new Thread(javier)).start();
		System.out.println("javier started");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		(new Thread(emilio)).start();
		System.out.println("emilio started");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		(new Thread(mariano)).start();
		System.out.println("mariano started");
	}

}
