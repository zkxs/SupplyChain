import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zkxs.supplychain.Util;


public class UtilTest
{
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{}
	
	@Before
	public void setUp() throws Exception
	{}
	
	@After
	public void tearDown() throws Exception
	{}
	
	@Test
	public void testFuzzyCompare()
	{
		assertFalse(Util.fuzzyCompare(20, -50, 0.1));
		assertTrue(Util.fuzzyCompare(1, 1.13, 0.116));
		assertFalse(Util.fuzzyCompare(901, 895, 0.0066591));
		assertTrue(Util.fuzzyCompare(0.0, 0.0, 0.01));
		assertFalse(Util.fuzzyCompare(0.01, 0.0, 0.99));
	}
	
}
