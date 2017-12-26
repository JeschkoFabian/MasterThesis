package at.ac.uibk.testcase_gatherer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.http.HttpException;

import at.ac.uibk.testcase_gatherer.model.FeatureContext;
import at.ac.uibk.testcase_gatherer.model.RepositoryContext;

public class ExecuteDownload {
	private static String baseDir = "repos";
	private static FileHandler fileHandler = FileHandler.getInstance();
	private static DownloadHandler downloadHandler = DownloadHandler.getInstance();

	public static void main(String[] args) {
		fileHandler.setBaseDir(baseDir);

		// downloadXPopularReposInto(10);
		countRepos();

		// long currentTime = System.currentTimeMillis() / 1000;
		// long stamp = 1496336184;
		//
		// System.out.println("Current time is " + currentTime);
		// System.out.println("Time left is " + (stamp - currentTime)/60 + "
		// min.");

		// downloadAllRepos();

		// printRepoStats();

//		revertFileStructure();
	}

	private static void revertFileStructure() {
		Map<String, RepositoryContext> repos = fileHandler.getReposContext();
		String baseDir = fileHandler.getBaseDir();

		int numFix = 0;
		int numTotal = 0;

		for (RepositoryContext ctx : repos.values()) {
			for (FeatureContext fctx : ctx.getFeatures()) {
				String realPath = baseDir + "/" + ctx.getName() + "/";
				if (!(fctx.getPath() == null) && !fctx.getPath().equals("")) {
					realPath += fctx.getPath() + "/";
				}
				realPath += fctx.getName();
				String oldPath = realPath.replace("-", "__");

				String[] realPaths = realPath.split("/");
				String[] oldPaths = oldPath.split("/");
				
				for (int i = 0; i < realPaths.length; i++){
					String subReal = realPaths[0];
					String subOld = oldPaths[0];
					
					for(int j = 1; j <= i; j++){
						subReal += "/" + realPaths[j];
						if (j == i){
							subOld += "/" + oldPaths[j];
						} else {
							subOld += "/" + realPaths[j];
						}
					}

					if (Paths.get(subOld).toFile().exists()) {
						if (!subOld.equals(subReal)) {
							numTotal++;
							try {
								Files.move(Paths.get(subOld), Paths.get(subReal));
								System.out.println(subOld + " -> " + subReal);
								numFix++;
							} catch (IOException e) {
								if (!Paths.get(subReal).toFile().exists()) {
									System.err.println("Failed to create " + subReal);
									e.printStackTrace();
									break;
								}
							}
						}
					}
				}
				
				if (!Paths.get(realPath).toFile().exists()){
					System.err.println("File does not exist! " + realPath);
				}
			}
		}

		System.out.println("Out of " + numTotal + " were " + numFix + " corrected.");

		fileHandler.saveContext();
	}

	private static void printRepoStats() {
		Map<String, RepositoryContext> repos = fileHandler.getReposContext();

		System.out.println("There is a total of " + repos.size() + " repos locally stored.");
		for (RepositoryContext repo : repos.values()) {
			System.out.println("Repo '" + repo.getName() + "' with " + repo.getFeatures().size()
					+ " Features and the languages " + repo.getLanguages());
		}
	}

	private static void countRepos() {
		Map<String, RepositoryContext> repos = fileHandler.getReposContext();
		File f = new File(baseDir);

		int moduleCount = repos.size();
		int repoCount = f.listFiles().length;

		System.out.println("Total amount of repos for folder " + baseDir + " is " + repoCount + " with a total of "
				+ moduleCount + " modules inside.");

		int featureCount = 0;

		for (RepositoryContext c : repos.values()) {
			featureCount += c.getFeatures().size();
		}

		System.out.println("There is a total of " + featureCount + " features alltogether.");
	}

	/**
	 * GitHubs search API only allows searches up to 1k elements, if you want
	 * more the results need to be split up via some filter, best done with
	 * dates.
	 * 
	 * @param dlFolder
	 */
	private static void downloadAllRepos() {
		try {
			int amount = 0;

			// startDate is 2016 since thats when cucumber got really popular
			int startDate = 2016;
			// endDate is 2018 since, well its 2017 right now
			int endDate = 2017;

			// get all cases before 2016, is smth like about 400?
			System.out.println("Downloading tests prior to the year " + startDate);
			amount += downloadHandler.downloadPopularTests(0, 1000, "created:<=" + startDate + "-01-01");

			// from here on go yearly
			for (int i = startDate; i < endDate; i++) {
				// takes repos in the span of one year, if that surpasses 1k
				// files the span needs to be decreased to months
				System.out.println("Downloading tests inbetween the years " + i + " and " + (i + 1));
				amount += downloadHandler.downloadPopularTests(0, 1000,
						"created:" + i + "-01-01.." + (i + 1) + "-01-01");
			}

			// get all tests after 2017
			System.out.println("Downloading tests after the year " + endDate);
			amount += downloadHandler.downloadPopularTests(0, 1000, "created:2017-01-01..2017-06-01");

			// amount += downloadHandler.downloadPopularTests(0, 1000,
			// "created:>" + endDate + "-01-01");

			System.out.println("A total of " + amount + " tests were downloaded.");
		} catch (URISyntaxException | IOException | HttpException e) {
			System.err.println("Failed to download test cases for all repositories.");
			e.printStackTrace();
		}
		System.out.println("A total of " + fileHandler.getAllTestCases().size() + " are now stored locally.");
	}

	private static void downloadXPopularReposInto(int dlAmount) {
		try {
			System.out.println("Trying to download test cases from the top " + dlAmount + " repositories.");
			int dl = downloadHandler.downloadPopularTests(dlAmount);
			System.out.println("A total of " + dl + " tests were downloaded.");
		} catch (URISyntaxException | IOException | HttpException e) {
			System.err.println("Failed to download test cases for top " + dlAmount + " repositories.");
			e.printStackTrace();
		}
		System.out.println("A total of " + fileHandler.getAllTestCases().size() + " are now stored locally.");

	}

}
