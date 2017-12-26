package at.ac.uibk.testcase_analysis.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import at.ac.uibk.feature.model.Feature;
import at.ac.uibk.feature.model.FeatureBuilder;

public class FeatureLoader {

	/**
	 * Method to obtain all Features within a given package, divided by sub packages.
	 * 
	 * Uses reflection to load the given package and then obtain all classes
	 * that extend/implement FeatureBuilder, then instances those and calls
	 * the build() method. This returns a fully built Feature that will be
	 * returned in a Map, separated by sub package.
	 * 
	 * @param packagePath
	 *            a java package path
	 * @return a Map of instanced Feature classes
	 */
	public static Map<String, List<Feature>> loadAsMap(String packagePath) {
		Map<String, List<Feature>> features = new HashMap<String, List<Feature>>();
		Reflections ref = new Reflections(packagePath);

		Set<Class<? extends FeatureBuilder>> featureClasses = ref.getSubTypesOf(FeatureBuilder.class);
		int featureCount = 0;

		for (Class<? extends FeatureBuilder> clazz : featureClasses) {
			try {
				// instance class
				FeatureBuilder builder = clazz.getConstructor().newInstance(new Object[] {});
				// get package path
				Package p = clazz.getPackage();
				// subtract given package path
				String subPath = p.getName().replace(packagePath + ".", "");

				// in case its in the same dir the above replace wont work, so
				// set it manually
				if (subPath.equals(packagePath)) {
					subPath = "";
				}

				if (features.get(subPath) == null) {
					features.put(subPath, new ArrayList<Feature>());
				}

				features.get(subPath).add(builder.build());
				featureCount++;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// well, it failed so nothing i can do except print a notice,
				// should'nt happen anyways or the generator has serious issues
				System.err.println("Failed to instantiate '" + clazz.getName() + "'.");
				e.printStackTrace();
			}
		}

		System.out.println("Found " + featureCount + " features split over " + features.size()
				+ " repositories inside '" + packagePath + "'.");

		return features;
	}

	/**
	 * Method to obtain all Features within a given package.
	 * 
	 * Uses reflection to load the given package and then obtain all classes
	 * that extend/implement FeatureBuilder, then instances those and calls
	 * the build() method. This returns a fully built Feature that will be
	 * returned in a List.
	 * 
	 * @param packagePath
	 *            a java package path
	 * @return a List of instanced Feature classes
	 */
	public static List<Feature> loadAsList(String packagePath) {
		List<Feature> features = new ArrayList<Feature>();
		Reflections ref = new Reflections(packagePath);

		Set<Class<? extends FeatureBuilder>> featureClasses = ref.getSubTypesOf(FeatureBuilder.class);

		for (Class<? extends FeatureBuilder> clazz : featureClasses) {
			try {
				FeatureBuilder builder = clazz.getConstructor().newInstance(new Object[] {});
				features.add(builder.build());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// well, it failed so nothing i can do except print a notice,
				// should'nt happen anyways or the generator has serious issues
				System.err.println("Failed to instantiate '" + clazz.getName() + "'.");
				e.printStackTrace();
			}
		}

		System.out.println("Found " + features.size() + " features inside '" + packagePath + "'.");

		return features;
	}

}
