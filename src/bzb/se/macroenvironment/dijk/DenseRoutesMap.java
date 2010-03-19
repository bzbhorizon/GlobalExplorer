package bzb.se.macroenvironment.dijk;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * This map stores routes in a matrix, a nxn array. It is most useful when
 * there are lots of routes, otherwise using a sparse representation is
 * recommended.
 * 
 * @author Renaud Waldura &lt;renaud+tw@waldura.com&gt;
 * @version $Id: DenseRoutesMap.java,v 1.1 2008/10/22 13:06:33 bzb Exp $
 */

public class DenseRoutesMap
	implements RoutesMap
{
	public final int[][] distances;
	private Hashtable poiIndexes;
	
	public DenseRoutesMap(Hashtable poiIndexes)
	{
		this.poiIndexes = poiIndexes;
		distances = new int[poiIndexes.size()][poiIndexes.size()];
	}
	
	private int getIndex (String poiId) {
		return ((Integer)poiIndexes.get(poiId)).intValue();
	}
	
	private String getName (int poiIndex) {
		Enumeration e = poiIndexes.keys();
		String poiId;
		while (e.hasMoreElements()) {
			poiId = (String) e.nextElement();
			int index = ((Integer) poiIndexes.get(poiId)).intValue();
			if (index == poiIndex) {
				return poiId;
			}
		}
		return null;
	}
	
	/**
	 * Link two cities by a direct route with the given distance.
	 */
	public void addDirectRoute(String start, String end, int distance)
	{
		distances[getIndex(start)][getIndex(end)] = distance;
	}
	
	/**
	 * @return the distance between the two cities, or 0 if no path exists.
	 */
	public int getDistance(String start, String end)
	{
		return distances[getIndex(start)][getIndex(end)];
	}
	
	/**
	 * @return the list of all valid destinations from the given city.
	 */
	public List getDestinations(String city)
	{
		List list = new ArrayList();
		
		for (int i = 0; i < distances.length; i++)
		{
			if (distances[getIndex(city)][i] > 0)
			{
				list.add(getName(i));
			}
		}
		
		return list;
	}

	/**
	 * @return the list of all cities leading to the given city.
	 */
	public List getPredecessors(String city)
	{
		List list = new ArrayList();
		
		for (int i = 0; i < distances.length; i++)
		{
			if (distances[i][getIndex(city)] > 0)
			{
				list.add(getName(i));
			}
		}
		
		return list;
	}
	
	/**
	 * @return the transposed graph of this graph, as a new RoutesMap instance.
	 */
	public RoutesMap getInverse()
	{
		DenseRoutesMap transposed = new DenseRoutesMap(poiIndexes);
		
		for (int i = 0; i < distances.length; i++)
		{
			for (int j = 0; j < distances.length; j++)
			{
				transposed.distances[i][j] = distances[j][i];
			}
		}
		
		return transposed;
	}
}
