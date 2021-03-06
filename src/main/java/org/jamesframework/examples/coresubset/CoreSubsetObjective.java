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

package org.jamesframework.examples.coresubset;

import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.problems.objectives.Objective;
import org.jamesframework.core.problems.objectives.evaluations.SimpleEvaluation;
import org.jamesframework.core.subset.SubsetSolution;

/**
 * Implements the core subset selection objective: maximizing the average distance between all pairs of selected items.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class CoreSubsetObjective implements Objective<SubsetSolution, CoreSubsetData>{

    /**
     * Evaluates the given subset solution using the underlying data, by computing the average
     * distance between all pairs of selected items. If less than two items are selected,
     * the evaluation is defined to have a value of 0.0.
     * 
     * @param solution subset solution
     * @param data core subset data
     * @return evaluation with a value set to the average distance between all pairs of selected items;
     *         the value is defined to be 0.0 if less than 2 items are selected
     */
    @Override
    public Evaluation evaluate(SubsetSolution solution, CoreSubsetData data) {
        double value = 0.0;
        if(solution.getNumSelectedIDs() >= 2){
            // at least two items selected: compute average distance
            int numDist = 0;
            double sumDist = 0.0;
            Integer[] selected = new Integer[solution.getNumSelectedIDs()];
            solution.getSelectedIDs().toArray(selected);
            for(int i=0; i<selected.length; i++){
                for(int j=i+1; j<selected.length; j++){
                    sumDist += data.getDistance(selected[i], selected[j]);
                    numDist++;
                }
            }
            value = sumDist/numDist;
        }
        return SimpleEvaluation.WITH_VALUE(value);
    }
    
    /**
     * Always returns <code>false</code> as this objective has to be maximized.
     * 
     * @return <code>false</code>
     */
    @Override
    public boolean isMinimizing() {
        return false;
    }

}
