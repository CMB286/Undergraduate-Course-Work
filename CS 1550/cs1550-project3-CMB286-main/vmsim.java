import java.io.*;
import java.util.*;

public class vmsim{
    public static void main(String[] args){
        //Load variables with Command-Line Args, Initialize Counters to
        String algType = args[1];
        int numFrames = Integer.parseInt(args[3]);
        int pageSize = Integer.parseInt(args[5]);
        String memSplit = args[7];
        String traceFile = args[8];

        //Use memSplit to find number of frames needed per hash table
        double processZeroRatio = Double.parseDouble(memSplit.substring(0, 1));
        double processOneRatio = Double.parseDouble(memSplit.substring(2));

        double processZeroFrame = (numFrames / (processZeroRatio + processOneRatio)) * processZeroRatio;
        double processOneFrame = numFrames - processZeroFrame;

        int pageOffset = (int) Math.ceil(Math.log(pageSize)/Math.log(2) +10);

        //System.out.println(pageOffset);
        //Create 2 LinkedLists for each Process
        LinkedList<Page> processZeroList = new LinkedList<Page>();
        LinkedList<Page> processOneList = new LinkedList<Page>();

        System.out.println("Algorithm: " + algType.toUpperCase());
        System.out.println("Number of frames: " + numFrames);
        System.out.println("Page size: " + pageSize + " KB");

        if(algType.toLowerCase().equals("opt")){
            OPT(traceFile, pageOffset, processZeroList, processOneList, processZeroFrame, processOneFrame);
        }
        else if(algType.toLowerCase().equals("lru")){
            LRU(traceFile, pageOffset, processZeroList, processOneList, processZeroFrame, processOneFrame);
        }
        else{
            System.out.println("That algorithm was not required!");
        }
    }

    private static void LRU(String traceFile, int pageOffset, LinkedList<Page> processZeroList, LinkedList<Page> processOneList, double processZeroFrame, double processOneFrame){
        int lineNumber = 0;
        int pageFault = 0, writesToDisk = 0;
        Page currentPage, evictedPage;
        boolean foundInList = false;

        try{
            File readFile = new File(traceFile);
            Scanner scanner = new Scanner(readFile);

            //While we have another line in the trace file
            while(scanner.hasNextLine()){
                String currentLine = scanner.nextLine();
                String accessType = Character.toString(currentLine.charAt(0)); //Access type is given at position 0
                String process = Character.toString(currentLine.charAt(currentLine.length() - 1)); //Process ID is given at last position of string                
                String address = currentLine.substring(2, currentLine.length() - 2); //Remove the page offset from the right side of string along with 2 spaces for process ID
                Long translate = Long.decode(address);
                Long pageNumber = translate >>> pageOffset;
                foundInList = false;

                //If the access is process 0, do operations on 0, else do on 1
                if(process.equals("0")){
                    //Search through the list for the page number if size greater than 0. Otherwise, our table is empty
                    if(processZeroList.size() > 0){
                        for(int i = 0; i < processZeroFrame && i < processZeroList.size(); i++){
                             if(processZeroList.get(i).pageNumber == pageNumber){
                                currentPage = processZeroList.get(i);
                                processZeroList.remove(i);

                                //Check the accessType to properly set the dirty bit
                                if(accessType.equals("s")){
                                    currentPage.setDirtyBit(1);
                                }

                                processZeroList.addFirst(currentPage);
                                foundInList = true;
                            }
                        }
                    }

                    //If the pageNumber is not found in list and the list is full, evict the last thing, need to check if the dirty bit is set, and put new page in front. Else, add new page
                    if(!foundInList && processZeroList.size() == processZeroFrame){
                        evictedPage = processZeroList.removeLast();
                        if(evictedPage.getDirtyBit() == 1){
                            writesToDisk++;
                        }
                        pageFault++;
                        currentPage = new Page(0, process, pageNumber);
                        if(accessType.equals("s")){
                            currentPage.setDirtyBit(1);
                        }
                        processZeroList.addFirst(currentPage);
                    }
                    else if (!foundInList){
                        pageFault++;
                        currentPage = new Page(0, process, pageNumber);
                        if(accessType.equals("s")){
                            currentPage.setDirtyBit(1);
                        }
                        processZeroList.addFirst(currentPage);
                    }
                }
                else{
                    //Else dealing with process 1
                    //Search through the list for the page number if size greater than 0. Otherwise, our table is empty
                    if(processOneList.size() > 0){
                        for(int i = 0; i < processOneFrame && i < processOneList.size(); i++){
                             if(processOneList.get(i).pageNumber == pageNumber){
                                currentPage = processOneList.get(i);
                                processOneList.remove(i);

                                //Check the accessType to properly set the dirty bit
                                if(accessType.equals("s")){
                                    currentPage.setDirtyBit(1);
                                }

                                processOneList.addFirst(currentPage);
                                foundInList = true;
                            }
                        }
                    }

                    //If the pageNumber is not found in list and the list is full, evict the last thing, need to check if the dirty bit is set, and put new page in front. Else, add new page
                    if(!foundInList && processOneList.size() == processOneFrame){
                        evictedPage = processOneList.removeLast();
                        if(evictedPage.getDirtyBit() == 1){
                            writesToDisk++;
                        }
                        pageFault++;
                        currentPage = new Page(0, process, pageNumber);
                        if(accessType.equals("s")){
                            currentPage.setDirtyBit(1);
                        }
                        processOneList.addFirst(currentPage);
                    }
                    else if (!foundInList){
                        pageFault++;
                        currentPage = new Page(0, process, pageNumber);
                        if(accessType.equals("s")){
                            currentPage.setDirtyBit(1);
                        }
                        processOneList.addFirst(currentPage);
                    }
                }
            
                lineNumber++;
            }
            scanner.close();
        }
        catch (Exception e){
            System.out.println("File not found!");
        }

        System.out.println("Total memory accesses: " + lineNumber);
        System.out.println("Total page faults: " + pageFault);
        System.out.println("Total writes to disk: " + writesToDisk);
    }

    private static void OPT(String traceFile, int pageOffset, LinkedList<Page> processZeroList, LinkedList<Page> processOneList, double processZeroFrame, double processOneFrame){
        //Hashtables needed to keep track of next accesses
        Hashtable<Long, LinkedList<Integer>> processZero = new Hashtable<Long, LinkedList<Integer>>();
        Hashtable<Long, LinkedList<Integer>> processOne = new Hashtable<Long, LinkedList<Integer>>();
        int lineNumber = 0;
        int pageFault = 0, writesToDisk = 0;
        Page currentPage, evictedPage;
        boolean foundInList = false;
        LinkedList<Integer> temp = new LinkedList<Integer>();
        int highestLineNumber;

        //Read through the file and input it into the HashTable
        try{
            File readFile = new File(traceFile);
            Scanner scanner = new Scanner(readFile);

            //While we have another line in the trace file
            while(scanner.hasNextLine()){
                String currentLine = scanner.nextLine();
                String process = Character.toString(currentLine.charAt(currentLine.length() - 1)); //Process ID is given at last position of string                
                String address = currentLine.substring(2, currentLine.length() - 2); //Remove the page offset from the right side of string along with 2 spaces for process ID
                Long translate = Long.decode(address);
                Long pageNumber = translate >>> pageOffset;

                //HashTable keys are page numbers, values are linked lists of line numbers where the page number is accessed in the file
                if(process.equals("0")){
                    if(processZero.containsKey(pageNumber)){
                        //Get linked list at spot and append line number
                        temp = processZero.get(pageNumber);
                        temp.add(lineNumber);
                    }
                    else{
                        //Create a new LinkedList at the corresponding pageNumber key
                        processZero.put(pageNumber, temp = new LinkedList<Integer>());
                        temp.add(lineNumber);
                    }
                 }
                else {
                    if(processOne.containsKey(pageNumber)){
                        //Get linked list at spot and append line number
                        temp = processOne.get(pageNumber);
                        temp.add(lineNumber);
                    }
                    else{
                        processOne.put(pageNumber, temp = new LinkedList<Integer>());
                        temp.add(lineNumber);
                    }
                 }

                lineNumber++;
            }
            scanner.close();
        }
        catch (Exception e){
            System.out.println(e);
        }


        lineNumber = 0;
        try{
            File readFile = new File(traceFile);
            Scanner scanner = new Scanner(readFile);

            //While we have another line in the trace file
            while(scanner.hasNextLine()){
                String currentLine = scanner.nextLine();
                String accessType = Character.toString(currentLine.charAt(0)); //Access type is given at position 0
                String process = Character.toString(currentLine.charAt(currentLine.length() - 1)); //Process ID is given at last position of string                
                String address = currentLine.substring(2, currentLine.length() - 2); //Remove the page offset from the right side of string along with 2 spaces for process ID
                Long translate = Long.decode(address);
                Long pageNumber = translate >>> pageOffset;
                Long evictPageNumber = (long) 0;
                LinkedList<Long> tieListZero = new LinkedList<Long>();
                LinkedList<Long> tieListOne = new LinkedList<Long>();
                foundInList = false;
                evictedPage = null;

                //If the access is process 0, do operations on 0, else do on 1
                if(process.equals("0")){
                     //Remove lineNumber from the list
                     temp = processZero.get(pageNumber);
                     if(temp.size() > 0){
                         temp.removeFirst();
                     }

                    //Search through the list for the page number if size greater than 0. Otherwise, our table is empty
                    if(processZeroList.size() > 0){
                        for(int i = 0; i < processZeroFrame && i < processZeroList.size(); i++){
                            if(processZeroList.get(i).pageNumber == pageNumber){
                                currentPage = processZeroList.remove(i);


                                //Check the accessType to properly set the dirty bit
                                if(accessType.equals("s")){
                                    currentPage.setDirtyBit(1);
                                }
                
                                processZeroList.addFirst(currentPage);
                                foundInList = true;
                            }
                        }
                    }
                
                    //If the pageNumber is not found in list and the list is full, evict the page that has the farthest away access
                    if(!foundInList && processZeroList.size() == processZeroFrame){
                        //Search through hashtable to find either empty or the highest linenumber at head of array
                        highestLineNumber = lineNumber;
                        for(int i = 0; i < processZeroList.size(); i++){
                            if(processZero.get(processZeroList.get(i).pageNumber).size() == 0){
                                tieListZero.add(processZeroList.get(i).pageNumber);
                            }
                            else{
                                if(processZero.get(processZeroList.get(i).pageNumber).peek() > highestLineNumber){
                                    evictPageNumber = processZeroList.get(i).pageNumber;
                                    highestLineNumber = processZero.get(processZeroList.get(i).pageNumber).peek();
                                }
                            }
                        }

                        if(tieListZero.size() == 0){
                            for(int i = 0; i < processZeroList.size(); i++){
                                if(evictPageNumber == processZeroList.get(i).pageNumber){
                                    evictedPage = processZeroList.remove(i);
                                }
                            }
                        }
                        else if(tieListZero.size() == 1){
                            evictPageNumber = tieListZero.removeFirst();
                            for(int i = 0; i < processZeroList.size(); i++){
                                if(evictPageNumber == processZeroList.get(i).pageNumber){
                                    evictedPage = processZeroList.remove(i);
                                }
                            }
                        }
                        else if(tieListZero.size() > 1){
                            for(int i = 0; i < processZeroList.size(); i++){
                                for(int j = 0; j < tieListZero.size(); j++){
                                    if(processZeroList.get(i).pageNumber == tieListZero.get(j)){
                                        evictedPage = processZeroList.get(i);
                                    }
                                }
                            }
                            processZeroList.remove(evictedPage);
                        }

                        if(evictedPage.getDirtyBit() == 1){
                            writesToDisk++;
                        }
                        pageFault++;

                        currentPage = new Page(0, process, pageNumber);
                        if(accessType.equals("s")){
                            currentPage.setDirtyBit(1);
                        }
                        processZeroList.addFirst(currentPage);
                    }
                    else if (!foundInList){
                        pageFault++;

                        currentPage = new Page(0, process, pageNumber);
                        if(accessType.equals("s")){
                            currentPage.setDirtyBit(1);
                        }
                        processZeroList.addFirst(currentPage);
                    }
                }
                else{
                    //Remove lineNumber from the list
                    temp = processOne.get(pageNumber);
                    if(temp.size() > 0){
                        temp.removeFirst();
                    }

                    if(processOneList.size() > 0){
                        for(int i = 0; i < processOneFrame && i < processOneList.size(); i++){
                            if(processOneList.get(i).pageNumber == pageNumber){
                                currentPage = processOneList.remove(i);
                                
                                //Check the accessType to properly set the dirty bit
                                if(accessType.equals("s")){
                                    currentPage.setDirtyBit(1);
                                }
                
                                processOneList.addFirst(currentPage);
                                foundInList = true;
                            }
                        }
                    }
                
                    //If the pageNumber is not found in list and the list is full, evict the page that has the farthest away access
                    if(!foundInList && processOneList.size() == processOneFrame){
                        //Search through hashtable to find either empty or the highest linenumber at head of array
                        highestLineNumber = lineNumber;
                        for(int i = 0; i < processOneList.size(); i++){
                            if(processOne.get(processOneList.get(i).pageNumber).size() == 0){
                                tieListOne.add(processOneList.get(i).pageNumber);
                            }
                            else{
                                if(processOne.get(processOneList.get(i).pageNumber).peek() > highestLineNumber){
                                    evictPageNumber = processOneList.get(i).pageNumber;
                                    highestLineNumber = processOne.get(processOneList.get(i).pageNumber).peek();
                                }
                            }
                        }

                        if(tieListOne.size() == 0){
                            for(int i = 0; i < processOneList.size(); i++){
                                if(evictPageNumber == processOneList.get(i).pageNumber){
                                    evictedPage = processOneList.remove(i);
                                }
                            }
                        }
                        else if(tieListOne.size() == 1){
                            evictPageNumber = tieListOne.removeFirst();
                            for(int i = 0; i < processOneList.size(); i++){
                                if(evictPageNumber == processOneList.get(i).pageNumber){
                                    evictedPage = processOneList.remove(i);
                                }
                            }
                        }
                        else if(tieListOne.size() > 1){
                            for(int i = 0; i < processOneList.size(); i++){
                                for(int j = 0; j < tieListOne.size(); j++){
                                    if(processOneList.get(i).pageNumber == tieListOne.get(j)){
                                        evictedPage = processOneList.get(i);
                                    }
                                }
                            }
                            processOneList.remove(evictedPage);
                        }

                        if(evictedPage.getDirtyBit() == 1){
                            writesToDisk++;
                        }
                        pageFault++;

                        currentPage = new Page(0, process, pageNumber);
                        if(accessType.equals("s")){
                            currentPage.setDirtyBit(1);
                        }
                        processOneList.addFirst(currentPage);
                    }
                    else if (!foundInList){
                        pageFault++;

                        currentPage = new Page(0, process, pageNumber);
                        if(accessType.equals("s")){
                            currentPage.setDirtyBit(1);
                        }
                        processOneList.addFirst(currentPage);
                    }
                }

                lineNumber++;
            }
            scanner.close();
        }
        catch (Exception e){
            System.out.println(e);
        }

        System.out.println("Total memory accesses: " + lineNumber);
        System.out.println("Total page faults: " + pageFault);
        System.out.println("Total writes to disk: " + writesToDisk);
    }



}