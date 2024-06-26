package socs.network.message;

import java.io.*;


public class SOSPFPacket implements Serializable {

  //for inter-process communication
  public String srcProcessIP;
  public short srcProcessPort;

  //simulated IP address
  public String srcIP;
  public String dstIP;

  //common header
  public short sospfType; //0 - HELLO, 1 - LinkState Update
  public String routerID;

  //used by HELLO message to identify the sender of the message
  //e.g. when router A sends HELLO to its neighbor, it has to fill this field with its own
  //simulated IP address
  public String neighborID; //neighbor's simulated IP address

  //used by LSAUPDATE
  public LSA lsa = null;

  public SOSPFPacket() {
  }

  public static SOSPFPacket createHello(short srcProcessPort, String srcIP,
                                 String dstIP,
                                 String neighborID) {
    SOSPFPacket packet = new SOSPFPacket();
    packet.srcProcessIP = "127.0.0.1";
    packet.srcProcessPort = srcProcessPort;
    packet.srcIP = srcIP;
    packet.dstIP = dstIP;
    packet.neighborID = neighborID;
    return packet;
  }
}
