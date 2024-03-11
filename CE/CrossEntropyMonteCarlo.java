import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import java.io.File;
import java.util.Arrays;
import java.io.FileWriter;
import org.apache.commons.math3.random.RandomDataGenerator;

public class CrossEntropyMonteCarlo {
    public static RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
    public double[][] initializeProbabilities(int n, int k) {
        double[][] probabilities = new double[n][k];
        double initialValue = 1.0 / n;
        for (int i = 0; i < n; i++) {
            Arrays.fill(probabilities[i], initialValue);
        }
        return probabilities;
    }

    public List<int[][]> sampleMatrices(double[][] probabilities, int N) {
        int n = probabilities.length;
        int k = probabilities[0].length;
        List<int[][]> samples = new ArrayList<>();
        //RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        

        for (int i = 0; i < N; i++) {
            int[][] sample = new int[n][k];
            int[] row_sum = new int[n];
            for(int col = 0; col < k; col++){
                int col_sum = 0;
                for(int row = 0; row < n; row++){
                    if( row_sum[row] != 1){
                        int val = randomDataGenerator.nextBinomial(1, probabilities[row][col]);
                        sample[row][col] = val;
                        if(val == 1){
                            row_sum[row] = 1;
                            col_sum = 1;
                            break;
                        }
                    }
                }
                while(col_sum != 1){
                    int new_row = randomDataGenerator.nextInt(0, n-1);
                    if(row_sum[new_row] != 1){
                        sample[new_row][col] = 1;
                        col_sum = 1;
                        row_sum[new_row] = 1;
                    }
                }
            }
            samples.add(sample);
        }
        return samples;
    }

    public double[][] updateProbabilities(List<int[][]> sampledMatrices, double[][] probabilities, double rho, List<String[]> statements, int[] numStatementsByMethod) {
        Collections.sort(sampledMatrices, Comparator.comparingDouble(matrix -> computeObjectiveFunction(matrix, statements, numStatementsByMethod)));
        int quantileIndex = (int) (rho * sampledMatrices.size());
        int[][] quantileMatrix = sampledMatrices.get(quantileIndex);
        for (int i = 0; i < probabilities.length; i++) {
            for (int j = 0; j < probabilities[0].length; j++) {
                double sum = 0.0;
                for (int[][] sampledMatrix : sampledMatrices) {
                    if (computeObjectiveFunction(sampledMatrix, statements, numStatementsByMethod) <= computeObjectiveFunction(quantileMatrix, statements, numStatementsByMethod)) {
                        sum += sampledMatrix[i][j];
                    }
                }
                probabilities[i][j] = (sum + probabilities[i][j])  / (probabilities.length + sampledMatrices.size());
            }
        }
        return probabilities;
    }

    public double computeObjectiveFunction(int[][] matrix, List<String[]> statements, int[] numStatementsByMethod) {
        double distance = 0.0;
        for (int col = 0; col < matrix[0].length; col++) {
            for (int row = 0; row < matrix.length; row++) {
                if (matrix[row][col] == 1) {
                    String[] statement = statements.get(row);
                    for (int k = 1; k < statement.length; k++) {
                        int rank = Integer.parseInt(statement[k]);
                        if (rank == -1) {
                            distance += numStatementsByMethod[k - 1];
                        } else {
                            distance += Math.abs(col - rank);
                        }
                    }
                }
            }
        }
        return distance;
    }

    public static List<String[]> createListWithoutDuplicates(List<String>[] dataArray) {
        Set<String> uniqueItems = new HashSet<>();
        List<String[]> statements = new ArrayList<>();
        for (int col = 0; col < dataArray.length; col++) {
            List<String> columnList = dataArray[col];
            String[] column = columnList.toArray(new String[0]);
            Map<String, List<Integer>> itemRowMap = new HashMap<>();
            for (int row = 0; row < column.length; row++) {
                String item = column[row];
                itemRowMap.computeIfAbsent(item, k -> new ArrayList<>()).add(row);
            }
            for (Map.Entry<String, List<Integer>> entry : itemRowMap.entrySet()) {
                String item = entry.getKey();
                List<Integer> rowNumbers = entry.getValue();
                Collections.sort(rowNumbers);
                for (int rowNumber : rowNumbers) {
                    String[] statement = new String[dataArray.length + 1];
                    statement[0] = item;
                    Arrays.fill(statement, 1, statement.length, "-1");
                    statement[col + 1] = String.valueOf(rowNumber);
                    statements.add(statement);
                }
            }
            uniqueItems.addAll(itemRowMap.keySet());
        }
        statements.removeIf(statement -> !uniqueItems.remove(statement[0]));
        return statements;
    }

    public static List<String> getStatements(String filePath) {
        
        System.out.println("here");
        System.out.println(filePath);
        List<String> statements = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int k = 5;
            int num_statements = 0;
            while ((line = br.readLine()) != null) {
                if(num_statements != 0){
                    if (num_statements >= k + 1) {
                        break;
                    }
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        statements.add(parts[0].trim());
                        num_statements++;
                    }
                }else {
                    num_statements++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            e.printStackTrace();
        }
        return statements;
    }

    public static void printListArray(List<String>[] array) {
        System.out.println("in func");
        for (int i = 0; i < array.length; i++) {
            System.out.print("Element " + i + ": ");
            List<String> list = array[i];
            for (int j = 0; j < list.size(); j++) {
                System.out.print(list.get(j));
                if (j < list.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }

    public static void printStringArrayList(List<String[]> arrayList) {
        for (int i = 0; i < arrayList.size(); i++) {
            System.out.print("Element " + i + ": ");
            String[] array = arrayList.get(i);
            for (int j = 0; j < array.length; j++) {
                System.out.print(array[j]);
                if (j < array.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }

    private static double calculateMaxChange(double[][] matrix1, double[][] matrix2) {
        double maxChange = 0.0;
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                double change = Math.abs(matrix1[i][j] - matrix2[i][j]);
                if (change > maxChange) {
                    maxChange = change;
                }
            }
        }
        return maxChange;
    }

    public static int[] selectRowValues(double[][] probabilities) {
        int numRows = probabilities.length;
        int numCols = probabilities[0].length;
        int[] selectedRows = new int[numCols];
        Arrays.fill(selectedRows, -1);
        for (int j = 0; j < numCols; j++) {
            double maxProb = -1.0;
            int maxRow = -1;
            for (int i = 0; i < numRows; i++) {
                if (probabilities[i][j] > maxProb && !contains(selectedRows, i)) {
                    maxProb = probabilities[i][j];
                    maxRow = i;
                }
            }
            selectedRows[j] = maxRow;
        }

        return selectedRows;
    }
    private static boolean contains(int[] array, int value) {
        for (int num : array) {
            if (num == value) {
                return true;
            }
        }
        return false;
    }
    
    public static String[] listFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            System.err.println("Error: Not a directory");
            return null;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            System.err.println("Error: Could not list files in directory");
            return null;
        }

        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) { 
                fileNames[i] = files[i].getName();
            }
        }
        return fileNames;
    }

    public static void writeToFile(List<String[]> statements, int[] rankRows, int N, double rho, int iterations, String project, String defect) {
        String changeFile = "N=" + String.valueOf(N) + "_rho=" + String.valueOf(rho) + "_it=" + String.valueOf(iterations) + "/";
        String directoryPath = "/nfs/stak/users/bourassn/cs569/seed_956_363results/" + changeFile + project + "/" + defect;
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                System.err.println("Failed to create directory.");
                return;
            }
        }
        String filePath = directoryPath + File.separator + "stmt-susps.txt";
        File file = new File(filePath);
        try {
            FileWriter writer = new FileWriter(file);
            for (int i = 0; i < rankRows.length; i++) {
                writer.write(statements.get(rankRows[i])[0] + System.lineSeparator());
            }
            writer.close();
            System.out.println("File created successfully at: " + filePath);
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java Main defect number_of_lists <file_path> N rho iterationsNoChange");
            System.exit(1);
        }
        String[] defects = args[0].split("_");
        int numRefineVals = 5;
        int numMeathods = Integer.parseInt(args[1]);
        //int n = numMeathods;
        int k = 5;
        int N = Integer.parseInt(args[args.length - 3]); //2000 - 20000 -> step of 2000
        double rho = Double.parseDouble(args[args.length -2]);//0.01; //0.01 - 0.1
        double convergenceThreshold = 0.01;
        int maxIterations = 10000;
        int iteration = 0;
        int consecutiveSmallChange = 0; 
        int iterationsNoChang = Integer.parseInt(args[args.length - 1]); //convergence iteration 10-100

        List<String>[] dataArray = new ArrayList[numMeathods];
        int[] numStatementsByMethod = new int[numMeathods];
        //randomDataGenerator.reSeed(11);
        if(defects[1].equals("all")){
            String directoryPath ="/nfs/stak/users/bourassn/cs569/LLMAO_input/blues_results_2/" + defects[0]; //"/nfs/stak/users/bourassn/SBIR-ReplicationPackage/FaultLocalization/data/perfectFL_results/" + defects[0];
            String[] files = listFilesInDirectory(directoryPath);
            long startTime = System.nanoTime();
            for(int x = 0; x < files.length; x++){
              //  if(false == files[x].equals("58")){//fix this
                for (int i = 0; i < numMeathods; i++) {
                    String filePath = args[i + 2] + defects[0] + "/" + files[x] + "/stmt-susps-normalized.txt";
                    dataArray[i] = getStatements(filePath);
                    numStatementsByMethod[i] = dataArray[i].size();
                }
                //printListArray(dataArray);
                //System.out.println("len: " + dataArray.length);
                List<String[]> statements = createListWithoutDuplicates(dataArray);
                //printStringArrayList(statements);
                CrossEntropyMonteCarlo ceAlgorithm = new CrossEntropyMonteCarlo();
                
                ceAlgorithm.randomDataGenerator.reSeed(956);
                double[][] probabilities = ceAlgorithm.initializeProbabilities(statements.size(), k);
                double[][] prevProbabilities = probabilities;
                /*for (double[] row : probabilities) {
                    for (double value : row) {
                        System.out.print(value + " ");
                    }
                    System.out.println();
                }*/
                while(iteration < maxIterations){
                    List<int[][]> sampledMatrices = ceAlgorithm.sampleMatrices(probabilities, N);
                    probabilities = ceAlgorithm.updateProbabilities(sampledMatrices, probabilities, rho, statements, numStatementsByMethod);
                    /*for (double[] row : probabilities) {
                        for (double value : row) {
                            System.out.print(value + " ");
                        }
                        System.out.println();
                    }*/
                    double maxChange = calculateMaxChange(prevProbabilities,probabilities);
                    if(maxChange < convergenceThreshold){
                        consecutiveSmallChange++;
                        if(consecutiveSmallChange >= 30){
                            break;
                        }
                    }else{
                        consecutiveSmallChange = 0;
                    }
                    prevProbabilities = probabilities;
                    iteration++;
                }
                int[] rankRows = selectRowValues(probabilities);
                writeToFile(statements, rankRows, N, rho, iterationsNoChang, defects[0], files[x]);
               // }
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime)/1000000000;
            System.out.println("duration: ");
            System.out.println(duration);
        }else{

        }
    }
}

