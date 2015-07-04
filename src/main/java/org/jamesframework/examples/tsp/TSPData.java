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

/**
 * TSP data: stores a travel distance matrix.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class TSPData {

    // travel distance matrix
    private final double[][] dist;

    public TSPData(double[][] dist) {
        this.dist = dist;
    }
    
    // get travel distance from the given city to the given other city
    public double getDistance(int from, int to){
        return dist[from][to];
    }
    
    // retrieve number of cities
    public int getNumCities(){
        return dist.length;
    }
    
}
