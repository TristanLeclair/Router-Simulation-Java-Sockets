package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LinkStateDatabase {

  // linkID => LSAInstance
  public HashMap<String, LSA> _store = new HashMap<String, LSA>();

  private RouterDescription rd = null;

  public LinkStateDatabase(RouterDescription routerDescription) {
    rd = routerDescription;
    LSA l = initLinkStateDatabase();
    _store.put(l.linkStateID, l);
  }

  /**
   * Output the shortest path from this router to the destination with the given
   * IP address
   * 
   * @param destinationIP The IP address of the destination router
   */
  public String getShortestPath(String destinationIP) {
    // Check if router exists in network
    RouterDescription destinationRouter = null;
    // Find the destination router description
    synchronized (_store) {
      for (LSA lsa : _store.values()) {
        for (LinkDescription ld : lsa.links) {
          if (ld.linkID.equals(destinationIP)) {
            destinationRouter = new RouterDescription();
            destinationRouter.simulatedIPAddress = destinationIP;
            break;
          }
        }
        if (destinationRouter != null) {
          break;
        }
      }
      if (destinationRouter == null) {
        return "Destination router not found in the network.";
      }
    }
    // Call Dijkstra's algorithm to find the shortest path
    Map<String, Integer> distances = dijkstra(rd, destinationRouter);

    // Reconstruct the shortest path
    StringBuilder shortestPath = new StringBuilder();
    // shortestPath.append("Shortest path from
    // ").append(rd.simulatedIPAddress).append(" to
    // ").append(destinationIP).append(": ");
    String currentRouter = rd.simulatedIPAddress;
    while (!currentRouter.equals(destinationIP)) {
      shortestPath.append(currentRouter).append(" -> ");
      int minDistance = Integer.MAX_VALUE;
      String nextRouter = null;
      for (Map.Entry<String, Integer> entry : distances.entrySet()) {
        if (entry.getKey().equals(currentRouter)) {
          continue;
        }
        if (entry.getValue() < minDistance) {
          minDistance = entry.getValue();
          nextRouter = entry.getKey();
        }
      }
      currentRouter = nextRouter;
    }
    shortestPath.append(destinationRouter.simulatedIPAddress);
    return shortestPath.toString();
  }

  /**
   * Dijkstra's algorithm to find the shortest path from source to destination
   * 
   * @param source      the source router
   * @param destination the destination router
   * @return a map of router IP addresses to the distance from the source router
   */
  private synchronized Map<String, Integer> dijkstra(RouterDescription source, RouterDescription destination) {
    Map<String, Integer> distances = new HashMap<>();
    Map<String, String> previous = new HashMap<>();
    Set<String> visited = new HashSet<>();

    for (LSA lsa : _store.values()) {
      distances.put(lsa.linkStateID, Integer.MAX_VALUE);
      previous.put(lsa.linkStateID, null);
    }
    distances.put(source.simulatedIPAddress, 0);

    // visited.add(source.simulatedIPAddress);

    // Loop through all routers in the network
    while (visited.size() < _store.size()) {
      String current = getMinimumDistanceRouter(distances, visited);

      if (current == null || distances.get(current) == Integer.MAX_VALUE) {
        break;
      }

      visited.add(current);

      // Process each link of the current router
      for (LinkDescription ld : _store.get(current).links) {
        String neighbor = ld.linkID;
        if (!visited.contains(neighbor)) {
          int alt = distances.get(current) + 1; // Assuming weight is 1 for each link
          if (alt < distances.get(neighbor)) {
            distances.put(neighbor, alt);
            previous.put(neighbor, current);
          }
        }
      }
    }

    return distances;
  }

  /**
   * Get the router with the minimum distance that has not been visited
   * 
   * @param distances The map of router IP addresses to distances
   * @param visited   The set of visited routers
   */
  private String getMinimumDistanceRouter(Map<String, Integer> distances, Set<String> visited) {
    String minRouter = null;
    int minDistance = Integer.MAX_VALUE;
    for (Map.Entry<String, Integer> entry : distances.entrySet()) {
      if (!visited.contains(entry.getKey()) && entry.getValue() < minDistance) {
        minDistance = entry.getValue();
        minRouter = entry.getKey();
      }
    }
    return minRouter;
  }

  /**
   * initialize the linkstate database by adding an entry about the router itself
   * 
   * @return the LSA instance containing the information about the router
   */
  private LSA initLinkStateDatabase() {
    LSA lsa = new LSA();
    lsa.linkStateID = rd.simulatedIPAddress;
    lsa.lsaSeqNumber = Integer.MIN_VALUE;
    LinkDescription ld = new LinkDescription();
    ld.linkID = rd.simulatedIPAddress;
    ld.portNum = -1;
    lsa.links.add(ld);
    return lsa;
  }

  /**
   * Sync the link state database with the given LSA
   * If the LSA already exists, update it if the sequence number is larger
   * If the LSA does not exist, add it to the database
   * 
   * @param lsa The LSA to update the database with
   */
  public void syncLinkStateDatabase(LSA lsa) {
    // Potentially lock on the lsa object instead of the whole store
    synchronized (_store) {
      if (_store.containsKey(lsa.linkStateID)) {
        // Update the LSA if the sequence number is larger
        LSA existingLsa = _store.get(lsa.linkStateID);
        if (lsa.lsaSeqNumber > existingLsa.lsaSeqNumber) {
          _store.put(lsa.linkStateID, lsa);
        }
      } else {
        // Add the LSA if it does not exist
        _store.put(lsa.linkStateID, lsa);
      }
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (LSA lsa : _store.values()) {
      sb.append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\t");
      for (LinkDescription ld : lsa.links) {
        sb.append(ld.linkID).append(",").append(ld.portNum).append("\t");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
