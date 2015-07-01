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

package org.jamesframework.examples;

/**
 * Main class of the JAMES examples module that prints an overview of the
 * implemented examples when running Java on the distrubuted JAR-file.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("##################");
        System.out.println("# JAMES EXAMPLES #");
        System.out.println("##################");
        System.out.println("");
        System.out.println("Example 1A: core subset selection");
        System.out.println("--------------------------------");
        System.out.println("");
        System.out.println("Given a distance matrix, sample a subset of fixed size with maximum average \n"
                         + "distance between every pair of selected items. The random descent algorithm \n"
                         + "is applied for optimization of the selected core subset.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("java -cp james-examples.jar "
                         + "org.jamesframework.examples.coresubset.CoreSubset "
                         + "<inputfile> <subsetsize> <runtime>");
        System.out.println("");
        System.out.println("Example 1B: core subset selection (2)");
        System.out.println("-------------------------------------");
        System.out.println("");
        System.out.println("Same problem and algorithm as for example 1A, where the objective \n"
                         + "has been extended to include an efficient delta evaluation.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("java -cp james-examples.jar "
                         + "org.jamesframework.examples.coresubset2.CoreSubset2 "
                         + "<inputfile> <subsetsize> <runtime>");
        System.out.println("");
        System.out.println("Example 1C: core subset selection (3)");
        System.out.println("-------------------------------------");
        System.out.println("");
        System.out.println("A core selection problem where the average distance of each selected item \n"
                         + "to the closest other selected item is maximized. This objective produces \n"
                         + "custom evaluation objects that track metadata used for efficient delta \n"
                         + "evaluation. The random descent and parallel tempering algorithms are applied \n"
                         + "to optimize the core.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("java -cp james-examples.jar "
                         + "org.jamesframework.examples.coresubset3.CoreSubset3 "
                         + "<inputfile> <subsetsize> <runtime>");
        System.out.println("");
        System.out.println("Example 2A: the 0/1 knapsack problem");
        System.out.println("-----------------------------------");
        System.out.println("");
        System.out.println("Given a series of items with a specific profit and weight, select a subset \n"
                         + "of these items with maximum total profit without exceeding the capacity \n"
                         + "(maximum total weight) of the knapsack. Both random descent and parallel \n"
                         + "tempering are applied for optimization of the knapsack, with a mandatory \n"
                         + "constraint on the total weight of the selection.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("java -cp james-examples.jar "
                         + "org.jamesframework.examples.knapsack.KnapSack "
                         + "<inputfile> <capacity> <runtime>");
        System.out.println("");
        System.out.println("Example 2B: the 0/1 knapsack problem (2)");
        System.out.println("----------------------------------------");
        System.out.println("");
        System.out.println("In this example, the 0/1 knapsack is solved using a penalizing instead of \n"
                         + "a mandatory constraint. The parallel tempering algorithm is applied to optimize \n"
                         + "the selection.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("java -cp james-examples.jar "
                         + "org.jamesframework.examples.knapsack2.KnapSack2 "
                         + "<inputfile> <capacity> <runtime>");
        System.out.println("");
        System.out.println("Example 3: the maximum clique problem");
        System.out.println("-------------------------------------");
        System.out.println("");
        System.out.println("A random descent and variable neighbourhood search are applied to find a \n"
                         + "maximum clique in a given graph, using a custom greedy neighbourhood.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("java -cp james-examples.jar "
                         + "org.jamesframework.examples.clique.MaximumClique "
                         + "<inputfile> <maxshake> <runtime>");
        System.out.println("");
        System.out.println("Example 4A: the travelling salesman problem");
        System.out.println("------------------------------------------");
        System.out.println("");
        System.out.println("This example demonstrates a basic implementation of the symmetric TSP problem, \n"
                         + "which is not a subset selection but a permutation problem.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("java -cp james-examples.jar "
                         + "org.jamesframework.examples.tsp.TSP "
                         + "<inputfile> <runtime>");
        System.out.println("");
        System.out.println("Example 4B: the travelling salesman problem (2)");
        System.out.println("-----------------------------------------------");
        System.out.println("");
        System.out.println("Reconsiders implementing TSP by extending AbstractProblem \n"
                         + "to separate the data from the objective (and possible constraints).");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("java -cp james-examples.jar "
                         + "org.jamesframework.examples.tsp2.TSP2 "
                         + "<inputfile> <runtime>");
        System.out.println("");
        System.out.println("Example 4C: the travelling salesman problem (3)");
        System.out.println("-----------------------------------------------");
        System.out.println("");
        System.out.println("Reconsiders implementing TSP by using the generic components for \n"
                         + "permutation problems as defined in the extensions module.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("java -cp james-examples.jar "
                         + "org.jamesframework.examples.tsp3.TSP3 "
                         + "<inputfile> <runtime>");
        System.out.println("");
        System.out.println("Example 5A: parameter sweep");
        System.out.println("---------------------------");
        System.out.println("");
        System.out.println("Performs a parameter sweep using the provided tools from the extensions module. \n"
                         + "The core subset selection problem is used as a case study, and the entry-to-nearest-entry \n"
                         + "objective from example 1C is maximized. Different Metropolis searches with a range of \n"
                         + "fixed temperatures are applied to find an appropriate temperature range to be used for a \n"
                         + "parallel tempering search (see example 5B). Results are written to a file 'ParameterSweep.json'.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("java -cp james-examples.jar "
                         + "org.jamesframework.examples.analysis.ParameterSweep "
                         + "<selection-ratio> <runs> <runtime> <mintemp> <maxtemp> "
                         + "<numsearches> [<inputfile>]+");
        System.out.println("");
        System.out.println("Example 5B: comparing algorithm performance");
        System.out.println("-------------------------------------------");
        System.out.println("");
        System.out.println("Compares algorithm performance using the provided tools from the extensions module. \n"
                         + "The core subset selection problem is used as a case study, and the entry-to-nearest-entry \n"
                         + "objective from example 1C is maximized. Both random descent as well as parallel tempering \n"
                         + "are applied to solve the problem for a series of given data sets. Results are written to a \n"
                         + "file 'AlgoComparison.json'.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("java -cp james-examples.jar "
                         + "org.jamesframework.examples.analysis.AlgoComparison "
                         + "<selection-ratio> <runs> <runtime> [<inputfile>]+");
        System.out.println("");
    }
    
}
