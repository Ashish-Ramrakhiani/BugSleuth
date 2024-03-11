package ramrakhiani.bugsleuth.config;

public class Configuration {

	public static String rootDirectory = null;
	public static String resultDirectory = "/home/ashish/BugSleuth/data/Time_results/SBIR_results_seed"; // absolute path to the output directory
	public static String defectsFilePath = null;	// path to the d4j-defects.txt file storing 815 defects in Defects4J~v2.0
	public static int k = 5; // size of the combined list
	public static int seed = 1; // seed specified for reproducibility
	public static int maxIter = 1000; // max #iterations allowed (default 1000)
	public static int convIn = 60;  // #consecutive iterations to decide if algorithm has converged (default: 7 for CE, 30 for GA)
	public static int popSize = 200; // population size in each generation for the GA (default 100)
	public static Double CP = 0.9;  // Cross-over probability for the GA (default 0.4)
	public static Double MP = 0.6; // Mutation probability for the GA

	public static void setParameters(String rootDirectory){
		defectsFilePath = rootDirectory + "/704Defects.txt";
		//resultDirectory = "/home/ashish/RAFL/RAFL_results/Junit";
	}
}