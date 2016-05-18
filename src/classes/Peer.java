package classes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class Peer implements Runnable
{
	static int k = 2;
	static int alpha = 3;
	static int bucketLength = 160;

	byte[] myGuid;
	Map<byte[], String> hashTable;
	byte[][][] kBucket;

	String host;
	int port;
	InetAddress group;
	MulticastSocket socket;

	
	public Peer(String identificador, String host, int port)
	{
		this.myGuid = sha1(identificador);
		this.hashTable = new HashMap<>();
		this.kBucket = new byte[bucketLength][k][];
		this.host = host;
		this.port = port;

		try {
			this.group = InetAddress.getByName(this.host);
			this.socket = new MulticastSocket(this.port);
			this.socket.joinGroup(this.group);
//			this.socket.setLoopbackMode(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public byte[] put(byte[] key, String value)
	{
		byte[] guid = getNode(key);

		return guid;
	}

	
	public String get(byte[] key)
	{
		String value = hashTable.get(key);

		if (value == null) {
		}

		return value;
	}

	
	private int distance(byte[] key, byte[] guid)
	{
		int d = -1;
		BitSet keyBit = BitSet.valueOf(key);
		BitSet guidBit = BitSet.valueOf(guid);

		System.out.println("key:" + key.length + ", guid: " + guid.length);
		
		if (key.length != guid.length) {
			System.out.println("ops");
			return d;
		}

		d++;

		for (int i = 0; i < keyBit.length(); i++) {
			System.out.println("key:" + keyBit.get(i) + ", guid: " + guidBit.get(i));
			if (keyBit.get(i) != guidBit.get(i)) {
				break;
			}
			d++;
		}
		
		System.out.println("distance: " + d);

		return d;
	}

	
	private byte[] sha1(String value)
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
	
	
	private byte[] getNode(byte[] key)
	{
		byte[] guid = null;
		int d = distance(key, myGuid);

		if (d == bucketLength) {
			return myGuid;
		}
		
		while (guid == null && d < bucketLength) {
			guid = kBucket[d][0];
			d++;
		};

		if (d == bucketLength) {
			return myGuid;
		}

		return guid;
	}

	
	private void putIntoBucket(byte[] guid)
	{
		int d = distance(guid, myGuid);

		for (int i = 0; i < k; i++) {
			if (Arrays.equals(kBucket[d][i], guid)) {
				return;
			}
		}
		
		for (int i = 0; i < k; i++) {
			if (kBucket[d][i] == null) {
				kBucket[d][i] = guid;
				System.out.println(Base64.getEncoder().encodeToString(myGuid) + " added " + Base64.getEncoder().encodeToString(guid));
				break;
			}
		}

		return;
	}

	
	private void getOutOfBucket(byte[] guid)
	{
		int d = distance(guid, myGuid);

		for (int i = 0; i < k; i++) {
			if (Arrays.equals(kBucket[d][i], guid)) {
				kBucket[d][i] = null;
				System.out.println(Base64.getEncoder().encodeToString(myGuid) + " deleted " + Base64.getEncoder().encodeToString(guid));
				break;
			}
		}

		return;
	}

	
	public void run()
	{
		System.out.println(Base64.getEncoder().encodeToString(myGuid) + " running");

		try {
			String msg = Base64.getEncoder().encodeToString(myGuid) + ":hi:end";
			socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, port));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		byte[] buf = new byte[1024];

		while (true) {
			DatagramPacket recv = new DatagramPacket(buf, buf.length);

			try {
				System.out.println(Base64.getEncoder().encodeToString(myGuid) + " waiting for msg");
				socket.receive(recv);
				System.out.println(Base64.getEncoder().encodeToString(myGuid) + " msg recived");

				String cmd[] = (new String(recv.getData())).split(":");
				
				if (cmd.length < 2) {
					continue;
				}
				
				if (Arrays.equals(Base64.getDecoder().decode(cmd[0]), myGuid)) {
					System.out.println(Base64.getEncoder().encodeToString(myGuid) + " my own msg");
					continue;
				}
				
				System.out.println(cmd[0] + " said " + cmd[1]);

				switch (cmd[1]) {
				case "hi":
					putIntoBucket(Base64.getDecoder().decode(cmd[0]));
					String msg = Base64.getEncoder().encodeToString(myGuid) + ":sup:end";
					socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, port));
					break;
				case "sup":
					putIntoBucket(Base64.getDecoder().decode(cmd[0]));
					break;
				case "bye":
					getOutOfBucket(Base64.getDecoder().decode(cmd[0]));
					break;
				case "ping":
					break;
//				case "get":
//					if (Arrays.equals(Base64.getDecoder().decode(cmd[2]), myGuid)) {
//						if (cmd[3] == "petition") {
//							get(cmd[4].getBytes());
//						}
//						if (cmd[3] == "response") {
//							System.out.println(cmd[3]);
//						}
//					}
//					break;
				case "put":
					if (Arrays.equals(Base64.getDecoder().decode(cmd[2]), myGuid)) {
						put(cmd[3].getBytes(), cmd[3]);
					}
					break;
				default:
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		
		try {
			String msg = Base64.getEncoder().encodeToString(myGuid) + ":bye:end";
			socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, port));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
