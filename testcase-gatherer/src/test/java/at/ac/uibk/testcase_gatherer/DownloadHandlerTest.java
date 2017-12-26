package at.ac.uibk.testcase_gatherer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.client.HttpResponseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DownloadHandlerTest {
	// path of the properties file
	private final static String PROP_PATH = "config" + File.separator + "github.properties";
	// remember the previous properties file, will be reset to that at the end
	private static Properties oldProps = new Properties();
	// the directory in which the test cases will be stored
	private static String testPathName = "testRepositories";

	private static FileHandler fileHandler = FileHandler.getInstance();
	private static DownloadHandler downloadHandler = DownloadHandler.getInstance();

	@BeforeClass
	public static void beforeClass() {
		// set a new base directory to avoid conflicts with a possible existing
		// one
		fileHandler.setBaseDir(testPathName);

		try {
			oldProps.load(new FileInputStream(PROP_PATH));
		} catch (IOException e1) {
			// do nothing, there probably where no old props
		}
	}

	@Before
	public void beforeTest() {
		// manually set some GitHub credentials for the tests
		// Properties prop = new Properties();
		// prop.setProperty("username", "name");
		// prop.setProperty("token", "token");
		// prop.setProperty("password", "");

		try {
			// prop.store(new FileOutputStream(PROP_PATH), "Testing
			// properties");

			// or use the already stored stuff
			oldProps.store(new FileOutputStream(PROP_PATH), "");
		} catch (IOException e) {
			fail("Could not set up GitHub properties, can't execute tests.");
		}
	}

	@AfterClass
	public static void afterClass() {
		// clean everything up after testing
		try {
			oldProps.store(new FileOutputStream(PROP_PATH), "");
		} catch (IOException e) {
			System.err.println("Failed to reset properties file.");
		}
		
		// clean everything up after testing
		Path testDir = new File(testPathName).toPath();
		try {
			Files.walkFileTree(testDir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			System.err.println("Failed to delete file structure of " + testPathName);
			e.printStackTrace();
		}
	}

	@After
	public void afterTest() {
		List<String> repoNames = new ArrayList<>(fileHandler.getReposContext().keySet());
		for (String repoName : repoNames){
			try {
				fileHandler.removeRepository(repoName);
			} catch (IOException e) {
				System.err.println("Failed to remove repository " +  repoName);
			}
		}
	}

	@Test
	public void testDownloadTestsFromRepo() throws Exception {
		// needs auth headers or wont work, therefore load them
		if (!downloadHandler.loadAuthHeader(true)) {
			System.out.println(
					"Failed to load authentication headers, check test configuration for a valid GitHub account if rate limits apply.");
		}

		// check if the download works correcty
		assertEquals("Something went wrong with the setup, there are test cases where there should be none.",
				fileHandler.getAllTestCases().size(), 0);
		int downloaded1 = downloadHandler.downloadTestsFromRepo("sdkman/sdkman-cli", true);
		assertTrue("No testcases downloaded at all, check if repository still exists.", downloaded1 > 0);
		assertEquals("The number of stored testcases in the repository is not equal to the number of downloaded ones.",
				fileHandler.getAllTestCases().size(), downloaded1);

		// check if test cases aren't downloaded again if the flag is set to do
		// so
		int downloaded2 = downloadHandler.downloadTestsFromRepo("sdkman/sdkman-cli", true);
		assertEquals("Downloaded testcases again even though it shouldn't.", downloaded2, 0);
	}

	@Test
	public void testDownloadPopularTests() throws Exception {
		// check if testcases from the first repository are downloaded
		assertEquals("Something went wrong with the setup, there are test cases where there should be none.",
				fileHandler.getAllTestCases().size(), 0);
		int downloaded1 = downloadHandler.downloadPopularTests(1);
		assertTrue("No testcases downloaded at all, check if search is correct.", downloaded1 > 0);
		assertEquals("The number of stored testcases in the repository is not equal to the number of downloaded ones.",
				fileHandler.getAllTestCases().size(), downloaded1);

		// check if testcases of the second repo were downloaded
		int downloaded2 = downloadHandler.downloadPopularTests(2);
		assertTrue("No testcases downloaded at all, check if search is correct.", downloaded2 > 0);
		assertEquals("The number of stored testcases in the repository is not equal to the number of downloaded ones.",
				fileHandler.getAllTestCases().size(), downloaded1 + downloaded2);
	}

	@Test
	public void testProperties() throws Exception {
		downloadHandler.setCredentials("", null, "asd");
		Properties testProps = new Properties();
		testProps.load(new FileInputStream(PROP_PATH));

		// assert that the values were actually persisted
		assertEquals(testProps.getProperty("username"), "");
		assertEquals(testProps.getProperty("password"), "");
		assertEquals(testProps.getProperty("token"), "asd");
	}

	@Test
	public void testAuthentication() throws Exception {
		// test that invalid credentials are not accepted
		downloadHandler.setCredentials("", null, "asd");
		assertFalse("Expected loading of invalid authentication headers to fail.",
				downloadHandler.loadAuthHeader(true));

		// test if valid but incorrect credentials fail the request properly
		downloadHandler.setCredentials("invalidUser", null, "invalidPassword");
		assertTrue("Failed to set authentication headers with technically correct values.",
				downloadHandler.loadAuthHeader(true));
		try {
			downloadHandler.requestRemainingRates();
			fail("Requests with invalid credentials should throw an exception.");
		} catch (HttpResponseException e) {
			// expected
		}

	}

}
