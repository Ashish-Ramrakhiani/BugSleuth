package ramrakhiani.bugsleuth.main;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;

import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.Optimize;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.PermutationChromosome;
import io.jenetics.Phenotype;
import io.jenetics.RouletteWheelSelector;
import io.jenetics.SwapMutator;
import io.jenetics.engine.Engine;
import io.jenetics.engine.Engine.Builder;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;
import ramrakhiani.bugsleuth.config.Configuration;
import io.jenetics.util.RandomRegistry;

import java.util.Comparator;
import java.util.HashMap;



public class RankAggregation {

    private static LinkedHashSet<String> allStatements = new LinkedHashSet<>();
    private static List<LinkedHashSet<String>> allRankLists = new ArrayList<>();


    private static Double fitnessFunction(Genotype<EnumGene<String>> gt) {

    	List<String> ranking = new ArrayList<>();
    	

    	Iterator<EnumGene<String>> it = gt.chromosome().iterator();
        while (it.hasNext()) {
            EnumGene<String> gene = it.next();
            ranking.add(gene.allele());
        }


         double totalDistance = 0.0;
        
         for (LinkedHashSet<String> rankList : allRankLists) {
        	 
        	 LinkedHashSet<String> union = new LinkedHashSet<>(ranking);
        	 union.addAll(ranking);
        	 union.addAll(rankList);
       	  	 
             double distance = 0.0;
         

             for (String statement : union) {
            	
                 int rankInGenotype = ranking.contains(statement) ? new ArrayList<>(ranking).indexOf(statement) + 1 : rankList.size() + 1;
                 int rankInList = rankList.contains(statement) ? new ArrayList<>(rankList).indexOf(statement) + 1 : rankList.size() + 1; 
                distance += Math.abs(rankInGenotype - rankInList);
                 
             }
       
             totalDistance += distance;
             
             union.clear();
             
         }

         return totalDistance;
    }
    
    private static void setParametersFromSettingsFile() throws IOException{
		File settings_file = new File("bugsleuth.settings");
		BufferedReader br = new BufferedReader(new FileReader(settings_file));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.contains("root"))
				Configuration.rootDirectory = line.split("=")[1].trim();
		}
		br.close();
		System.out.println("Setting root directory as: " + Configuration.rootDirectory);
		Configuration.setParameters(Configuration.rootDirectory);
	}
    
    public static void main(String[] args) throws Exception {
    	
    	
    	
    	
    	
    setParametersFromSettingsFile();	
   
    Boolean all_defects = false;
    	
    LinkedHashSet<String> defects_all = new LinkedHashSet<String>();
    	String defect = args[0].trim().toLowerCase();
    	if (args.length < 3) {
            System.out.println("Usage: java RankAggregation <defect> <number_of_files> <file1> <file2> ...");
            System.exit(1);
        }
    	 int numberOfFiles = Integer.parseInt(args[1]);
    	
    	String line,d;
    	if(defect.equals("all"))
    	{
    		all_defects = true;
    		File fl_result = new File(Configuration.defectsFilePath);
    		if (!fl_result.exists()) {
    			System.out.println("Error, results not found in the file path: "+Configuration.defectsFilePath);
    			System.exit(1);
    		}
    		try (BufferedReader reader = new BufferedReader(new FileReader(Configuration.defectsFilePath))){
    			while ((line = reader.readLine()) != null)
    			{
    				d = line.trim().toLowerCase();
    				if(!(d.isEmpty()))
    				{
    				defects_all.add( line.trim().toLowerCase());
    				}		
    			}
    			
    		}catch(IOException e) {e.printStackTrace();}
    	}
    	else {
    		defects_all.add(defect);
    	}
    	
    	long startTime = System.nanoTime();
    	
    	for(String defects: defects_all)
    	{
    		
    		defect = defects;
    		
    	allRankLists.clear();
    	allStatements.clear();
    	
    	System.out.println("Processing "+ defect + "defect");
        for (int i = 2; i <= numberOfFiles+1; i++) {
        	allRankLists.add(new LinkedHashSet<>());
            String filePath = args[i];
            
            
            File flPath = new File(filePath);
            if (!flPath.exists())
            {
            	System.out.println(filePath + " file path does not exist");
            	System.exit(1);
            }

            readStatementsFromFile(defect,filePath,i-2);
        }  
        if(allRankLists.size()!= numberOfFiles)
        {
        	System.out.println("Not enough input results found for "+defect);
        	continue;
        }
        
            Factory<Genotype<EnumGene<String>>> genotype = Genotype.of(
                    PermutationChromosome.of(ISeq.of(allStatements), Configuration.k)
            );

            
        
     Builder<EnumGene<String>, Double> builder = Engine.builder(
                RankAggregation::fitnessFunction,
                genotype
        )
        .optimize(Optimize.MINIMUM)
        .populationSize(Configuration.popSize)
        .selector(new RouletteWheelSelector<>())
        .alterers(new PartiallyMatchedCrossover<>(Configuration.CP), new SwapMutator<>(Configuration.MP)).executor(Runnable::run);
     

        EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();

        Engine<EnumGene<String>, Double> engine = builder.build();
        
        Phenotype<EnumGene<String>, Double> best =  RandomRegistry.with(RandomGeneratorFactory.of("L128X1024MixRandom").create(Configuration.seed), r->engine.stream()
                .limit(Limits.bySteadyFitness(Configuration.convIn))
                .limit(Configuration.maxIter)
                .peek(statistics)
                .collect(EvolutionResult.toBestPhenotype()));
        
        EvolutionResult<EnumGene<String>, Double> finalResult =  RandomRegistry.with(RandomGeneratorFactory.of("L128X1024MixRandom").create(Configuration.seed), r->engine.stream()
                .limit(Limits.bySteadyFitness(Configuration.convIn))
                .limit(Configuration.maxIter)
                .peek(statistics)
                .collect(EvolutionResult.toBestEvolutionResult()));

        System.out.println("Best Phenotype: " + best);
        
        
        saveResultsToFile(defect,best);
    	}
    	long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000000; 
        if(all_defects == true)
        {
        	defect = "All defects";
        }
        System.out.println("Total execution time for " + defect + " took " + duration + " seconds");
    }
    
	private static HashMap<String, Double> sortByValues(HashMap<String, Double> map) {
		List<Object> list = new LinkedList<Object>(map.entrySet());

		Collections.sort(list, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		Collections.reverse(list);

		double maxscore = Double.parseDouble(list.get(0).toString().split("=")[1].trim());
		double minscore = Double.parseDouble(list.get(list.size() - 1).toString().split("=")[1].trim());

		HashMap<String, Double> sortedHashMap = new LinkedHashMap<String, Double>();
		for (Iterator<Object> it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Double score = (Double) entry.getValue();
			Double normalizedScore = (score - minscore) / (maxscore - minscore);
			sortedHashMap.put((String) entry.getKey(), normalizedScore);
		}
		return sortedHashMap;
	}
	
    private static void readStatementsFromFile(String defect,String filePath, int rankListIndex) {
    	
    	
        String result_file = filePath + "/" + defect.split("_")[0].toLowerCase() + "/"
				+ defect.split("_")[1] + "/stmt-susps.txt";
		File fl_result = new File(result_file);
		if (!fl_result.exists()) {
			System.out.println("Error, results not found in the file path: "+result_file);
			
		}
		else
		{
		HashMap<String, Double> FL = new HashMap<String, Double>();
		HashMap<String, Double> sortedFL = new HashMap<String, Double>();
		
        try (BufferedReader reader = new BufferedReader(new FileReader(result_file))) {
            String line;
            
            
            LinkedHashSet<String> uniqueStatements = new LinkedHashSet<>();
            uniqueStatements.clear();
            
            
            reader.readLine();
           
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                	double score = Double.parseDouble(parts[1]);
                	
                	if(score > 0.0)
                	{
                		String statement = parts[0].trim();
                	
                		if(!FL.containsKey(statement))
                		{	
                			FL.put(statement, score);
                		}
                		else {
                			if(FL.get(statement) < score) {
                				FL.put(statement, score);
                			
                		}
                    
                	 }
                	
                		}
                	}
            }
            
            if(!FL.isEmpty())
            {
            sortedFL = sortByValues(FL);
       
            int counter = 0;
            for(String stmt: sortedFL.keySet())
            {
            	if(counter == Configuration.k)
            		break;
            	
            	uniqueStatements.add(stmt);
            	
            	counter ++;
            	
            }
            allStatements.addAll(uniqueStatements);
            allRankLists.get(rankListIndex).addAll(uniqueStatements);
        
            uniqueStatements.clear();
            }
            

        } catch (IOException e) {
            e.printStackTrace();
        }
		}
    }
    
    private static void saveResultsToFile(String defect,Phenotype<EnumGene<String>, Double> bestPhenotype) {
    	String result_file = Configuration.resultDirectory+"/BugSleuth_results_seed"+Configuration.seed + "/" + defect.split("_")[0].toLowerCase() + "/"
				+ defect.split("_")[1] + "/stmt-susps.txt";
    	File rfile = new File(result_file);
    	
		rfile.getParentFile().mkdirs();
		
  	
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(rfile))) {
		
		for (EnumGene<String> gene : bestPhenotype.genotype().chromosome().stream().collect(Collectors.toList())) {
		    writer.write(gene.allele());
		    writer.newLine(); 
		}

		System.out.println("Results saved to "+ result_file);
      } catch (IOException e) {
		e.printStackTrace();
      }

    }
    
}
