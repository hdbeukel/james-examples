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

package org.jamesframework.examples.clique;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.jamesframework.core.subset.SubsetSolution;
import org.jamesframework.core.search.neigh.Move;
import org.jamesframework.core.search.neigh.Neighbourhood;
import org.jamesframework.core.subset.neigh.moves.AdditionMove;

/**
 * Greedy neighbourhood for the maximum clique problem that generates moves which add a single new vertex that is
 * connected to all vertices contained in the current clique. Among all vertices that can be added, only those with
 * maximum degree within the induced subgraph are considered.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GreedyCliqueNeighbourhood implements Neighbourhood<SubsetSolution> {

    // clique data (graph)
    private final CliqueData data;

    public GreedyCliqueNeighbourhood(CliqueData data) {
        this.data = data;
    }
    
    @Override
    public Move<SubsetSolution> getRandomMove(SubsetSolution solution, Random rnd) {
        List<Move<SubsetSolution>> allMoves = getAllMoves(solution);
        if(allMoves.isEmpty()){
            return null;
        } else {
            return allMoves.get(rnd.nextInt(allMoves.size()));
        }
    }

    @Override
    public List<Move<SubsetSolution>> getAllMoves(SubsetSolution solution) {
        // get current clique
        Set<Integer> clique = solution.getSelectedIDs();
        // construct set of possible additions
        Set<Integer> possibleAdds = solution.getUnselectedIDs().stream()
                                                               .filter(v -> isPossibleAdd(v, clique))
                                                               .collect(Collectors.toSet());
        // retain only additions of candidate vertices
        // with maximum degree within induced subgraph
        List<Move<SubsetSolution>> moves = new ArrayList<>();
        long degree, maxDegree = -1;
        for(int v : possibleAdds){
            // get degree within subgraph
            degree = data.degree(v, possibleAdds);
            if(degree > maxDegree){
                // higher degree
                maxDegree = degree;
                moves.clear();
                moves.add(new AdditionMove(v));
            } else if (degree == maxDegree){
                // same degree
                moves.add(new AdditionMove(v));
            }
        }
        return moves;
    }
    
    // check if given vertex is connected to all vertices in the current clique
    private boolean isPossibleAdd(int vertex, Set<Integer> clique){
        return clique.stream().allMatch(vc -> data.connected(vertex, vc));
    }

}
