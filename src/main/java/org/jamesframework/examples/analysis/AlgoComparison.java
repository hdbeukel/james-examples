/*
 * Copyright 2015 Ghent University, Bayer CropScience.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jamesframework.examples.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.jamesframework.core.search.Search;
import org.jamesframework.core.search.algo.ParallelTempering;
import org.jamesframework.core.search.algo.RandomDescent;
import org.jamesframework.core.search.stopcriteria.MaxRuntime;
import org.jamesframework.core.search.stopcriteria.StopCriterion;
import org.jamesframework.core.subset.SubsetProblem;
import org.jamesframework.core.subset.SubsetSolution;
import org.jamesframework.core.subset.neigh.SingleSwapNeighbourhood;
import org.jamesframework.examples.coresubset.CoreSubsetData;
import org.jamesframework.examples.coresubset.CoreSubsetFileReader;
import org.jamesframework.examples.coresubset3.EntryToNearestEntryObjective;
import org.jamesframework.ext.analysis.Analysis;
import org.jamesframework.ext.analysis.AnalysisResults;
import org.jamesframework.ext.analysis.JsonConverter;

/**
 * Compares algorithm performance using the analysis tools from the extensions module (example 5B).
 * The core subset selection problem is used as a case study, and the entry-to-nearest-entry
 * objective from example 1C is maximized. Both random descent as well as parallel tempering
 * are applied to solve the problem for a series of given data sets.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class AlgoComparison {
    
    /**
     * Runs the analysis. Expects a variable number of parameters: (1) the desired selection ratio (real value
     * in [0,1]) which determines the core size, (2) the number of runs (i.e. repeats) per search (3) the runtime
     * limit (in seconds) of each search run and (4+) the input file paths of the datasets for which the analysis
     * is to be performed. The input files are specified in a CSV file in which the first row (header) lists the
     * N item names and the subsequent N rows describe a symmetric (N x N) distance matrix. The distance matrix
     * indicates the distance between each pair of items, where the rows follow the same order as the columns,
     * as indicated by the header row.
     * 
     * @param args array containing the desired selection ratio, number of search runs, runtime limit per run
     *             and the data set file paths
     */
    public static void main(String[] args) {
        System.out.println("###########################################");
        System.out.println("# ANALYSIS: COMPARE ALGORITHM PERFORMANCE #");
        System.out.println("###########################################");
        // parse arguments
        if(args.length < 4){
            System.err.println("Usage: java -cp james-examples.jar "
                    + "org.jamesframework.examples.analysis.AlgoComparison "
                    + "<selection-ratio> <runs> <runtime> [<inputfile>]+");
            System.exit(1);
        }
        double selRatio = Double.parseDouble(args[0]);
        int runs = Integer.parseInt(args[1]);
        int timeLimit = Integer.parseInt(args[2]);
        List<String> filePaths = new ArrayList<>();
        for(int i=3; i<args.length; i++){
            filePaths.add(args[i]);
        }
        run(filePaths, selRatio, runs, timeLimit);
    }
    
    private static void run(List<String> filePaths, double selRatio, int runs, int timeLimit){
        
        // read data sets
        System.out.println("# PARSING INPUT");
        List<CoreSubsetData> dataSets = new ArrayList<>();
        for(String filePath : filePaths){
            System.out.println("Reading file: " + filePath);
            try {
                CoreSubsetData data = new CoreSubsetFileReader().read(filePath);
                dataSets.add(data);
            } catch (FileNotFoundException ex) {
                System.err.println("Failed to read file: " + filePath);
                System.exit(2);
            }
        }
        
        // create objective
        EntryToNearestEntryObjective obj = new EntryToNearestEntryObjective();
        
        // initialize analysis object
        Analysis<SubsetSolution> analysis = new Analysis<>();
        // set number of runs
        analysis.setNumRuns(runs);
        
        // ADD PROBLEMS (ONE PER DATA SET)
        System.out.println("# ADDING PROBLEMS TO ANALYSIS");
        
        for(int d=0; d<dataSets.size(); d++){
            // create problem
            CoreSubsetData data = dataSets.get(d);
            // set core size
            int coreSize = (int) Math.round(selRatio * data.getIDs().size());
            SubsetProblem<CoreSubsetData> problem = new SubsetProblem<>(obj, data, coreSize);
            // set problem ID to file name (without directories and without extension)
            String path = filePaths.get(d);
            String filename = new File(path).getName();
            String id = filename.substring(0, filename.lastIndexOf("."));
            System.out.println("Add problem \"" + id + "\" (read from: " + path + ")");
            analysis.addProblem(id, problem);
        }
        
        // ADD SEARCHES
        System.out.println("# ADDING SEARCHES TO ANALYSIS");

        // create stop criterion
        StopCriterion stopCrit = new MaxRuntime(timeLimit, TimeUnit.SECONDS);
        
        // add random descent
        System.out.println("Add random descent");
        analysis.addSearch("Random Descent", problem -> {
            Search<SubsetSolution> rd = new RandomDescent<>(problem, new SingleSwapNeighbourhood());
            rd.addStopCriterion(stopCrit);
            return rd;
        });
        
        // add parallel tempering
        System.out.println("Add parallel tempering");
        analysis.addSearch("Parallel Tempering", problem -> {
            double minTemp = 1e-8;
            double maxTemp = 3e-4;
            int numReplicas = 10;
            Search<SubsetSolution> pt = new ParallelTempering<>(problem,
                                                                new SingleSwapNeighbourhood(),
                                                                numReplicas, minTemp, maxTemp);
            pt.addStopCriterion(stopCrit);
            return pt;
        });
        
        // run analysis
        System.out.format("# RUNNING ANALYSIS (runs per search: %d)\n", runs);
        
        Timer loaderTimer = new Timer();
        TimerTask loaderTask = new TimerTask() {
            private char[] loader = new char[40];
            private int l = 6;
            private int p = 0;

            @Override
            public void run() {
                printLoader(loader);
                p = (p+1)%loader.length;
                updateLoader(loader, p, l);
            }
        };
        loaderTimer.schedule(loaderTask, 0, 100);
        
        AnalysisResults<SubsetSolution> results = analysis.run();
        
        // stop loader
        loaderTask.cancel();
        loaderTimer.cancel();
        System.out.println("# Done!");
        
        // write to JSON
        System.out.println("# WRITING JSON FILE");
        String jsonFile = "AlgoComparison.json";
        try {
            results.writeJSON(jsonFile, JsonConverter.SUBSET_SOLUTION);
        } catch (IOException ex) {
            System.err.println("Failed to write JSON file: " + jsonFile);
            System.exit(3);
        }
        System.out.println("# Wrote \"" + jsonFile + "\"");
        
    }
    
    private static void updateLoader(char[] loader, int start, int loaderLength){
        Arrays.fill(loader, ' ');
        for(int t=0; t<loaderLength; t++){
            int pos = (start+t) % (loader.length);
            loader[pos] = '-';
        }
    }
    
    private static void printLoader(char[] loader){
        for(char c : loader){
            System.out.print(c);
        }
        System.out.print("\r");
    }

}
