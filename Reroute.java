/**
 * The following code implements a manner of fast reroute in a simulated network.
 * Djistra's gives the shortest path initially but after a node breaks, it gives
 * a local optimum between then the node preceding and succeeding the broken node.
 * This may result in a less than optimal path between the final source and destination 
 * but takes less time to calculate and hence is faster. This has the final result of
 * the network connecting back as soon as possible using the fast reroute method.
 *
 */


import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;

public class Reroute extends JFrame{
	
	final int WIDTH = 1080;			// width of window
	final int HEIGHT = 720;			// height of window
	final int TOTAL_NODES = 30;		// total number of network nodes
	final int TOT_NEIGHBORS = 4;	// approximate number of neighbor nodes connected to each node.
	final int RADIUS = 8;			// radius of each node circle on window
	String BROKEN_NODE = "";
	Map<String, ArrayList<String>> neighbours;	// maps each node to it's list of neighbors
	Map<String, int[]> allNodes;				// maps each node to it's coordinates
	ArrayList<String> path = null;	// stores final path to be displayed
	
	public static void main(String[] args) throws IOException {
		
		// generates the random nodes and calculates it's neighbors and draws map
		Reroute reroute = new Reroute(); 
		reroute.allNodes = new HashMap<String, int[]>();
		reroute.neighbours = new HashMap<String, ArrayList<String>>();
		reroute.generatesNodes();
		reroute.drawMap();
		
		
		// takes in source and destination and draws map with shortest route
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter source: ");
		String src = br.readLine();
		System.out.println("Enter destination: ");
		String dst = br.readLine();
		reroute.path = reroute.djikstra(src, dst);
		reroute.drawMap();
		
		
		// takes in broken node and calculates shortest route between
		// the nodes preceding and succeeding the broken node
		System.out.println("Enter Broken Node: ");
		reroute.BROKEN_NODE = br.readLine();
		int broken = reroute.path.indexOf(reroute.BROKEN_NODE);
		ArrayList<String> newPath = reroute.djikstra(reroute.path.get(broken - 1), reroute.path.get(broken + 1));
		
		newPath.remove(newPath.size() - 1);	// remove dst because it is already in path
		reroute.path.remove(broken);
		
		// add newly calculated path to original path, taking care of trace back path
		while(newPath.size() > 0){	//reverse traversal
			String node = newPath.remove(newPath.size() - 1);
			if(! reroute.path.contains(node))
				reroute.path.add(broken, node);
			else{
				int tracebackNode = reroute.path.indexOf(node);
				if(tracebackNode > broken)		// means nodes around dst of newPath should be skipped
					reroute.path.remove(broken);
				else{
					for(int i=1; i < broken-tracebackNode; i++)
						reroute.path.remove(tracebackNode+1);	// skips nodes around src of newPath
					break;
				}	
			}
		}
		// draw fast reroute map
		reroute.drawMap();
	}
	
	
	// Djikstra's algorithm gives shortest path between src and dst
	private ArrayList<String> djikstra(String src, String dst){
		Map<String, Integer> minDist = new HashMap<String, Integer>();		//seen but not visited
		Map<String, String> visitedFrom = new HashMap<String, String>();	//key visited from value
		
		String currentNode = src;
		int totCost = 0;
		while(true){
			if(currentNode.equals(dst))
				break;
			double min = WIDTH*HEIGHT;	//initially assign a large value to min
			for(String key:neighbours.get(currentNode)){
				if(key.equals(src) || key.equals(BROKEN_NODE) || (visitedFrom.containsKey(key) && !minDist.containsKey(key)))
					continue;
				int dist =(int) getDistance(currentNode, key, allNodes);
				if(!( minDist.containsKey(key) && (totCost+dist > minDist.get(key)))){
					minDist.put(key, totCost+dist);
					//key is visited from current node
					visitedFrom.put(key, currentNode);
				}	
			}
			for(String key:minDist.keySet()){
				if(minDist.get(key) < min){
					min = totCost = minDist.get(key);
					currentNode = key;
				}
			}
			minDist.remove(currentNode);
		}
		return calcPath(src, dst, visitedFrom);
	}
	
	// gives final path between src and dst. Method called by Djikstra's
	private ArrayList<String> calcPath(String src, String dst, Map<String, String> visitedFrom){
		String currentNode = dst;
		ArrayList<String> path = new ArrayList<String>();
		while(true){
			path.add(currentNode);
			if(currentNode.equals(src))
				break;
			currentNode = visitedFrom.get(currentNode);
		}
		Collections.reverse(path);
		return path;
	}
	
	// get euclidean distance between loc1 and loc2 based on the coordinates
	private double getDistance(String loc1, String loc2, Map<String, int[]> nodes){
		return Math.sqrt(Math.pow(nodes.get(loc2)[1] - nodes.get(loc1)[1], 2)
				+ Math.pow(nodes.get(loc2)[0] - nodes.get(loc1)[0], 2));
	}
	
	// generate random nodes
	private void generatesNodes(){
		Random rand = new Random(); 
		for(int i=0; i<=TOTAL_NODES; i++){
			String nodeName = "Node"+i;
			allNodes.put(nodeName, new int[]{rand.nextInt(WIDTH *9/10)+4*RADIUS, rand.nextInt(HEIGHT *9/10)+4*RADIUS});
			neighbours.put(nodeName, new ArrayList<String>());
		}
		
		//Compute neighbors
		if(TOTAL_NODES <= TOT_NEIGHBORS){
			System.err.println("Insufficient nodes");
			System.exit(ERROR);
		}
		for(String currentNode: allNodes.keySet()){
			ArrayList<String> connections = new ArrayList<String>();	//list of potential neighbors
			ArrayList<Integer> distances = new ArrayList<Integer>();	//and their distances
			for(String aNode: allNodes.keySet()){
				if(currentNode.equals(aNode))
					continue;
				
				int dist = (int) getDistance(currentNode, aNode, allNodes);
				int start=0, end = connections.size();
				// add aNode in sorted order
				while(start <= end){
		            int mid = start + (end - start)/2;
		            if(start == end){
						connections.add(start, aNode);
						distances.add(start, dist);
						break;
					}
		            else if(distances.get(mid) < dist)
		            	start = mid+1;
		            else
		            	end = mid;
		        }
			}
			for(int i=0; i<TOT_NEIGHBORS; i++){
				String aNode = connections.get(i);
				if(!neighbours.get(currentNode).contains(aNode))
					neighbours.get(currentNode).add(aNode);
				if(!neighbours.get(aNode).contains(currentNode))
					neighbours.get(aNode).add(currentNode);
			}
		}
	}
	
	// draws window
	public void paint(Graphics g){
        //getContentPane().setBackground(Color.YELLOW);

		for(String key: allNodes.keySet()){
			int x = allNodes.get(key)[0];// - radius;
			int y = allNodes.get(key)[1];// - radius;
			g.setColor(Color.BLUE);
			if(BROKEN_NODE.equals(key))
				g.setColor(Color.RED);
			if(path != null && path.contains(key))
				g.setColor(Color.GREEN);
			g.fillOval(x - RADIUS, y - RADIUS, RADIUS*2, RADIUS*2);
			g.setColor(Color.DARK_GRAY);
			g.drawString(key, x + RADIUS, y + RADIUS);
			
			
			for(String neighbor: neighbours.get(key)){
				g.setColor(Color.BLACK);
				if(path != null && path.contains(key) && path.contains(neighbor) 
						&& Math.abs(path.indexOf(key) - path.indexOf(neighbor)) == 1)
					g.setColor(Color.GREEN);
				g.drawLine(x, y, allNodes.get(neighbor)[0], allNodes.get(neighbor)[1]);
			}
		}
	}

	// helper method for paint
	private void drawMap(){
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
        //repaint();
	}
}