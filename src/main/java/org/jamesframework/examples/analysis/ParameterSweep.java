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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.jamesframework.core.search.Search;
import org.jamesframework.core.search.algo.MetropolisSearch;
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
 * Performs a parameter sweep using the analysis tools from the extensions module (example 5A).
 * The core subset selection problem is used as a case study, and the entry-to-nearest-entry
 * objective from example 1C is maximized. Different Metropolis searches with a variety of
 * fixed temperatures are applied to find an appropriate temperature range to be used for a
 * parallel tempering search (see example 5B).
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ParameterSweep {
    
    /**
     * Runs the analysis. Expects a variable number of parameters: (1) the desired selection ratio (real value
     * in [0,1]) which determines the core size, (2) the number of runs (i.e. repeats) per search (3) the runtime
     * limit (in seconds) of each search run, (4) the minimum temperature, (5) the maximum temperature, (6) the
     * number of applied Metropolis searches and (7+) the input file paths of the datasets for which the analysis
     * is to be performed. The input files are specified in a CSV file in which the first row (header) lists the
     * N item names and the subsequent N rows describe a symmetric (N x N) distance matrix. The distance matrix
     * indicates the distance between each pair of items, where the rows follow the same order as the columns,
     * as indicated by the header row.
     * 
     * @param args array containing the desired selection ratio, number of search runs, runtime limit per run,
     *             minimum temperature, maximum temperature, number of applied Metropolis searches and data set
     *             file paths
     */
    public static void main(String[] args) {
        System.out.println("#############################");
        System.out.println("# ANALYSIS: PARAMETER SWEEP #");
        System.out.println("#############################");
        // parse arguments
        if(args.length < 7){
            System.err.println("Usage: java -cp james-examples.jar "
                    + "org.jamesframework.examples.analysis.ParameterSweep "
                    + "<selection-ratio> <runs> <runtime> <mintemp> <maxtemp> "
                    + "<numsearches> [<inputfile>]+");
            System.exit(1);
        }
        double selRatio = Double.parseDouble(args[0]);
        int runs = Integer.parseInt(args[1]);
        int timeLimit = Integer.parseInt(args[2]);
        double minTemp = Double.parseDouble(args[3]);
        double maxTemp = Double.parseDouble(args[4]);
        int n = Integer.parseInt(args[5]);
        List<String> filePaths = new ArrayList<>();
        for(int i=6; i<args.length; i++){
            filePaths.add(args[i]);
        }
        run(filePaths, selRatio, runs, timeLimit, minTemp, maxTemp, n);
    }
    
    private static void run(List<String> filePaths, double selRatio, int runs,
            int timeLimit, double minTemp, double maxTemp, int numSearches){
        
        // read datasets
        System.out.println("# PARSING INPUT");
        CoreSubsetFileReader reader = new CoreSubsetFileReader();
        List<CoreSubsetData> datasets = new ArrayList<>();
        for(String filePath : filePaths){
            System.out.println("Reading file: " + filePath);
            try {
                CoreSubsetData data = reader.read(filePath);
                datasets.add(data);
            } catch (FileNotFoundException ex) {
                System.err.println("Failed to read file: " + filePath);
                System.exit(2);
            }
        }
        
        // create objective
        EntryToNearestEntryObjective obj = new EntryToNearestEntryObjective();
        
        // initialize analysis object
        Analysis<SubsetSolution> analysis = new Analysis<>();
        
        // ADD PROBLEMS (ONE PER DATA SET)
        System.out.println("# ADDING PROBLEMS TO ANALYSIS");
        
        for(int d = 0; d < datasets.size(); d++){
            // create problem
            CoreSubsetData data = datasets.get(d);
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
        
        double tempDelta = (maxTemp - minTemp)/(numSearches - 1);
        DecimalFormat df = new DecimalFormat("#.################");
        for(int s = 0; s < numSearches; s++){
            // compute temperature
            double temp = minTemp + s * tempDelta;
            // add Metropolis search
            System.out.format("Add Metropolis search (temp: %s)\n", df.format(temp));
            String id = "MS-" + (s+1);
            analysis.addSearch(id, problem -> {
                Search<SubsetSolution> ms = new MetropolisSearch<>(problem, new SingleSwapNeighbourhood(), temp);
                ms.addStopCriterion(stopCrit);
                return ms;
            });
        }
        // set number of search runs
        analysis.setNumRuns(runs);
                
        // start loader
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
        
        // run analysis
        System.out.format("# RUNNING ANALYSIS (runs per search: %d)\n", runs);
        
        AnalysisResults<SubsetSolution> results = analysis.run();
        
        // stop loader
        loaderTask.cancel();
        loaderTimer.cancel();
        System.out.println("# Done!");
        
        // write to JSON
        System.out.println("# WRITING JSON FILE");
        String jsonFile = "sweep.json";
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
