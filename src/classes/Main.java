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
    try {
      System.in.read();
    } catch (IOException e) {
      e.printStackTrace();
    }
    emilio.put(sha1("le gustan las flores"), "le gustan las flores");
    mariano.put(sha1("le gustan las flores"), "le gustan las flores");
    stoika.put(sha1("le gustan las flores"), "le gustan las flores");
  }

}
