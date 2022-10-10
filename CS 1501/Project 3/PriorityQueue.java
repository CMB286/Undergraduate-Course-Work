import java.util.HashMap;

public class PriorityQueue {

    private int maxN;               // maximum number of elements in PQ
    private int n;                  // number of elements in PQ
    public Apartments[] costPQ;     //Arrays for PQ
    public Apartments[] squareFootPQ;
    HashMap<String, Integer> costIndirection;   //Indirection Symbol Tables
    HashMap<String, Integer> squareFootIndirection;

    public PriorityQueue(int maxN) {
        if (maxN < 0) throw new IllegalArgumentException();
        this.maxN = maxN;
        n = 0;
        costPQ = new Apartments[maxN + 1];
        squareFootPQ = new Apartments[maxN + 1];
        costIndirection = new HashMap<String, Integer>(maxN + 1);
        squareFootIndirection = new HashMap<String, Integer>(maxN + 1);
    }

    public void insert(Apartments key) {    //Inserts apartment into PQ and Indirection table
        n++;
        costPQ[n] = key;
        costIndirection.put(key.getHashString(), n);

        squareFootPQ[n] = key;
        squareFootIndirection.put(key.getHashString(), n);

        swimCost(n);
        swimSquareFoot(n);
    }

    public Apartments minApartmentCost() {  //Returns top of min PQ
        return costPQ[1];
    }

    public Apartments maxApartmentSquareFootage() { //Returns top of max PQ
        return squareFootPQ[1];
    }

    public Apartments minApartmentCostCity(String city) {   //Public function to get min apartment by city name
        Apartments lowestLeft, lowestRight;
        if (costPQ[1] == null) {
            return null;
        }
        if (costPQ[1].getCity().toLowerCase().equals(city.toLowerCase())) {
            return costPQ[1];
        } else {
            int index = 1;
            lowestLeft = minApartmentCostCity(city, index * 2);
            lowestRight = minApartmentCostCity(city, index * 2 + 1);

            if (lowestLeft == null && lowestRight == null) {
                return null;
            } else if (lowestLeft == null) {
                return lowestRight;
            } else if (lowestRight == null) {
                return lowestLeft;
            } else if (lowestLeft.getMonthlyCost() < lowestRight.getMonthlyCost()) {
                return lowestLeft;
            } else {
                return lowestRight;
            }
        }
    }

    private Apartments minApartmentCostCity(String city, int index) {   //Recursive function to get min apartment by city name
        Apartments lowestLeft, lowestRight;
        if (costPQ[index] == null || index > n) {
            return null;
        }
        if (costPQ[index].getCity().toLowerCase().equals(city.toLowerCase())) {
            return costPQ[index];
        } else {
            lowestLeft = minApartmentCostCity(city, index * 2);
            lowestRight = minApartmentCostCity(city, index * 2 + 1);

            if (lowestLeft == null && lowestRight == null) {
                return null;
            } else if (lowestLeft == null) {
                return lowestRight;
            } else if (lowestRight == null) {
                return lowestLeft;
            } else if (lowestLeft.getMonthlyCost() < lowestRight.getMonthlyCost()) {
                return lowestLeft;
            } else {
                return lowestRight;
            }
        }
    }

    public Apartments maxApartmentSquareFootageCity(String city) {  //Public function to get max apartment by city name
        Apartments highestLeft, highestRight;
        if (squareFootPQ[1] == null) {
            return null;
        }
        if (squareFootPQ[1].getCity().toLowerCase().equals(city.toLowerCase())) {
            return squareFootPQ[1];
        } else {
            int index = 1;
            highestLeft = maxApartmentSquareFootageCity(city, index * 2);
            highestRight = maxApartmentSquareFootageCity(city, index * 2 + 1);

            if (highestLeft == null && highestRight == null) {
                return null;
            } else if (highestLeft == null) {
                return highestRight;
            } else if (highestRight == null) {
                return highestLeft;
            } else if (highestLeft.getSquareFootage() > highestRight.getSquareFootage()) {
                return highestLeft;
            } else {
                return highestRight;
            }
        }
    }

    private Apartments maxApartmentSquareFootageCity(String city, int index) {  //Recursive function to get max apartment by city name
        Apartments highestLeft, highestRight;
        if (squareFootPQ[index] == null || index > n) {
            return null;
        }
        if (squareFootPQ[index].getCity().toLowerCase().equals(city.toLowerCase())) {
            return squareFootPQ[index];
        } else {
            highestLeft = maxApartmentSquareFootageCity(city, index * 2);
            highestRight = maxApartmentSquareFootageCity(city, index * 2 + 1);

            if (highestLeft == null && highestRight == null) {
                return null;
            } else if (highestLeft == null) {
                return highestRight;
            } else if (highestRight == null) {
                return highestLeft;
            } else if (highestLeft.getSquareFootage() > highestRight.getSquareFootage()) {
                return highestLeft;
            } else {
                return highestRight;
            }
        }
    }

    public void delete(String apartmentHash) {  //Deletes apartment based on  street, apartment number, and zip code
        int index = costIndirection.get(apartmentHash); //Removes from cost PQ
        exchRent(index, n);
        costPQ[n] = null;
        costIndirection.remove(apartmentHash);
        swimCost(index);
        sinkCost(index);

        index = squareFootIndirection.get(apartmentHash);   //Removes from square foot PQ
        exchSqFt(index, n);
        squareFootPQ[n] = null;
        squareFootIndirection.remove(apartmentHash);
        swimSquareFoot(index);
        sinkSquareFoot(index);
        n--;
    }


    public void updateRent(String apartmentHash, int newMonthlyCost) { //Updates rent and readjusts the PQ
        int index = costIndirection.get(apartmentHash);
        costPQ[index].setMonthlyCost(newMonthlyCost);
        swimCost(index);
        sinkCost(index);
    }


    /***************************************************************************
     * General helper functions.
     ***************************************************************************/
    private boolean lowerRent(int i, int j) {   //Checks price of 2 positions, returns null if position is empty, returns true if I > J, gives us min function
        if (costPQ[i] == null || costPQ[j] == null) {
            return false;
        }

        return costPQ[i].getMonthlyCost() - costPQ[j].getMonthlyCost() > 0;
    }

    private boolean greaterSqFt(int i, int j) { //Checks square footage of 2 positions, returns null if position is empty, returns true if I < J, gives us max function
        if (squareFootPQ[i] == null || squareFootPQ[j] == null) {
            return false;
        }

        return squareFootPQ[i].getSquareFootage() - squareFootPQ[j].getSquareFootage() < 0;
    }

    private void exchRent(int i, int j) {   //Swaps 2 positions in the rent PQ
        Apartments swap = costPQ[i];
        costPQ[i] = costPQ[j];
        costPQ[j] = swap;
        costIndirection.replace(costPQ[i].getHashString(), i);
        costIndirection.replace(costPQ[j].getHashString(), j);
    }

    private void exchSqFt(int i, int j) {   //Swaps 2 positions in Square Foot PQ
        Apartments swap = squareFootPQ[i];
        squareFootPQ[i] = squareFootPQ[j];
        squareFootPQ[j] = swap;
        squareFootIndirection.replace(squareFootPQ[i].getHashString(), i);
        squareFootIndirection.replace(squareFootPQ[j].getHashString(), j);
    }


    /***************************************************************************
     * Heap helper functions.
     ***************************************************************************/
    private void swimCost(int k) {                      //Swim and Sink Cost move values within the cost PQ
        while (k > 1 && lowerRent(k / 2, k)) {
            exchRent(k, k / 2);
            k = k / 2;
        }
    }

    private void sinkCost(int k) {
        while (2 * k <= n) {
            int j = 2 * k;
            if (j < n && lowerRent(j, j + 1)) j++;
            if (!lowerRent(k, j)) break;
            exchRent(k, j);
            k = j;
        }
    }

    private void swimSquareFoot(int k) {                //Swim and Sink SquareFoot move values within square foot PQ
        while (k > 1 && greaterSqFt(k / 2, k)) {
            exchSqFt(k, k / 2);
            k = k / 2;
        }
    }

    private void sinkSquareFoot(int k) {
        while (2 * k <= n) {
            int j = 2 * k;
            if (j < n && greaterSqFt(j, j + 1)) j++;
            if (!greaterSqFt(k, j)) break;
            exchSqFt(k, j);
            k = j;
        }
    }
}
