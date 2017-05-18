package edu.umich.ctools.esb.utils;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RunTestIBMTest {

	RunTestIBM rti = null;
			
	@Before
	public void setUp() throws Exception {
		rti = new RunTestIBM();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testRenewTokenIBM() {
		try {
			WAPIResultWrapper newTokenWrapper = rti.renewToken();
			assertNotNull("have token renewal result",newTokenWrapper);
			assertEquals("token renewal",200,newTokenWrapper.getStatus());
			assertEquals("token renewal","TOKEN RENEWED",newTokenWrapper.getMessage());
		} catch (IOException e) {
			fail("exception: "+e);
		}
	}
	
	@Test
	public void testGetGradesIBM() {
		try {
			WAPIResultWrapper newTokenWrapper = rti.getGrades();
			assertNotNull("have grades result",newTokenWrapper);
			assertEquals("good status",200,newTokenWrapper.getStatus());
		} catch (IOException e) {
			fail("exception: "+e);
		}
	}
}
