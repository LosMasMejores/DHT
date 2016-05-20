package classes;


public class Main {


  public static void main(String[] args) {
    Peer javier = new Peer("javier", "228.5.6.7", 6789);
    System.out.println("javier created");
    Peer emilio = new Peer("emilio", "228.5.6.7", 6789);
    System.out.println("emilio created");
    Peer mariano = new Peer("mariano", "228.5.6.7", 6789);
    System.out.println("mariano created");
    Peer stoika = new Peer("stoika", "228.5.6.7", 6789);
    System.out.println("stoika created");
    Peer fulana = new Peer("fulana", "228.5.6.7", 6789);
    System.out.println("fulana created");
    Peer lina = new Peer("lina", "228.5.6.7", 6789);
    System.out.println("lina created");

    (new Thread(javier)).start();
    System.out.println("javier started");
    (new Thread(emilio)).start();
    System.out.println("emilio started");
    (new Thread(mariano)).start();
    System.out.println("mariano started");
    (new Thread(stoika)).start();
    System.out.println("stoika started");
    (new Thread(fulana)).start();
    System.out.println("fulana started");
    (new Thread(lina)).start();
    System.out.println("lina started");
    
//    try {
//		Thread.sleep(1000);
//	} catch (InterruptedException e) {
//		e.printStackTrace();
//	}

    emilio.put(Peer.sha1("1"), "le gustan las flores");
    mariano.put(Peer.sha1("2"), "le gustan los penes");
    javier.put(Peer.sha1("1"), "le gustan las flores");
    
    try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
    
    System.out.println("emilio response: " + emilio.get(Peer.sha1("1")));
    System.out.println("lina response: " + lina.get(Peer.sha1("2")));
  }

}
