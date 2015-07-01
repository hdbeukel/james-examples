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

package org.jamesframework.examples.tsp2;

import java.util.List;
import org.jamesframework.core.exceptions.IncompatibleDeltaEvaluationException;
import org.jamesframework.core.problems.objectives.Objective;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.problems.objectives.evaluations.SimpleEvaluation;
import org.jamesframework.core.search.neigh.Move;
import org.jamesframework.examples.tsp.TSP2OptMove;
import org.jamesframework.examples.tsp.TSPSolution;

/**
 * Objective for the TSP problem: minimize total travel distance of the round trip.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class TSPObjective implements Objective<TSPSolution, TSPData>{

    @Override
    public Evaluation evaluate(TSPSolution solution, TSPData data) {
        // compute sum of travel distances
        List<Integer> cities = solution.getCities();
        int n = cities.size();
        double totalDistance = 0.0;
        for(int i=0; i<n; i++){
            int fromCity = cities.get(i);
            int toCity = cities.get((i+1)%n);
            totalDistance += data.getDistance(fromCity, toCity);
        }
        // wrap in simple evaluation
        return SimpleEvaluation.WITH_VALUE(totalDistance);
    }

    @Override
    public Evaluation evaluate(Move move, TSPSolution curSolution, Evaluation curEvaluation, TSPData data){
        
        // check move type
        if(!(move instanceof TSP2OptMove)){
            throw new IncompatibleDeltaEvaluationException("Delta evaluation in TSP objective expects move of type TSP2OptMove.");
        }
        // cast move
        TSP2OptMove move2opt = (TSP2OptMove) move;
        // get bounds of reversed subsequence
        int i = move2opt.getI();
        int j = move2opt.getJ();
        // get number of cities
        int n = data.getNumCities();
        
        if((j+1)%n == i){
            // special case: entire round trip reversed
            return curEvaluation;
        } else {
            // get current total travel distance
            double totalDistance = curEvaluation.getValue();
            // get current order of cities
            List<Integer> cities = curSolution.getCities();

            // get crucial cities (at boundary of reversed subsequence)
            int beforeReversed = cities.get((i-1+n)%n);
            int firstReversed = cities.get(i);
            int lastReversed = cities.get(j);
            int afterReversed = cities.get((j+1)%n);

            // account for dropped distances
            totalDistance -= data.getDistance(beforeReversed, firstReversed);
            totalDistance -= data.getDistance(lastReversed, afterReversed);

            // account for new distances
            totalDistance += data.getDistance(beforeReversed, lastReversed);
            totalDistance += data.getDistance(firstReversed, afterReversed);

            // return updated travel distance
            return SimpleEvaluation.WITH_VALUE(totalDistance);
        }
        
    }
    
    @Override
    public boolean isMinimizing() {
        return true;
    }

}
