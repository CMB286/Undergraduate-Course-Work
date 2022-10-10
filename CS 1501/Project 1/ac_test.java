import java.util.*;
import java.io.*;

public class ac_test {

    private static DLBNode[] userPredictions;

    public static void main(String[] args) throws IOException {
        new ac_test();
    }

    public ac_test()  throws IOException{
        int foundWordPlace = -1;
        StringBuilder wordSearching = new StringBuilder();
        String foundWord = " ";
        boolean firstRun = true, newWordRun = false;

        //Initialize DLB trie and dictionary
        Scanner fileReader = new Scanner(new File("dictionary.txt"));
        String dictionaryContent;
        TrieST DLBTrie = new TrieST();  //Trie to store the words from the dictionary file

        //Create userHistory file and data structure to hold user input
        File userHistory = new File("user_history.txt");
        if(!userHistory.exists()){                          //Checks to see if folder exists and creates it if it does not exist
            userHistory.createNewFile();
        }
        Scanner userHistoryReader = new Scanner (new File("user_history.txt"));
        FileWriter userHistoryFW = new FileWriter("user_history.txt", true);
        PrintWriter userHistoryWriter = new PrintWriter(userHistoryFW);
        TrieST userHistoryTrie = new TrieST();  //Trie to store words from userHistory file
        int count;

        //Time Tracking Declarations
        long timeStart, timeFinish;
        double totalTimePerRun = 0.0;
        double totalTimeOverAll = 0.0;
        int amountOfRuns = 0;

        //Read dictionary into DLB trie
        while(fileReader.hasNext()){
            dictionaryContent = fileReader.next();
            DLBTrie.addWord(dictionaryContent + "!"); //Chose ! as the termination char as its ASCII value is lower than both upper and lowercase letters
        }
        fileReader.close();


        //Read userHistory from file and store into userHistoryTrie
        while(userHistoryReader.hasNext()){
            dictionaryContent = userHistoryReader.next();
            count = userHistoryReader.nextInt();
            userHistoryTrie.addWord(dictionaryContent + "!", count);
        }
        userHistoryReader.close();

        //Scanner for user input
        Scanner inScan = new Scanner(System.in);
        String userEntry = "";


        //Loop that only exits once the user enters !
        while(!userEntry.equals("!")){
            if(firstRun){                       //Nested if block to ask the user for very first character, next character, or first character of another word
                firstRun = false;
                System.out.print("Enter your first character:  ");
                userEntry = inScan.next();
                wordSearching.append(userEntry);
            }
            else if (newWordRun) {
                System.out.print("\nEnter first character of the next word:  ");
                wordSearching = new StringBuilder();
                userEntry = inScan.next();
                wordSearching.append(userEntry);
                newWordRun = false;
            }
            else{
                System.out.print("\n\nEnter the next character:  ");
                userEntry = inScan.next();
                wordSearching.append(userEntry);
            }

            //Structure to see if user chose a word based on userEntry input
            if(userEntry.equals("1")){
                foundWord = userPredictions[0].word.substring(0, userPredictions[0].word.length() - 1);
                foundWordPlace = 0;
            }
            else if(userEntry.equals("2")){
                foundWord = userPredictions[1].word.substring(0, userPredictions[1].word.length() - 1);
                foundWordPlace = 1;
            }
            else if(userEntry.equals("3")){
                foundWord = userPredictions[2].word.substring(0, userPredictions[2].word.length() - 1);
                foundWordPlace = 2;
            }
            else if(userEntry.equals("4")){
                foundWord = userPredictions[3].word.substring(0, userPredictions[3].word.length() - 1);
                foundWordPlace = 3;
            }
            else if(userEntry.equals("5")){
                foundWord = userPredictions[4].word.substring(0, userPredictions[4].word.length() - 1);
                foundWordPlace = 4;
            }
            else if(userEntry.equals("$")){
                foundWordPlace = 0;
                foundWord = wordSearching.substring(0, wordSearching.length() - 1);
                userPredictions = new DLBNode[0];
                userHistoryTrie.addWord(foundWord + "!", 1);
                userHistoryWriter.print(foundWord + "\n" + 1 + "\n");
                foundWord = "New word added";
            }
            else if(userEntry.equals("!")){
                foundWord = "FINISH";
            }

            //Nested If Statement to print if the user's input determines prediction search, new word add, or completed word
            if(foundWord.equals(" ")){
                timeStart = System.nanoTime();  //Use java System.nanoTime() to calculate how long program takes and display this time
                userPredictions = predictions(wordSearching, userHistoryTrie, DLBTrie);
                timeFinish = System.nanoTime();
                totalTimePerRun = calculateTime(timeStart, timeFinish);
                totalTimeOverAll += totalTimePerRun;
                amountOfRuns++;


                //Prints predictions out to the user
                System.out.printf("\n(%f s)\n", totalTimePerRun);
                System.out.println("Predictions:");
                for(int i = 0; i < userPredictions.length; i++){
                    if(userPredictions[i] != null) {
                        System.out.print("(" + (i + 1) + ") " + userPredictions[i].word.substring(0, userPredictions[i].word.length() - 1) + "\t ");
                    }
                }

                if (userPredictions.length == 0) {
                    System.out.println("\n\t\tNO PREDICTIONS WERE FOUND");
                }
            }
            else if(foundWord.equals("New word added")){
                System.out.println("\n\nNew Word Added to User History!");
                foundWord = " ";
                newWordRun = true;
            }
            else if(!foundWord.equals("FINISH")){ //Handles when a user chose a word
                System.out.println("\n\n\t\tWORD COMPLETED:  " + foundWord);

                //Add word to userHistory and increment the count
                userHistoryTrie.addWord(foundWord + "!", 1);
                userHistoryWriter.print(foundWord + "\n" + (userPredictions[foundWordPlace].getCount() + 1) + "\n");
                userPredictions = new DLBNode[0];
                foundWord = " ";
                newWordRun = true;
            }
        }

        userHistoryWriter.close();

        //Prints out final message to the user with average time and friendly goodbye
        System.out.printf("\n\nAverage time:  %f  s\n", calculateTotalAverage(totalTimeOverAll, amountOfRuns));
        System.out.println("Bye!");
    }



    //Method to find the total time that the prediction took
    public double calculateTime(long timeStart, long timeFinish){
        long totalTime = timeFinish - timeStart;
        double totalTimeRun;
        totalTimeRun = (double)totalTime / 1000000000;
        return totalTimeRun;
    }

    //Method to find the average time across all prediction runs
    public double calculateTotalAverage(Double totalTimeSeconds, int amountOfRuns){
        return totalTimeSeconds / amountOfRuns;
    }


    //Method to get predictions which returns a list of references to get the stored words
    public DLBNode[] predictions(StringBuilder wordSearching, TrieST userHistory, TrieST dictionary){
        int predictionTotal, predictionsWant;
        userPredictions = new DLBNode[5];

        //Sub-Tries starting at the last letter that the user entered
        TrieST subTrieUserHistory = new TrieST(userHistory.getSubTrie(wordSearching.toString()));
        TrieST subTrieDictionary = new TrieST(dictionary.getSubTrie(wordSearching.toString()));

        try{    //Catches if a null is thrown from method in DLBNode class
            DLBNode temp = subTrieUserHistory.root;
            predictionTotal = temp.getPredictions(temp.front, userPredictions, 5);
            predictionsWant = 5 - (5 - predictionTotal);  //Calculate how many more predictions we need to find in dictionary
        } catch (NullPointerException e){
            predictionsWant = 5;
        }

        try {   //Catches if a null is thrown from method in DLBNode class, if null is thrown here we know that there are no words with the prefix entered so we can set array to be 0 length
            if (predictionsWant != 0) {
                DLBNode temp = subTrieDictionary.root;
                temp.getPredictions(temp.front, userPredictions, predictionsWant);
            }
        } catch (NullPointerException f){
            userPredictions = new DLBNode[0];
        }

        return userPredictions;
    }


}
