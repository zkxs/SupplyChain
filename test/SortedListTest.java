import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

import com.zkxs.supplychain.SortedList;

@FixMethodOrder()
public class SortedListTest
{
	SortedList<Integer> empty;
	SortedList<Integer> one;
	SortedList<Integer> two;
	SortedList<Integer> three;
	
	@SuppressWarnings("unchecked")
	SortedList<Integer>[] lists = new SortedList[4];
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{}
	
	@Before
	public void setUp() throws Exception
	{
		empty = new SortedList<Integer>();
		one = new SortedList<Integer>();
		two = new SortedList<Integer>();
		three = new SortedList<Integer>();
		
		lists[0] = empty;
		lists[1] = one;
		lists[2] = two;
		lists[3] = three;
		
		for (int list = 1; list < lists.length; list++)
		{
			for(int numbers = list; numbers > 0; numbers--)
			{
				lists[list].add(numbers);
			}
		}
	}
	
	@After
	public void tearDown() throws Exception
	{}
	
	@SuppressWarnings({ "rawtypes", "unused" })
	@Test
	public void testSortedList()
	{
		SortedList sortedList = new SortedList();
		SortedList<Double> sortedList2 = new SortedList<Double>();
	}
	
	@Test
	public void testIsEmpty()
	{
		assertTrue(empty.isEmpty());
		for (int list = 1; list < lists.length; list++)
		{
			assertFalse(lists[list].isEmpty());
		}
	}
	
	@Test
	public void testSize()
	{
		for (int list = 0; list < lists.length; list++)
		{
			assertEquals(list, lists[list].size());
		}
	}
	
	@Test
	public void testClear()
	{
		for (int list = 0; list < lists.length; list++)
		{
			lists[list].clear();
			assertTrue(lists[list].isEmpty());
		}
	}
	
	@Test
	public void testGet()
	{
		SortedList<Integer> list = new SortedList<Integer>();
		
		list.add(3);
		list.add(13);
		list.add(1);
		list.add(-5);
		list.add(25);
		
		assertEquals(Integer.valueOf(-5), list.get(0));
		assertEquals(Integer.valueOf(3), list.get(2));
		assertEquals(Integer.valueOf(25), list.get(4));
	}
	
	@Test
	public void testRemoveInt()
	{
		SortedList<Integer> list = new SortedList<Integer>();
		
		list.add(3);
		list.add(13);
		list.add(1);
		list.add(-5);
		list.add(25);
		
		list.remove(0);
		assertEquals("{1, 3, 13, 25}", list.toString());
		
		list.remove(1);
		assertEquals("{1, 13, 25}", list.toString());
		
		list.remove(2);
		assertEquals("{1, 13}", list.toString());
	}
	
	@Test
	public void testAdd()
	{
		SortedList<Integer> list = new SortedList<Integer>();
		
		list.add(3);
		list.add(13);
		list.add(1);
		list.add(-5);
		list.add(25);
		
		assertEquals("{-5, 1, 3, 13, 25}", list.toString());
	}
	
	@Test
	public void testAppend()
	{
		SortedList<Integer> list = new SortedList<Integer>();
		
		list.append(-5);
		list.append(1);
		list.append(3);
		list.append(3);
		list.append(13);
		list.append(25);
		
		assertEquals("{-5, 1, 3, 3, 13, 25}", list.toString());
		
		try
		{
			list.append(24);
			fail("should not append");
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Appended item less than last item", e.getMessage());
		}
	}
	
	@Test
	public void testRemove()
	{
		SortedList<Integer> list = new SortedList<Integer>();
		
		list.add(3);
		list.add(13);
		list.add(1);
		list.add(-5);
		list.add(25);
		
		list.remove(Integer.valueOf(-5));
		assertEquals("{1, 3, 13, 25}", list.toString());
		
		list.remove(Integer.valueOf(3));
		assertEquals("{1, 13, 25}", list.toString());
		
		list.remove(Integer.valueOf(25));
		assertEquals("{1, 13}", list.toString());
	}
	
	@Test
	public void testContains()
	{
		SortedList<Integer> list = new SortedList<Integer>();
		
		list.add(3);
		list.add(13);
		list.add(1);
		list.add(-5);
		list.add(25);
		
		assertTrue(list.contains(Integer.valueOf(-5)));
		assertTrue(list.contains(Integer.valueOf(3)));
		assertTrue(list.contains(Integer.valueOf(25)));
		assertFalse(list.contains(Integer.valueOf(7)));
	}
	
	@Test
	public void testIndexOf()
	{
		SortedList<Integer> list = new SortedList<Integer>();
		
		list.add(3);
		list.add(13);
		list.add(1);
		list.add(-5);
		list.add(25);
		
		assertEquals(0, list.indexOf(Integer.valueOf(-5)));
		assertEquals(2, list.indexOf(Integer.valueOf(3)));
		assertEquals(4, list.indexOf(Integer.valueOf(25)));
		assertEquals(3, list.indexOf(Integer.valueOf(7)));
	}
	
	@Test
	public void testToString()
	{
		for (int list = 0; list < lists.length; list++)
		{
			lists[list].toString();
		}
		
		SortedList<Integer> list = new SortedList<Integer>();
		
		list.add(3);
		list.add(13);
		list.add(1);
		list.add(-5);
		list.add(25);
		
		assertEquals("{-5, 1, 3, 13, 25}", list.toString());
	}
	
	@Test
	public void testToStringIntInt()
	{
		for (int list = 0; list < lists.length; list++)
		{
			lists[list].toString();
		}
		
		SortedList<Integer> list = new SortedList<Integer>();
		
		list.add(3);
		list.add(13);
		list.add(1);
		list.add(-5);
		list.add(25);
		
		assertEquals("{1, 3, 13}", list.toString(1,3));
	}
	
	@Test
	public void testIterator()
	{
		// no need to test this, it just passes the ArrayList's iterator out
	}
	
}
