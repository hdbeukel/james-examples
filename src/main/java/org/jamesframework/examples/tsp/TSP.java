/*
 * Copyright 2014 Ghent University, Bayer CropScience.
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

package org.jamesframework.examples.tsp;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.jamesframework.core.problems.GenericProblem;
import org.jamesframework.core.problems.Problem;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.problems.sol.RandomSolutionGenerator;
import org.jamesframework.core.search.LocalSearch;
import org.jamesframework.core.search.algo.ParallelTempering;
import org.jamesframework.core.search.algo.RandomDescent;
import org.jamesframework.core.search.stopcriteria.MaxRuntime;
import org.jamesframework.examples.util.ProgressSearchListener;

/**
 * Main class for the travelling salesman example (example 4A).
 * This version implements the problem by extending {@link GenericProblem}
 * to separate the data from the objective (and possible constraints).
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class TSP {
    
    // specify random solution generator
    public static final RandomSolutionGenerator<TSPSolution, TSPData> RANDOM_SOLUTION_GENERATOR = (rnd, data) -> {
        // create random permutation of cities
        List<Integer> cities = new ArrayList<>();
        int n = data.getNumCities();
        for(int i=0; i<n; i++){
            cities.add(i);
        }
        Collections.shuffle(cities, rnd);
        // create and return TSP solution
        return new TSPSolution(cities);
    };
    
    /**
     * Solves a (symmetric) travelling salesman problem. Expects two parameters: (1) the input file path and
     * (2) the runtime limit (in seconds). The input is specified in a text file in which the first row contains
     * a single integer value indicating the number of cities. The remainder of the file contains the entries of
     * the lower triangular part of a symmetric distance matrix (row-wise without diagonal entries), separated
     * by whitespace and/or newlines.
     * 
     * @param args array containing the input file path and runtime limit
     */
    public static void main(String[] args) {
        System.out.println("###############################");
        System.out.println("# TRAVELLING SALESMAN PROBLEM #");
        System.out.println("###############################");
        // parse arguments
        if(args.length != 2){
            System.err.println("Usage: java -cp james-examples.jar org.jamesframework.examples.tsp.TSP <inputfile> <runtime>");
            System.exit(1);
        }
        String filePath = args[0];
        int timeLimit = Integer.parseInt(args[1]);
        run(filePath, timeLimit);
    }
    
    private static void run(String filePath, int timeLimit){
        
        /***************/
        /* PARSE INPUT */
        /***************/
        
        System.out.println("# PARSING INPUT");
        System.out.println("Reading file: " + filePath);
        
        try {
            
            /*************************/
            /* PROBLEM SPECIFICATION */
            /*************************/
            
            // read data (distance matrix)
            TSPData data = new TSPFileReader().read(filePath);
            // create objective
            TSPObjective obj = new TSPObjective();
            
            // wrap in generic problem
            Problem<TSPSolution> problem = new GenericProblem<>(data, obj, RANDOM_SOLUTION_GENERATOR);        
            
            System.out.println("# OPTIMIZING TSP ROUND TRIP");

            System.out.println("Number of cities: " + data.getNumCities());
            System.out.println("Time limit: " + timeLimit + " seconds");
            
            /******************/
            /* RANDOM DESCENT */
            /******************/

            System.out.println("# RANDOM DESCENT");
            
            // create random descent search with TSP neighbourhood
            LocalSearch<TSPSolution> randomDescent = new RandomDescent<>(problem, new TSP2OptNeighbourhood());
            // set maximum runtime
            randomDescent.addStopCriterion(new MaxRuntime(timeLimit, TimeUnit.SECONDS));
            // attach listener
            randomDescent.addSearchListener(new ProgressSearchListener());

            // start search
            randomDescent.start();
            
            // print results
            Evaluation randomDescentBestEval = null;
            if(randomDescent.getBestSolution() != null){
                System.out.println("Best round trip: "
                                        + randomDescent.getBestSolution().getCities());
                randomDescentBestEval = randomDescent.getBestSolutionEvaluation();
                System.out.println("Best round trip travel distance: "
                                        + randomDescentBestEval);
            } else {
                System.out.println("No valid solution found...");
            }

            // dispose
            randomDescent.dispose();
            
            /**********************/
            /* PARALLEL TEMPERING */
            /**********************/
            
            System.out.println("# PARALLEL TEMPERING");

            // set temperature range, scaled according to average
            // distance between cities and their nearest neighbours
            double scale = computeAvgNearestNeighbourDistance(data);
            double minTemp = scale * 1e-8;
            double maxTemp = scale * 0.6;
            // create parallel tempering search with TSP neighbourhood
            int numReplicas = 10;
            ParallelTempering<TSPSolution> parallelTempering = new ParallelTempering<>(
                                                                    problem,
                                                                    new TSP2OptNeighbourhood(),
                                                                    numReplicas, minTemp, maxTemp
                                                               );
            
            // set maximum runtime
            parallelTempering.addStopCriterion(new MaxRuntime(timeLimit, TimeUnit.SECONDS));
            // attach listener
            parallelTempering.addSearchListener(new ProgressSearchListener());

            // start search
            parallelTempering.start();
            
            // print results
            Evaluation ptBestEval = null;
            if(parallelTempering.getBestSolution() != null){
                System.out.println("Best round trip: "
                                        + parallelTempering.getBestSolution().getCities());
                ptBestEval = parallelTempering.getBestSolutionEvaluation();
                System.out.println("Best round trip travel distance: "
                                        + ptBestEval);
            } else {
                System.out.println("No valid solution found...");
            }

            // dispose
            parallelTempering.dispose();
            
            /***********/
            /* SUMMARY */
            /***********/

            System.out.println("---------------------------------------");
            System.out.println("Summary:");
            System.out.println("---------------------------------------");

            System.out.println("Number of cities: " + data.getNumCities());
            System.out.println("Time limit: " + timeLimit + " seconds");
            System.out.println("---------------------------------------");

            DecimalFormat df = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US));
            System.out.format("%20s    %15s \n", "", "Travel distance");
            System.out.format("%20s    %15s \n",
                                "Random descent:",
                                randomDescentBestEval != null ? df.format(randomDescentBestEval.getValue()) : "-");
            System.out.format("%20s    %15s \n",
                                "Parallel tempering:",
                                ptBestEval != null ? df.format(ptBestEval.getValue()) : "-");
            System.out.println("---------------------------------------");
            
        } catch (FileNotFoundException ex) {
            System.err.println("Failed to read file: " + filePath);
            System.exit(2);
        }
        
    }

    // compute average nearest neighbour distance
    private static double computeAvgNearestNeighbourDistance(TSPData data){
        int n = data.getNumCities();
        double sum = 0.0;
        for(int i=0; i<n; i++){
            double min = Double.MAX_VALUE;
            for(int j=0; j<n; j++) {
                double dist = data.getDistance(i, j);
                if(dist > 0.0 && dist < min){
                    min = dist;
                }
            }
            sum += min;
        }
        return sum/n;
    }
    
}
