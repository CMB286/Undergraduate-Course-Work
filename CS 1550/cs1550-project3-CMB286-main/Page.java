public class Page {
    
    int dirtyBit; 
    String processID;
    long pageNumber;

    public Page(int dirtyBit, String processID, long pageNumber){
        this.dirtyBit = dirtyBit;
        this.processID = processID;
        this.pageNumber = pageNumber;
    }


    //Set values for object
    public void setDirtyBit(int dirtyBit){
        this.dirtyBit = dirtyBit;
    }
    public void setPageNumber(long pageNumber){
        this.pageNumber = pageNumber;
    }
    public void setProcessID(String processID){
        this.processID = processID;
    }

    //Get values from object
    public int getDirtyBit(){
        return dirtyBit;
    }
    public long getPageNumber(){
        return pageNumber;
    }
    public String getProcessID(){
        return processID;
    }
}
