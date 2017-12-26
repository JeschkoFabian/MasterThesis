package at.ac.uibk.testcase_gatherer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import at.ac.uibk.testcase_gatherer.FileHandler;
import at.ac.uibk.testcase_gatherer.model.FeatureContext;

public class FileHandlerTest {
	// some content that will be stored locally
	private String content = "Some content\nWith multiple lines.";
	// a sample repository name
	private static String repoName = "testrepo/testrepo-testmodule";
	// the path where the test repository should be stored in
	private static String testPath = "testRepositories";
	private static FileHandler fileHandler = FileHandler.getInstance();

	@BeforeClass
	public static void beforeClass() {
		// set a new base directory to avoid conflicts with a possible existing one
		fileHandler.setBaseDir(testPath);
	}

	@AfterClass
	public static void afterClass() {
		// clean everything up after testing
		Path testDir = new File(testPath).toPath();
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
			System.err.println("Failed to delete file structure of " + testPath);
			e.printStackTrace();
		}
	}

	@Test
	public void testStoreGetAndRemove() throws IOException {
		String featureName = "test.feature", featurePath = "testPath";
		FeatureContext context = new FeatureContext(featureName, featurePath, repoName);
		fileHandler.storeTestCase(context, content);

		// verify file was actually created locally
		File testFile = Paths.get(fileHandler.getBaseDir(), repoName.replaceAll("-", "__"), featurePath, featureName).toFile();
		assertTrue("Method storeTestCase did not create the file.", testFile.exists() && testFile.isFile());

		// verify the stored text is correct, needs trimming since there is a little transformation
		String storedText = fileHandler.getFeatureContent(context);
		assertEquals("Retrieved and stored content are not equal.", storedText.trim(), content.trim());

		// verify that deleting a repository works
		fileHandler.removeRepository(repoName);
		assertFalse("Method removeRepository did not remove the files.", testFile.exists());
	}

	@Test
	public void testOtherGetters() throws IOException {
		// prepare some test cases
		int storeAmount = 42;
		for (int i = 0; i < storeAmount; i++) {
			fileHandler.storeTestCase(new FeatureContext("test" + i + ".feature", "", repoName), i + " " + content);
		}

		// verify the getAllTestCases method
		assertEquals("Did not store or retrieve all test cases.", fileHandler.getAllTestCases().size(), storeAmount);
		
		// verify the getSomeTestCases method with different inputs
		assertEquals("The getSomeTestCases method retrieved a wrong number of tests.",
				fileHandler.getSomeTestCases(10).size(), 10);
		assertEquals("The getSomeTestCases method did not retrieve all in case of too many.",
				fileHandler.getSomeTestCases(55).size(), 42);
		
		assertEquals("Did not get all the test cases inside the repository.", fileHandler.getTestCases(repoName).size(), 42);
		assertEquals("Should get an empty list if the repository is not existing.", fileHandler.getTestCases("asd").size(), 0);
	}

}
