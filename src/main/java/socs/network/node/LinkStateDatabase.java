package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LinkStateDatabase {

  //linkID => LSAInstance
  public HashMap<String, LSA> _store = new HashMap<String, LSA>();

  private RouterDescription rd = null;

  public LinkStateDatabase(RouterDescription routerDescription) {
    rd = routerDescription;
    LSA l = initLinkStateDatabase();
    _store.put(l.linkStateID, l);
  }

  /**
   * output the shortest path from this router to the destination with the given IP address
   */
  public String getShortestPath(String destinationIP) {
    // Check if router exists in network
    RouterDescription destinationRouter = null;
    // Find the destination router description
    synchronized (_store){
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
    // shortestPath.append("Shortest path from ").append(rd.simulatedIPAddress).append(" to ").append(destinationIP).append(": ");
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

  // Dijkstra's algorithm to find the shortest path
  private synchronized Map<String, Integer> dijkstra(RouterDescription source, RouterDescription destination) {
      Map<String, Integer> distances = new HashMap<>();
      Map<String, String> previous = new HashMap<>();
      Set<String> visited = new HashSet<>();

      for (LSA lsa : _store.values()) {
          String router = lsa.linkStateID;
          // Check if router is neighbor of source, then we know the distance
          if (lsa.links.stream().anyMatch(ld -> ld.linkID.equals(source.simulatedIPAddress))) {
            // double check if the router is the source
            if (router.equals(source.simulatedIPAddress)) {
              distances.put(router, 0);
              previous.put(router, null);
            } else {
              distances.put(router, 1);
              previous.put(router, source.simulatedIPAddress);
            }
          } else {
            distances.put(router, Integer.MAX_VALUE);
            previous.put(router, null);
          }
      }

      visited.add(source.simulatedIPAddress);

      // Loop through all routers in the network
      while (visited.size() < _store.size()){
          String current = getMinimumDistanceRouter(distances, visited);
          
          if (current == null) {
              break;
          }

          visited.add(current);

          for (LSA lsa : _store.values()) {
              if (current.equals(lsa.linkStateID)) {
                  for (LinkDescription ld : lsa.links) {
                      String neighbor = null;
                      neighbor = ld.linkID;
                      if (!visited.contains(neighbor)) {
                          int alt = distances.get(current) + 1; // Assuming equal weight for all links
                          if (alt < distances.get(neighbor)) {
                              distances.put(neighbor, alt);
                              previous.put(neighbor, current);
                          }
                      }
                  }
              }
          }
      }

      return distances;
  }

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


  //initialize the linkstate database by adding an entry about the router itself
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


  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (LSA lsa: _store.values()) {
      sb.append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\t");
      for (LinkDescription ld : lsa.links) {
        sb.append(ld.linkID).append(",").append(ld.portNum).append("\t");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
