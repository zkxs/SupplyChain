package com.zkxs.supplychain;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

/**
 * An array-backed list that is guaranteed to be in sorted order.
 * Inserts and searches are done via a binary search, completing at
 * worst in log(n) comparisons.
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) July 23, 2013
 * @version 1.1.0 2014-06-05
 * @param <E> no idea where "? super E" comes from
 */
public class SortedList<E extends Comparable <? super E>> implements Iterable<E>
{
	/** The ArrayList used as a backend for this data structure */
	private ArrayList<E> list;
	
	/** The comparator used to perform comparisons */
	private Comparator<E> comparator;
	
	/**
	 * Construct a new, empty SortedList
	 */
	public SortedList()
	{
		list = new ArrayList<E>();
		
		comparator = new Comparator<E>()
		{
			@Override
			public int compare(E o1, E o2)
			{
				return o1.compareTo(o2);
			}
		};
	}
	
	/**
	 * Construct a new SortedList with a specified comparator
	 * @param comparator the comparator to use
	 */
	public SortedList(Comparator<E> comparator)
	{
		list = new ArrayList<E>();
		this.comparator = comparator;
	}
	
	/**
	 * Construct a new SortedList that is a shallow copy of this SortedList.
	 * (The elements themselves are not cloned)
	 * @param toCopy The SortedList to copy.
	 */
	@SuppressWarnings("unchecked")
	public SortedList(SortedList<E> toCopy)
	{
		list = (ArrayList<E>) toCopy.list.clone();
		
		comparator = new Comparator<E>()
		{
			@Override
			public int compare(E o1, E o2)
			{
				return o1.compareTo(o2);
			}
		};
	}
	
	/**
	 * Construct a new, empty SortedList with the specified initial capacity
	 * @param initialCapacity the initial capacity of the list
	 */
	public SortedList(int initialCapacity)
	{
		list = new ArrayList<E>(initialCapacity);
		
		comparator = new Comparator<E>()
		{
			@Override
			public int compare(E o1, E o2)
			{
				return o1.compareTo(o2);
			}
		};
	}
	
	/**
	 * Check if this list is empty
	 * @return <code>true</code> if this list is empty
	 */
	public boolean isEmpty()
	{
		return list.isEmpty();
	}
	
	/**
	 * Get the number of element in this list
	 * @return the number of element in this list
	 */
	public int size()
	{
		return list.size();
	}
	
	/**
	 * Remove all elements from this list
	 */
	public void clear()
	{
		list.clear();
	}
	
	/**
	 * Get an item out of the list
	 * @param index the item's index
	 * @return the item
	 * @throws IndexOutOfBoundsException when the index is not in the array's bounds
	 */
	public E get(int index) throws IndexOutOfBoundsException
	{
		return list.get(index);
	}
	
	/**
	 * Remove the element at the given index
	 * @param index the index of the element to be removed
	 * @return The element that was removed from the list
	 */
	public E remove(int index)
	{
		return list.remove(index);
	}
	
	/**
	 * Add a new item into the appropriate position in this list
	 * @param newItem the new item to add
	 * @return the index the item is now at
	 */
	public int add(E newItem)
	{
		int index = indexOf(newItem);
		list.add(index, newItem);
		return index;
	}
	
	/**
	 * Append an item to the end if this SortedList. The new item MUST be
	 * greater than or equal to the current last item. This completes in linear time,
	 * and is therefore more efficient than {@link #add(Comparable)} when it is know that
	 * the new item should go on the end of the list.
	 * @param newItem The item to append
	 * @throws IllegalArgumentException If the new item cannot be appended to the list
	 * while maintaining sorted order.
	 */
	public void append(E newItem)
	{
		if (!list.isEmpty() && comparator.compare(newItem, list.get(list.size() - 1)) < 0)
			throw new IllegalArgumentException("Appended item less than last item");
		
		list.add(newItem);
	}
	
	/**
	 * Remove an item from this list
	 * @param anItem the item to remove
	 * @return <code>true</code> if the item was found and removed, <code>false</code> if it was not found
	 */
	public boolean remove(E anItem)
	{
		int index = indexOf(anItem);
		if (get(index).equals(anItem))
		{
			list.remove(index);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Check to see if an item is in this list
	 * @param anItem the item to search for
	 * @return <code>true</code> if the item is in this list
	 */
	public boolean contains(E anItem)
	{
		return get(indexOf(anItem)).equals(anItem);
	}
	
	/**
	 * Find the index of a given item using a binary search
	 * @param anItem the item to search for
	 * @return the item's index, or the index where it should be
	 */
	public int indexOf(E anItem)
	{
		int firstIndex = 0;
		int lastIndex = size() - 1;
		int pivot;
		
		if (list.isEmpty())
		{
			return 0;
		}
		
		while (firstIndex < lastIndex)
		{
			pivot = firstIndex - 1 + (lastIndex - firstIndex + 1) / 2;
			if (comparator.compare(anItem, get(pivot)) == 0) 
			{	// anItem is the pivot
				return pivot;
			}
			else if (comparator.compare(anItem, get(pivot)) > 0)
			{	// anItem is greater than pivot
				firstIndex = pivot + 1;
			}
			else
			{	// anItem is less than pivot
				lastIndex = pivot - 1;
			}
		}
		// at this point, firstIndex == lastIndex
		
		if (comparator.compare(anItem, get(firstIndex)) > 0)
		{	// element does not exist and should go after this position
			return firstIndex + 1; // +1 to adjust to before this position
		}
		else
		{	// element is there, or should go before there
			return firstIndex;
		}
	}
	
	public int lastIndexOf(E anItem)
	{
		int index = indexOf(anItem);
		
		do
		{
			index++;
			
			// while in bounds and equal
		} while (index < size() && comparator.compare(anItem, get(index)) == 0);
		
		// previous index always valid
		// if this index is valid, increment and repeat
		// else, return last valid (previous) index
		
		return index - 1;
	}
	
	@Override
	public String toString()
	{
		return toString(0, size() - 1);
	}
	
	/**
	 * Print part of this list
	 * @param first first index, inclusive
	 * @param last last index, inclusive
	 * @return
	 */
	public String toString(int first, int last)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i = first; i <= last; i++)
		{
			if (i != first)
				sb.append(", ");
			sb.append(get(i));
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public Iterator<E> iterator()
	{
		return list.iterator();
	}
	
}  // end class
