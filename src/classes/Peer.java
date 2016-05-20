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
import java.util.concurrent.Semaphore;


public class Peer implements Runnable
{
	static final int k = 2;
	static final int alpha = 3;
	static final int bucketLength = 160;

	byte[] myGuid;
	byte[] getNode;
	Map<byte[], String> hashTable;
	byte[][][] kBucket;
	String value;

	String ip;
	int port;
	InetAddress group;
	MulticastSocket socket;
	Semaphore getNodeSem;
	Semaphore getValueSem;

	
	public Peer(String identificador, String ip, int port)
	{
		this.myGuid = sha1(identificador);
		this.getNode = sha1(identificador);
		this.hashTable = new HashMap<>();
		this.kBucket = new byte[bucketLength][k][];
		this.kBucket[bucketLength - 1][0] = this.myGuid;
		this.value = "";
		this.ip = ip;
		this.port = port;
		this.getNodeSem = new Semaphore(1, true);
		this.getValueSem = new Semaphore(1, true);

		try {
			this.group = InetAddress.getByName(this.ip);
			this.socket = new MulticastSocket(this.port);
//			this.socket.setLoopbackMode(true);
			this.socket.joinGroup(this.group);
			this.getNodeSem.acquire();
			this.getValueSem.acquire();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	public synchronized byte[] put(byte[] key, String value)
	{
		byte[] guid = getNode(key);
		
		if (Arrays.equals(guid, myGuid)) {
			hashTable.put(key, value);
			return guid;
		}
		
		try {
			String msg = Base64.getEncoder().encodeToString(myGuid) 
					+ ":node:"
					+ Base64.getEncoder().encodeToString(guid)
					+ ":Q:"
					+ Base64.getEncoder().encodeToString(key) 
					+ ":end";
			socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, port));
			getNodeSem.acquire();
			guid = getNode;
			getNodeSem.release();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			String msg = Base64.getEncoder().encodeToString(myGuid) 
					+ ":put:"
					+ Base64.getEncoder().encodeToString(guid) 
					+ ":"
					+ Base64.getEncoder().encodeToString(key)
					+ ":"
					+ value
					+ ":end";
			socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return guid;
	}

	
	public synchronized String get(byte[] key)
	{
		String value = "";
		byte[] guid = getNode(key);
		
		if (Arrays.equals(guid, myGuid)) {
			value = hashTable.get(key);
			return value;
		}
		
		try {
			String msg = Base64.getEncoder().encodeToString(myGuid) 
					+ ":node:"
					+ Base64.getEncoder().encodeToString(guid)
					+ ":Q:"
					+ Base64.getEncoder().encodeToString(key) 
					+ ":end";
			socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, port));
			getNodeSem.acquire();
			guid = getNode;
			getNodeSem.release();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		try {
			String msg = Base64.getEncoder().encodeToString(myGuid) 
					+ ":get:"
					+ Base64.getEncoder().encodeToString(guid) 
					+ ":Q:"
					+ Base64.getEncoder().encodeToString(key)
					+ ":end";
			socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, port));
			getValueSem.acquire();
			value = this.value;
			getValueSem.release();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		return value;
	}

	
	private int distance(byte[] key, byte[] guid)
	{
		int d = -1;
//		System.out.println("key:" + key.length + ", guid: " + guid.length);

		if (key.length != guid.length) {
			return d;
		}
		
		BitSet keyBit = BitSet.valueOf(key);
		BitSet guidBit = BitSet.valueOf(guid);

		for (int i = 0; i < 160; i++) {
//			System.out.println("key:" + keyBit.get(i) + ", guid: " + guidBit.get(i));
			d++;
			if (keyBit.get(i) != guidBit.get(i)) {
				break;
			}
		}

//		System.out.println("distance: " + d);

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

		while (guid == null) {
			guid = kBucket[d][0];
			if (guid == null) {
				guid = kBucket[d][1];
			}
			d++;
		}

		return guid;
	}
	
//	private byte[][] getNodes(byte[] key)
//	{
//		byte[][] guid = new byte[alpha][];
//
//		for (int i = 0; i < alpha; i++) {
//			int d = distance(key, myGuid);
//			while (guid == null) {
//				guid = kBucket[d][0];
//				d++;
//			}
//		}
//		
//		return guid;
//	}

	
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
				System.out.println(Base64.getEncoder().encodeToString(myGuid) + " added "
						+ Base64.getEncoder().encodeToString(guid));
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
				System.out.println(Base64.getEncoder().encodeToString(myGuid) + " deleted "
						+ Base64.getEncoder().encodeToString(guid));
				break;
			}
		}

		return;
	}

	
	public void run()
	{
		byte[] prevNode = myGuid;
		
//		System.out.println(Base64.getEncoder().encodeToString(myGuid) + " running");

		try {
			String msg = Base64.getEncoder().encodeToString(myGuid) 
					+ ":hi"
					+ ":end";
			socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, port));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		byte[] buf = new byte[1024];

		while (true) {
			DatagramPacket recv = new DatagramPacket(buf, buf.length);

			try {
//				System.out.println(Base64.getEncoder().encodeToString(myGuid) + " waiting for msg");
				socket.receive(recv);
//				System.out.println(Base64.getEncoder().encodeToString(myGuid) + " msg recived");

				String cmd[] = (new String(recv.getData())).split(":");

				if (cmd.length < 2) {
					continue;
				}

				if (Arrays.equals(Base64.getDecoder().decode(cmd[0]), myGuid)) {
//					System.out.println(Base64.getEncoder().encodeToString(myGuid) + " my own msg");
					continue;
				}

//				System.out.println(cmd[0] + " said " + cmd[1]);

				switch (cmd[1]) {
				case "hi":
					putIntoBucket(Base64.getDecoder().decode(cmd[0]));
					String msg = Base64.getEncoder().encodeToString(myGuid) 
							+ ":sup"
							+ ":end";
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
					
				case "put":
					if (!Arrays.equals(Base64.getDecoder().decode(cmd[2]), myGuid)) {
						break;
					}
					System.out.println(Base64.getEncoder().encodeToString(myGuid) + " is putting");
					hashTable.put(Base64.getDecoder().decode(cmd[3]), cmd[4]);
					break;
					
				case "get":
					if (!Arrays.equals(Base64.getDecoder().decode(cmd[2]), myGuid)) {
						break;
					}
					break;
					
				case "node":
					if (!Arrays.equals(Base64.getDecoder().decode(cmd[2]), myGuid)) {
						break;
					}
					switch (cmd[3]) {
					case "Q":
						System.out.println(Base64.getEncoder().encodeToString(myGuid) + " asked for node");
						byte[] node = getNode(Base64.getDecoder().decode(cmd[4]));
						try {
							msg = Base64.getEncoder().encodeToString(myGuid) 
									+ ":node:"
									+ cmd[0]
									+ ":A:"
									+ cmd[4]
									+ ":"
									+ Base64.getEncoder().encodeToString(node) 
									+ ":end";
							socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, port));
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
						
					case "A":
						System.out.println(Base64.getEncoder().encodeToString(myGuid) + " get the node");
						if (Arrays.equals(getNode, Base64.getDecoder().decode(cmd[5]))) {
							getNodeSem.release();
							break;
						}
						
						if (Arrays.equals(prevNode, Base64.getDecoder().decode(cmd[5]))) {
							getNode = prevNode;
							getNodeSem.release();
							break;
						}
						
						prevNode = Base64.getDecoder().decode(cmd[0]);
						getNode = Base64.getDecoder().decode(cmd[5]);
						
						try {
							msg = Base64.getEncoder().encodeToString(myGuid) 
									+ ":node:"
									+ cmd[5]
									+ ":Q:"
									+ cmd[4] 
									+ ":end";
							socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, port));
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
						
					default:
						break;
						
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
			String msg = Base64.getEncoder().encodeToString(myGuid) 
					+ ":bye"
					+ ":end";
			socket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, group, port));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
