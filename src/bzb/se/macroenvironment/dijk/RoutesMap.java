package bzb.se.macroenvironment.dijk;

import java.util.List;

/**
 * This interface defines the object storing the graph of all routes in the
 * system.
 * 
 * @author Renaud Waldura &lt;renaud+tw@waldura.com&gt;
 * @version $Id: RoutesMap.java,v 1.1 2008/10/22 13:06:33 bzb Exp $
 */

public interface RoutesMap
{
	/**
	 * Enter a new segment in the graph.
	 */
	public void addDirectRoute(String start, String end, int distance);
	
	/**
	 * Get the value of a segment.
	 */
	public int getDistance(String start, String end);
	
	/**
	 * Get the list of cities that can be reached from the given city.
	 */
	public List getDestinations(String city); 
	
	/**
	 * Get the list of cities that lead to the given city.
	 */
	public List getPredecessors(String city);
	
	/**
	 * @return the transposed graph of this graph, as a new RoutesMap instance.
	 */
	public RoutesMap getInverse();
}
