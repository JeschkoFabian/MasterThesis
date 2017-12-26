package at.ac.uibk.testcase_gatherer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import at.ac.uibk.testcase_gatherer.model.FeatureContext;
import at.ac.uibk.testcase_gatherer.model.RepositoryContext;

/**
 * Class that handles how test cases are stored and retrieved. Also has some
 * neat utility methods that will probably be useful for the learning process.
 * 
 * @author Fabian
 *
 */
public class FileHandler {
	/**
	 * Singleton instance
	 */
	private static FileHandler instance;

	/**
	 * Base directory where the test cases are stored, relative to the project
	 * dir.
	 */
	private String baseDir;

	private Map<String, RepositoryContext> repos;

	private FileHandler() {
		setBaseDir("repositories");
	}

	public static FileHandler getInstance() {
		if (instance == null) {
			instance = new FileHandler();
		}

		return instance;
	}

	public String getBaseDir() {
		return baseDir;
	}

	@SuppressWarnings("unchecked")
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;

		File reposFile = new File(baseDir + "/" + "repos.context");
		if (reposFile.exists()) {
			try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(reposFile))) {
				repos = (Map<String, RepositoryContext>) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				// nothing really..
				repos = new HashMap<>();
			}
		} else {
			repos = new HashMap<>();
		}
	}

	public void saveContext() {
		File reposFile = new File(baseDir + "/" + "repos.context");
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(reposFile))){
			oos.writeObject(repos);
		} catch (IOException e) {
			// nothing really..
		}
	}
	
	public void createOrUpdateRepoContext(RepositoryContext context){
		if (repos.get(context.getName()) == null){
			try {
				removeRepository(context.getName());
			} catch (IOException e) {
				System.out.println("Can't delete directory " + context.getName() + ", either none present or no permission.");
				// clean it up if there is already one, failure means empty or no permission
			}
			
			repos.put(context.getName(), context);
		} else {
			RepositoryContext repo = repos.get(context.getName());
			
			if (!repo.getPushedAt().equals(context.getPushedAt())){
				try{
					removeRepository(repo.getName());
				} catch (IOException e){
					System.out.println("Can't delete directory " + repo.getName() + ", either none present or no permission.");
					// doesnt matter, probably not existing
				}
				
				repos.put(context.getName(), context);
			} else if (!repo.getUpdatedAt().equals(context.getUpdatedAt())){
				// not sure if the id can change, probably not but whatever
				repo.setId(context.getId());
				repo.setUpdatedAt(context.getUpdatedAt());
				repo.setLanguages(context.getLanguages());
			}
		}
	}
	
	public Map<String, RepositoryContext> getReposContext(){
		return repos;
	}

	public RepositoryContext getRepoContext(String name){
		return repos.get(name);
	}

	
	/**
	 * Stores a test case in some organised structure. The final path will be as
	 * follows: [projectDir]/[baseDir]/[path]/[testcaseFile].
	 * 
	 * @param path
	 *            is the repository the file was taken from including the folder
	 *            structure below.
	 * @param fileName
	 *            is the file name this testcase had (probably [name].feature if
	 *            gherkin)
	 * @param content
	 *            is the testcase content that will be stored in the file
	 * @throws IOException
	 *             if there was some trouble with creating the file/folders
	 */
	public void storeTestCase(FeatureContext context, String content) throws IOException {
		RepositoryContext repoContext = repos.get(context.getRepoName());
		if(repoContext == null){
			System.out.println("No context for " + context.getRepoName() + " found, therefore creating a simplistic one.");
			repoContext = new RepositoryContext(0, context.getRepoName(), "", "");
			
			repos.put(context.getRepoName(), repoContext);
		}
		
		String path = repoContext.getName() + "/" + context.getPath();

		// add a new line at the end if there is none, is relevant due to parser
		// limitations..
		if (!content.endsWith("\n")) {
			content += "\n";
		}

		// create the folder structure if not already there
		Path folderPath = Paths.get(baseDir, path);
		File f = folderPath.toFile();
		if (!f.exists()) {
			if (!f.mkdirs()) {
				System.err.println("Failed to create dirs for " + folderPath.getFileName());
			}
		}

		Files.write(Paths.get(baseDir, path, context.getName()), content.getBytes(StandardCharsets.UTF_8));
		if (!repoContext.getFeatures().contains(context)){
			repoContext.getFeatures().add(context);
		}
	}

	/**
	 * Counterpart to {@link #storeTestCase(String, String, String)} method.
	 * Retrieves the stored testcase or throws an exception if it does not
	 * exist. For how the path was constructed, see the above linked method.
	 * 
	 * @param path
	 *            is the repository and folder structure the file was taken
	 *            from.
	 * @param fileName
	 *            is the file name this testcase had (probably [name].feature if
	 *            gherkin)
	 * @return is the testcase content that will be stored in the file
	 * @throws IOException
	 *             if there was some trouble with creating the file/folders
	 */
//	public String getTestCase(String path, String fileName) throws IOException {
//		path = path.replaceAll("-", "__");
//
//		return new String(Files.readAllBytes(Paths.get(baseDir, path, fileName)));
//	}
	public String getFeatureContent (FeatureContext context) throws IOException{
		String path = baseDir + "/" + context.getRepoName() + "/" + context.getPath();
		
		return new String(Files.readAllBytes(Paths.get(path, context.getName())));
	}

	/**
	 * Method to get all test cases for a repository. Can also include
	 * subfolders. If there are no files, it returns an empty list.
	 * 
	 * @param repoName
	 *            the repository name from GitHub
	 * @return a list of test cases or an empty list
	 * @throws IOException
	 */
	public List<String> getTestCases(String repoName) throws IOException {
		List<String> tests = new ArrayList<>();
		RepositoryContext repo = repos.get(repoName);
		
		if (repo != null){
			for (FeatureContext feature : repo.getFeatures()){
				String content = getFeatureContent(feature);
				tests.add(content);
			}
		}

		return tests;
	}

	/**
	 * Removes a locally stored repository and all of its testcases. Can be used
	 * if the user wants to remove old ones or in case of tests.
	 * 
	 * @param repoName
	 *            is the name of the repository.
	 */
	public void removeRepository(String repoName) throws IOException{
		repos.remove(repoName);

		File repoDir = Paths.get(baseDir, repoName).toFile();
		deleteDir(repoDir);
	}
	
	private void deleteDir(File dir){
		if (dir.isDirectory()){
			for (File f : dir.listFiles()) {
				deleteDir(f);
			}
		}

		dir.delete();
	}

	/**
	 * Since test cases are stored in a specific logical file structure it isn't
	 * that simple to retrieve all of them. This way, the learning algorithm
	 * does not need to know about the specifics of how files were stored
	 * locally and can just get all of them at once. If none are found, an empty
	 * list is returned.
	 * 
	 * @return all locally stored test cases (in the set baseDir)
	 */
	public List<String> getAllTestCases() {
		List<String> tests = new ArrayList<>();
		
		for (Entry<String, RepositoryContext> entry : repos.entrySet()){
			RepositoryContext repo = entry.getValue();
			
			for (FeatureContext feature : repo.getFeatures()){
				try {
					String content = getFeatureContent(feature);
					tests.add(content);
				} catch (IOException e) {
					System.err.println("Failed to read feature: "  + feature.getPath() + "/" + feature.getName());
					e.printStackTrace();
				}
			}
		}

		return tests;
	}

	/**
	 * Method to get a specific amount of test cases or simply all if the
	 * specified amount was larger than the actual. Seems to be another likely
	 * use case for the learning algorithm.
	 * 
	 * @param amount
	 *            is the amount of tests to be retrieved
	 * @return the specified amount of test cases or all of them
	 */
	public List<String> getSomeTestCases(int amount) {
		List<String> tests = getAllTestCases();
		Collections.shuffle(tests);

		if (amount > tests.size()) {
			amount = tests.size();
		}

		return tests.subList(0, amount);
	}
}
