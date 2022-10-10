

public class DLBNode{

    public Nodelet front;       //First node of branch
    boolean exists;             //if this represents a string true, else false
    private int count;          //Keep track of how many times certain word is accessed
    String word;                //Store word

    //DLB-Trie
    public static class Nodelet {
        char c;                     //char value stored in nodelet
        Nodelet nextSibling;        //next possible char
        DLBNode nextChild;          //node of next char

        public Nodelet(char a, Nodelet nS, DLBNode nCh) {
            c = a;
            nextSibling = nS;
            nextChild = nCh;
        }

        public boolean hasChild(){  //Returns false if we are at the end of the trie
            return nextChild != null;
        }
    }

    public DLBNode() {
        front = null;
        count = 0;
        exists = false;
        word = "";
    }

    public void addChild(char nextC) { //Adds a new DLB child
        if (front == null) { //Start list if front is not created yet
            front = new Nodelet(nextC, null, new DLBNode());
        }
        else if (nextC < front.c) { //Add node to the front of trie, keeps it in alphabetical order
            front = new Nodelet(nextC, front, new DLBNode());
        }
        else { //Add node to the next open spot in structure
            Nodelet curr = front.nextSibling;
            Nodelet prev = front;
            while (curr != null && curr.c < nextC) {
                prev = curr;
                curr = curr.nextSibling;
            }
            prev.nextSibling = new Nodelet(nextC, curr, new DLBNode());
        }
    }

    public void addChildCount(char nextC, int count) { //Adds a new DLB child
        if (front == null) { //Start list if front is not created yet
            front = new Nodelet(nextC, null, new DLBNode());
        }
        else if (nextC < front.c) { //Add node to the front of trie, keeps it in alphabetical order
            front = new Nodelet(nextC, front, new DLBNode());
        }
        else { //Add node to the next open spot in structure
            Nodelet curr = front.nextSibling;
            Nodelet prev = front;
            while (curr != null && curr.c < nextC) {
                prev = curr;
                curr = curr.nextSibling;
            }
            prev.nextSibling = new Nodelet(nextC, curr, new DLBNode());
        }
    }
    public DLBNode getChild(char nextC){ //Returns DLBNode at a specific character
        if(front == null){
            return null;
        }
        Nodelet curr = front;
        while(curr != null && curr.c < nextC){
            curr = curr.nextSibling;
        }
        if(curr == null || curr.c != nextC){
            return null;
        }
        return curr.nextChild;
    }

    public boolean hasChild(char nextC){ //Checks to see if there exists a child with a given character, essentially same as getChild method but returns boolean
        if(front == null){
            return false;
        }
        Nodelet curr = front;
        while(curr != null && curr.c < nextC){
            curr = curr.nextSibling;
        }
        return curr != null && curr.c == nextC;
    }

    public void setCount(int count){ //Set the amount of times DLBNode has been accessed
        this.count = count;
    }

    public int getCount(){ //Return amount of times DLBNode word has been accessed
        return count;
    }

    //Returns the total number of predictions we still need to make, used to determine if need to check dictionaryTrie
    public int getPredictions(Nodelet curr, DLBNode[] predictions, int totalPredictions) { //Writes predictions to an array
        if(curr == null) {
            return 0;
        }

        if(curr.c == '!' && curr.nextSibling != null){              //If the char stored at the nodelet marks the end of a word, add it to the array
            DLBNode temp = curr.nextChild;
            predictions[predictions.length - totalPredictions] = temp;
            totalPredictions--;
            curr = curr.nextSibling;
        }

        if (curr.hasChild() && totalPredictions > 0) {  //If there is a value below the current node

            if (curr.nextChild.front != null) {     //If the DLBNode has a child that is not null, proceed down to next level of nodelets
                totalPredictions = getPredictions(curr.nextChild.front, predictions, totalPredictions);  //Go down vertically and left in trie

                if (totalPredictions == 0) {    //Break out of recursion when found 5 predictions
                    return totalPredictions;
                }


            if (curr.nextSibling != null) {          //If the Nodelet has a sibling that is not null, move to the right to find try to find next word
                totalPredictions = getPredictions(curr.nextSibling, predictions, totalPredictions);

                if (totalPredictions == 0) {    //Break out of recursion when found 5 predictions
                    return totalPredictions;
                }
            }

            return totalPredictions;                //Return the amount of predictions we still need to make
            }
        }


        if (curr.nextChild.exists && totalPredictions > 0) {    //Add words after moving Nodelet position
            DLBNode temp = curr.nextChild;
            predictions[predictions.length - totalPredictions] = temp;
            totalPredictions--;
        }

        return totalPredictions;              //Return the total amount of predictions we still need to make
    }

}
