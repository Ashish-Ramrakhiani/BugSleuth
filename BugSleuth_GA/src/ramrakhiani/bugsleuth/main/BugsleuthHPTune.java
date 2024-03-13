package ramrakhiani.bugsleuth.main;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import java.util.Arrays;
import java.util.Collection;
import ramrakhiani.bugsleuth.config.Configuration;

@RunWith(Parameterized.class)
public class BugsleuthHPTune {

    private double CP;
    private double MP;
    //private int convIn;
    private int popSize;

    public BugsleuthHPTune(double CP, double MP, int popSize) {
        this.CP = CP;
        this.MP = MP;
        this.popSize = popSize;
        //this.convIn = convIn;
    }

    @Parameters
    public static Collection<Object[]> data() {
        // Generate all possible combinations of CP, MP, popSize, and convIn parameters
        int numCombinations = (int) 1000;
        Object[][] data = new Object[numCombinations][3];
        int index = 0;

        for (double CP = 0.1; CP <= 1.0; CP += 0.1) {
            for (double MP = 0.1; MP <= 1.0; MP += 0.1) {
                for (int popSize = 50; popSize <= 500; popSize += 50) {
                        data[index] = new Object[]{CP, MP, popSize};
                        index++;
                    
                }
            }
        }
        return Arrays.asList(data);
    }

    @Test
    public void testRunWithDifferentConfigurations() {
        Configuration.popSize = popSize;
        Configuration.CP = CP;
        Configuration.MP = MP;
        //Configuration.convIn = convIn;

        // Set the result directory based on the current configuration
       // Configuration.resultDirectory = "/home/ashish/Bugsleuth/Bugsleuth_fintune_results/JacksonDatabind/CP=" + String.format("%.2f", CP) + "_MP=" + String.format("%.2f", MP) + "_popSize=" + popSize + "_convIn=" + convIn;
        
        	Configuration.resultDirectory = "/home/ashish/BugSleuth/BugSleuth_fintune_results_2/JacksonDatabind/CP=" + String.format("%.2f", CP) + "_MP=" + String.format("%.2f", MP) + "_popSize=" + popSize + "/";
        
        	
        // Your test code here
        // Call RankAggregation.main with the configured parameters and assert the expected behavior.
        // Example: RankAggregation.main(new String[]{"all", "2", "/home/ashish/SBIR-ReplicationPackage/FaultLocalization/data/SBFL_results", "/home/ashish/SBIR-ReplicationPackage/FaultLocalization/data/blues_results", "1", "10000"});

        // Run RankAggregation.main with the specific run configuration
        try {
            RankAggregation.main(new String[]{"all", "2", "/home/ashish/SBIR-ReplicationPackage/FaultLocalization/data/SBFL_results_jacksondatabind", "/home/ashish/SBIR-ReplicationPackage/FaultLocalization/data/blues_results_jacksondatabind"}); // Modify the arguments as needed
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }
