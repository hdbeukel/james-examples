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

package org.jamesframework.examples.tsp2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.jamesframework.core.problems.GenericProblem;
import org.jamesframework.examples.tsp.TSPSolution;

/**
 * Specification of the travelling salesman problem. Each city is represented by a unique integer value.
 * The corresponding solution type is the custom {@link TSPSolution}. The data type is set to the custom
 * {@link TSPData} which wraps a distance matrix containing the travel distance between each pair of cities. 
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class TSPProblem extends GenericProblem<TSPSolution, TSPData>{

    public TSPProblem(TSPData data) {
        // set TSP objective and pass data (distance matrix) to super class
        super(new TSPObjective(), data);
    }

    @Override
    public TSPSolution createRandomSolution(Random rnd) {
        // create random permutation of cities
        List<Integer> cities = new ArrayList<>();
        int n = getData().getNumCities();
        for(int i=0; i<n; i++){
            cities.add(i);
        }
        Collections.shuffle(cities, rnd);
        // create and return TSP solution
        return new TSPSolution(cities);
    }

}
