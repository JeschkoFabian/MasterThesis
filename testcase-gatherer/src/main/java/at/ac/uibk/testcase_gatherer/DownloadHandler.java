package at.ac.uibk.testcase_gatherer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import at.ac.uibk.testcase_gatherer.model.FeatureContext;
import at.ac.uibk.testcase_gatherer.model.RepositoryContext;

/**
 * Class that uses REST to build an initial knowledge base for the learning
 * algorithm by downloading tests from popular or given GitHub repositories.
 * 
 * @author Fabian
 *
 */
public class DownloadHandler {
	/**
	 * The language to search for in repositories. Default is cucumber.
	 */
	private String language = "cucumber";
	/**
	 * The file extension the specified language has, in case of cucumber that
	 * would be .feature
	 */
	private String extension = "feature";
	private int pageSize = 100;

	private Header authHeader = null;

	private final Charset ENCODING = StandardCharsets.UTF_8;
	private final String GIT_CONF_PATH = "config" + File.separator + "github.properties";

	// integer values of the remaining rates for search and core (file download
	// etc)
	private int rateSearchRemaining = 0;
	private int rateCoreRemaining = 0;
	
	private FileHandler fileHandler = FileHandler.getInstance();
	private static DownloadHandler instance;
	
	private DownloadHandler(){
	}
	
	public static DownloadHandler getInstance(){
		if (instance == null){
			instance = new DownloadHandler();
		}
		
		return instance;
	}
	
	
	/**
	 * All files with the given {@link #extension} extension in the specified
	 * repository. Furthermore limits the repository to be of the specified
	 * {@link #language} language.
	 * 
	 * This requires 1 search call (per minute) and about 20-30 resource calls
	 * (per hour). Normal GitHub ratings at this time are 10 search and 60
	 * resource calls for anonymous users and 30/5000 for authenticated
	 * users/applications.
	 * 
	 * This means on average this function can be called two times without
	 * authentication. Will check the current rate limits and throw an exception
	 * if they are insufficient.
	 * 
	 * @param repoName
	 *            is the repository name in GitHub (example: sdkman/sdkman-cli)
	 * @param downloadAlways
	 *            if the tests should be downloaded again if they are already
	 *            locally present
	 * @return returns the number of downloaded test cases
	 * @throws IOException
	 *             if the received content was not readable
	 * @throws URISyntaxException
	 *             if the built URI was invalid (due to invalid input)
	 * @throws HttpException
	 *             if the request returned some error code above 400
	 */
	public int downloadTestsFromRepo(String repoName, boolean checkContext)
			throws IOException, URISyntaxException, HttpException {
		// update local status of download and search rates
		requestRemainingRates();

		// no search remaining
		if (rateSearchRemaining == 0) {
			waitForReset(2);
		}
		
		// make sure to create a repoContext if there is none yet
		if (checkContext){
			URI repoInfoUrl = new URI("https://api.github.com/repos/" + repoName);
			HttpGet repoInfoRequest = new HttpGet(repoInfoUrl);
			
			// retrieve the search query as JSON data
			HttpResponse repoInfoResponse = requestData(repoInfoRequest);
			JsonObject repoInfoObject = readJson(repoInfoResponse).getAsJsonObject();

			Integer repoId = repoInfoObject.get("id").getAsInt();
			String updatedAt = repoInfoObject.get("updated_at").getAsString();
			String pushedAt = repoInfoObject.get("pushed_at").getAsString();

			RepositoryContext newContext = new RepositoryContext(repoId, repoName, pushedAt, updatedAt);
			RepositoryContext oldContext = fileHandler.getRepoContext(repoName);

			// updated at refers to the repo object, so the language could have changed for example or the name
			if (oldContext == null || !oldContext.getUpdatedAt().equals(updatedAt)){
				String langUrl = repoInfoObject.get("languages_url").getAsString();
				HttpGet langGet = new HttpGet(langUrl);
				HttpResponse langResp = requestData(langGet);
				JsonObject langObj = readJson(langResp).getAsJsonObject();
				
				for (Entry<String, JsonElement> entry : langObj.entrySet()){
					newContext.getLanguages().add(entry.getKey());
				}
			}

			fileHandler.createOrUpdateRepoContext(newContext);
			
			if (oldContext != null && oldContext.getPushedAt().equals(newContext.getPushedAt())){
				System.out.println("Skipping download for unchanged repository " + repoName);
				return 0;
			}
		}

		// build search URI
		URI repoUrl = new URI("https://api.github.com/search/code?q=+language:" + language + "+extension:" + extension
				+ "+repo:" + repoName + "&sort=stars&order=desc");
		HttpGet request = new HttpGet(repoUrl);

		// headers if necessary, but seems not?
		// request.addHeader(name, value);

		/**
		 * Example output:
		 * 
		 * <code>
		 * 
			{
				"total_count": 22,
				"incomplete_results": false,
				"items": [{
					"name": "broadcast.feature",
					"path": "src/test/cucumber/broadcast.feature",
					"sha": "ee2257f180757b0fc3735635d0334dca89c2b13c",
					"url": "https://api.github.com/repositories/5573275/contents/src/test/cucumber/broadcast.feature?ref=13d907b4a175c000da5ca23fa9041874b014ff5a",
					"git_url": "https://api.github.com/repositories/5573275/git/blobs/ee2257f180757b0fc3735635d0334dca89c2b13c",
					"html_url": "https://github.com/sdkman/sdkman-cli/blob/13d907b4a175c000da5ca23fa9041874b014ff5a/src/test/cucumber/broadcast.feature",
					"repository": {
					    "id": 5573275,
					    "name": "sdkman-cli",
					}
				}]
			}
		 * </code>
		 * 
		 */
		// retrieve the search query as JSON data
		HttpResponse filesResponse = requestData(request);
		JsonObject filesObject = readJson(filesResponse).getAsJsonObject();
		JsonArray items = filesObject.getAsJsonArray("items");
		int fileCount = 0;

		// update remaining search entries with the returned header
		rateSearchRemaining = Integer.valueOf(filesResponse.getFirstHeader("X-RateLimit-Remaining").getValue());

		System.out.println("Trying to download " + items.size() + " tests from repository " + repoName);

		// iterate through all .feature files
		for (JsonElement elem : items) {
			JsonObject file = elem.getAsJsonObject();

			// get file name
			String fileName = file.get("name").getAsString();
			String fileUrl = file.get("git_url").getAsString();

			// get the folder the file is in
			String path = file.get("path").getAsString();
			String filePath = "";
			
			// remove the file 
			if (!path.equals(fileName)){
				filePath = path.replace("/" + fileName, "");
			}
			
			// create the context
			FeatureContext context = new FeatureContext(fileName, filePath, repoName);

			// only necessary to check once it is certain a file needs to be
			// downloaded
			if (rateCoreRemaining == 0) {
				// current time as unix stamp (seconds)
				long currentTime = System.currentTimeMillis() / 1000;
				long resetTime = Long.getLong(filesResponse.getFirstHeader("X-RateLimit-Reset").getValue());
				
				// add one minute to the reset time to account for rounding errors
				int resetMinutes = (int) ((resetTime - currentTime)/60) + 1;
				
				// that way people can just cancel if they want to, but the download continues automatically
				System.out.println("Next rate reset for downloads happens in " + resetMinutes + " minutes, waiting for that.");
				waitForReset(resetMinutes);
				
//				throw new HttpException(
//						"Reached the rate limit for core calls on GitHub, wait for the next reset before continuing. Resets every hour.");
			}

			// request the actual file content (resource call)
			HttpGet contentRequest = new HttpGet(fileUrl);
			HttpResponse contentResponse = requestData(contentRequest);
			JsonObject fileObject = readJson(contentResponse).getAsJsonObject();

			rateCoreRemaining = Integer.valueOf(filesResponse.getFirstHeader("X-RateLimit-Remaining").getValue());

			// check if it was encoded
			if (fileObject.get("encoding") != null && !"base64".equals(fileObject.get("encoding").getAsString())) {
				System.err.println("File encoding should be 'base64' but isn't! Skipping file '" + fileName + "'");
				continue;
			}

			// seems like the content should be read with a line reader or
			// something, just throw out the new lines for now
			String encodedContent = fileObject.get("content").getAsString().replace("\n", "");
			String fileContent = new String(Base64.getDecoder().decode(encodedContent), ENCODING);

			// handle that error here since even if the storing of this one
			// fails, the others might not
			try {
				// store testcase locally
				fileHandler.storeTestCase(context, fileContent);
			} catch (IOException e) {
				System.err.println("Failed to store file: " + context.getPath() + "/" + fileName);
				e.printStackTrace();
			}

			// count the downloaded files, not the stored ones
			fileCount++;
		}

		fileHandler.saveContext();
		
		// System.out.println("Downloaded " + fileCount + " tests from
		// repository " + repoName);

		return fileCount;
	}

	/**
	 * Method to download tests cases from the most popular GitHub repositories
	 * and store them locally in an organised file structure via the FileHandler
	 * class. Valid authentication is required for this to work since a big
	 * amount of test cases will be downloaded, which in most cases (usually 3
	 * or above) will surpass the free limit.
	 * 
	 * First obtains the most popular GitHub repositories that contain the
	 * specified test cases suffix. Then for each of these tests, calls the
	 * {{@link #downloadTestsFromRepo(String, boolean)} method.
	 * 
	 * This method might run for a long time, especially if the amount of
	 * repositories is above 30, since then two minutes need to be spent waiting
	 * for the search rate limit to reset.
	 * 
	 * @param amount
	 *            the amount of repositories to search for
	 * @param downloadAlways
	 *            if locally stored tests should be downloaded again
	 * @return how many tests were actually downloaded
	 * @throws URISyntaxException
	 *             if the search URL contains invalid characters
	 * @throws IOException
	 *             if the tests could not be stored or the properties file could
	 *             not be read
	 * @throws HttpException
	 *             if the request failed with a HTTP code of above 400
	 */
	public int downloadPopularTests(int amount)
			throws URISyntaxException, IOException, HttpException {
		return downloadPopularTests(0, amount, "");
	}

	/**
	 * Method to download tests cases from the most popular GitHub repositories
	 * and store them locally in an organised file structure via the FileHandler
	 * class. Valid authentication is required for this to work since a big
	 * amount of test cases will be downloaded, which in most cases (usually 3
	 * or above) will surpass the free limit.
	 * 
	 * First obtains the most popular GitHub repositories that contain the
	 * specified test cases suffix. Then for each of these tests, calls the
	 * {{@link #downloadTestsFromRepo(String, boolean)} method.
	 * 
	 * This method might run for a long time, especially if the amount of
	 * repositories is above 30, since then two minutes need to be spent waiting
	 * for the search rate limit to reset.
	 * 
	 * @param startRepo the amount of repositories to skip
	 * @param amount
	 *            the amount of repositories to search for
	 * @param downloadAlways
	 *            if locally stored tests should be downloaded again
	 * @return how many tests were actually downloaded
	 * @throws URISyntaxException
	 *             if the search URL contains invalid characters
	 * @throws IOException
	 *             if the tests could not be stored or the properties file could
	 *             not be read
	 * @throws HttpException
	 *             if the request failed with a HTTP code of above 400
	 */
	public int downloadPopularTests(int startRepo, int amount, String searchTerms)
			throws URISyntaxException, IOException, HttpException {
		if (!loadAuthHeader(true)) {
			System.err.println(
					"No valid GitHub credentials set. This method will most likely surpass anonymous rate limits. Therefore aborting.");
			throw new HttpException("No authentication credentials provided for GitHub.");
		}

		// check up on the current rates for the GitHub api
		requestRemainingRates();

		int totalDownloaded = 0;
		int totalRepositories = 0;

		JsonArray searchItems = null;

		for (int i = startRepo; i < startRepo + amount; i++) {
			int j = i % pageSize;
			if (i == startRepo || j == 0) {
				int page = (i / pageSize) + 1;
				// request top rated repositories
				URI searchUrl = new URI("https://api.github.com/search/repositories?q=+language:" + language + "+" + URLEncoder.encode(searchTerms, "UTF-8")
						+ "&sort=stars&order=desc&per_page=" + pageSize + "&page=" + page);
				HttpGet searchRequest = new HttpGet(searchUrl);

				HttpResponse searchResponse = requestData(searchRequest);
				JsonObject searchObject = readJson(searchResponse).getAsJsonObject();
				searchItems = searchObject.getAsJsonArray("items");

				// set remaining search limit
				rateSearchRemaining = Integer
						.valueOf(searchResponse.getFirstHeader("X-RateLimit-Remaining").getValue());
			}

			// if theres no more elements left stop
			if (j >= searchItems.size()) {
				break;
			}

			// check if some search rate is remaining, if not wait for the
			// refresh (2 min)
			if (rateSearchRemaining <= 0) {
				waitForReset(2);
			}

			// get the current repo
			JsonObject repoObject = searchItems.get(j).getAsJsonObject();
			Integer repoId = repoObject.get("id").getAsInt();
			String repoName = repoObject.get("full_name").getAsString();
			String updatedAt = repoObject.get("updated_at").getAsString();
			String pushedAt = repoObject.get("pushed_at").getAsString();

			RepositoryContext newContext = new RepositoryContext(repoId, repoName, pushedAt, updatedAt);
			RepositoryContext oldContext = fileHandler.getRepoContext(repoName);

			// updated at refers to the repo object, so the language could have changed for example or the name
			if (oldContext == null || !oldContext.getUpdatedAt().equals(updatedAt)){
				String langUrl = repoObject.get("languages_url").getAsString();
				HttpGet langGet = new HttpGet(langUrl);
				HttpResponse langResp = requestData(langGet);
				JsonObject langObj = readJson(langResp).getAsJsonObject();
				
				for (Entry<String, JsonElement> entry : langObj.entrySet()){
					newContext.getLanguages().add(entry.getKey());
				}
			}

			fileHandler.createOrUpdateRepoContext(newContext);
			
			// refers to whether anything was pushed, if yes it could mean the features were updated
			if (oldContext == null || !oldContext.getPushedAt().equals(pushedAt)){
				int tests = downloadTestsFromRepo(repoName, false);
				totalDownloaded += tests;
				totalRepositories++;

				System.out.println("Downloaded " + tests + " test cases from " + repoName);
			} else {
				System.out.println("Skipping unchanged repository: " + repoName + ".");
			}
			
		}

		System.out.println(
				"Downloaded a total of " + totalDownloaded + " tests across " + totalRepositories + " repositories.");

		return totalDownloaded;
	}

	/**
	 * Utility method that requests the remaining rates the user has and stores
	 * them locally.
	 * 
	 * @throws URISyntaxException
	 *             should not happen
	 * @throws HttpResponseException
	 *             if the request returned an HTTP code above 400
	 * @throws IOException
	 *             if the request could not be parsed
	 */
	public void requestRemainingRates() throws URISyntaxException, HttpResponseException, IOException {
		URI rateUrl = new URI("https://api.github.com/rate_limit");
		HttpGet rateRequest = new HttpGet(rateUrl);

//		System.out.println("Requesting remaining rates from GitHub.");

		/**
		 * Example rate object <code>
		 * 	{
		 * 		"resources": {
		 *			"core": {
		 *				"limit": 5000,
		 *				"remaining": 4999,
		 *				"reset": 1372700873
		 *			},
		 *			"search": {
		 *				"limit": 30,
		 *				"remaining": 18,
		 *				"reset": 1372697452
		 *			}
		 *		},
		 *		"rate": {
		 *			"limit": 5000,
		 *			"remaining": 4999,
		 *			"reset": 1372700873
		 *		}
		 *	}
		 * </code>
		 */
		HttpResponse rateResponse = requestData(rateRequest);
		JsonObject rateObject = readJson(rateResponse).getAsJsonObject();
		JsonObject rateResourcesObject = rateObject.getAsJsonObject("resources");
		JsonObject rateCoreObject = rateResourcesObject.getAsJsonObject("core");
		JsonObject rateSearchObject = rateResourcesObject.getAsJsonObject("search");

		// get core rates (used for downloads)
		rateCoreRemaining = rateCoreObject.get("remaining").getAsInt();

		// get search rates (for queries and searches)
		rateSearchRemaining = rateSearchObject.get("remaining").getAsInt();

//		System.out.println(rateSearchRemaining + " search requests remaining.");
//		System.out.println(rateCoreRemaining + " download requests remaining.");
	}

	/**
	 * Will persist some user credentials for GitHub access for later usage.
	 * 
	 * Values can be null, but a userName and either password or token are
	 * necessary to be able to login.
	 * 
	 * @param userName
	 *            the user name on GitHub
	 * @param password
	 *            the password of the given user
	 * @param token
	 *            the OAuth access token of the user
	 * @throws IOException
	 *             if the credentials could not be stored
	 */
	public void setCredentials(String userName, String password, String token) throws IOException {
		Properties gitProps = new Properties();
		// gitProps.load(new FileInputStream(GIT_CONF_PATH));

		// no null values, instead save empty strings
		if (userName == null) {
			userName = "";
		}
		if (password == null) {
			password = "";
		}
		if (token == null) {
			token = "";
		}

		System.out.println("Overwriting GitHub credentials.");

		gitProps.setProperty("username", userName);
		gitProps.setProperty("password", password);
		gitProps.setProperty("token", token);

		gitProps.store(new FileOutputStream(GIT_CONF_PATH), "Properties that specify how to access GitHub.\n\n"
				+ "Needs 'username' and either 'token' or 'password' to authenticate the user.");

	}

	/**
	 * Load the locally stored properties file and try to set the authentication
	 * header for all further REST requests.
	 * 
	 * @param refresh
	 *            if a new header should be created regardless of an old one
	 *            existing
	 * @return if there exists a valid authentication header now
	 */
	public boolean loadAuthHeader(boolean refresh) {
		if (authHeader != null && !refresh) {
			System.out.println("No need to update authentication header.");
			return true;
		}

		Properties gitProps = new Properties();
		try {
			gitProps.load(new FileInputStream(GIT_CONF_PATH));

			String userName = gitProps.getProperty("username", "");
			String passwordOrToken = gitProps.getProperty("token", "");

			if (passwordOrToken.trim().isEmpty()) {
				passwordOrToken = gitProps.getProperty("password", "");
			}

			if (userName.trim().isEmpty() || passwordOrToken.trim().isEmpty()) {
				System.out.println("Either username or password  missing, can't create authentication header.");
				return false;
			}

			String encodedString = Base64.getEncoder()
					.encodeToString((userName + ":" + passwordOrToken).getBytes(ENCODING));
			authHeader = new BasicHeader("Authorization", "Basic " + encodedString);

			System.out.println("Valid authentication header created.");
			return true;
		} catch (IOException e) {
			System.err.println("Failed to open properties file to update auth header.");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Helper method that simplifies REST calls by packaging the common steps in
	 * this method.
	 * 
	 * @param request
	 *            is some HTTP request
	 * @return a JsonElement representing the whatever the request returned
	 * @throws IOException
	 *             if no data could be read
	 */
	private HttpResponse requestData(HttpRequestBase request) throws HttpResponseException, IOException {
		if (authHeader != null) {
			request.setHeader(authHeader);
		}

		HttpClient client = HttpClientBuilder.create().build();
		HttpResponse response = client.execute(request);

		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new HttpResponseException(response.getStatusLine().getStatusCode(),
					"Failed request with message: " + response.getStatusLine().getReasonPhrase());
		}

		return response;
	}

	/**
	 * Parses the HttpResponse into a JsonElement and returns that.
	 * 
	 * @param response
	 *            some HTTP response
	 * @return a JsonElement respresenting the body
	 * @throws IOException
	 *             if the response body could not be parsed
	 */
	private JsonElement readJson(HttpResponse response) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		JsonElement output = new JsonParser().parse(br);

		return output;
	}

	/**
	 * If the search rate limit is reached, the user needs to wait for a certain
	 * time (usually 2 minutes) for it to reset. This method accomplishes that.
	 * 
	 * @param time
	 *            the time in minutes to wait for
	 */
	private static void waitForReset(int time) {
		System.out.println("Waiting for " + time + " minutes for rate limits to reset...");
		try {
			Thread.sleep(time * 60 * 1000);
			System.out.println("Finished waiting.");
		} catch (InterruptedException e) {
			System.err.println("Got interrupted while waiting for search limit to reset.");
			e.printStackTrace();
		}
	}
}
