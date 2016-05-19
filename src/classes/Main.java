package classes;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Main {

	
	static public byte[] sha1(String value)
	{
		byte[] key = null;

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(value.getBytes());
			key = md.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return key;
	}
	
	
	public static void main(String[] args) {
		Peer javier = new Peer("javier", "228.5.6.7", 6789);
		System.out.println("javier created");
		Peer emilio = new Peer("emilio", "228.5.6.7", 6789);
		System.out.println("emilio created");
		Peer mariano = new Peer("mariano", "228.5.6.7", 6789);
		System.out.println("mariano created");
		
		(new Thread(javier)).start();
		System.out.println("javier started");
		(new Thread(emilio)).start();
		System.out.println("emilio started");
		(new Thread(mariano)).start();
		System.out.println("mariano started");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		emilio.put(sha1("le gustan los penes"), "le gustan los penes");
	}

}
