# Rank Aggregation for Effective and Efficient Fault Localization

The repository contains source code for BugSleuth.BugSleuth is an unsupervised fault localization technique to combine rank lists of suspicious statements of multiple fault localization techniques for effective and efficient fault localization thereby improving a practitioner's productivity. BugSleuth provides two variants namely BugSleuth-GA that utilizes Genetic Algorithm for Rank Aggregation and BugSleuth-CE that utilizes cross entropy Monte Carlo Algorithm

## Installation

### Dependencies for BugSleuth-GA
- Java version 17
- Jenetics version 7.2.0
- [Defects4J version 2.0.0](https://github.com/rjust/defects4j)

## How to run BugSleuth-GA:

1. After cloning the repository,Import the project in Eclipse and specify the absolute path to the cloned repository as the `root_directory` in the `bugleuth.settings` file.
2. After importing the project, make sure you are running the project in the Java 17 environment as well as the jenetics library dependency is configured
3. Run `RankAggregation.java` with the following command line arguments
4. BugSleuth-GA accepts the following command line arguments:
   - `<Defects4J project>_<bugid>` (e.g., Chart_1 (to run BugSleuth-GA on a particular Defects4J defect) or `all` (to run BugSleuth-GA on all Defects4J defects))
   - `NumOfFLTechniques` (the number of FL techniques' whose results are to be combined. This value should be at least 2) Based on the value specified, the     following arguments must specify the path to the files that store the FL results. For example, when combining the results of SBFL and Blues FL techniques, the next two arguments will be:
   - `<path-to-SBFL_results>`
   - `<path-to-Blues_results>`
## How to experiment using different configuration parameters
   - Import the project in Eclipse and follow steps described above.
   - Use the main function defined in `RankAggregation.java` to launch BugSleuth-GA by providing the command line arguments described in 4 above.
   - To experiment with different configuration update the parameters in file `Configuration.java`.


