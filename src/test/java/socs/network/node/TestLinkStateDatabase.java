package socs.network.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

public class TestLinkStateDatabase {

  @Test
  public void testGetShortestPath() {
    // Create a router description for the source router
    RouterDescription sourceRouter = new RouterDescription();
    sourceRouter.simulatedIPAddress = "192.168.0.1";

    // Create a router description for the destination router
    RouterDescription destinationRouter = new RouterDescription();
    destinationRouter.simulatedIPAddress = "192.168.0.2";

    // Create a link state database
    LinkStateDatabase linkStateDatabase = new LinkStateDatabase(sourceRouter);

    // Add link descriptions for source router
    LinkDescription linkDescription1 = new LinkDescription();
    linkDescription1.linkID = "192.168.0.2"; // Destination IP
    linkDescription1.portNum = 1; // Example port number
    linkStateDatabase._store.get(sourceRouter.simulatedIPAddress).links.add(linkDescription1);

    // Add link descriptions for destination router
    LinkDescription linkDescription2 = new LinkDescription();
    linkDescription2.linkID = "192.168.0.1"; // Source IP
    linkDescription2.portNum = 2; // Example port number
    LSA lsa = new LSA();
    lsa.linkStateID = destinationRouter.simulatedIPAddress;
    lsa.links.add(linkDescription2);
    linkStateDatabase._store.put(destinationRouter.simulatedIPAddress, lsa);
    linkStateDatabase._store.get(destinationRouter.simulatedIPAddress).links.add(linkDescription2);

    // Test shortest path
    String shortestPath = linkStateDatabase.getShortestPath(destinationRouter.simulatedIPAddress);

    // Assert expected shortest path
    assertEquals("Shortest path is not as expected", "192.168.0.1 -> 192.168.0.2", shortestPath);
  }
}