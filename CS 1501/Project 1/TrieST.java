


public class TrieST {

    public DLBNode root; //Front node of trie

    public TrieST(){    //Initializes an empty string symbol table
        root = null;
    }

    public TrieST(DLBNode subTrie){
        root = subTrie;
    }

    public void addWord(String word){ //Add word
        int length = word.length();
        if (root == null){  //Create a new DLB Trie
            root = new DLBNode();
        }
        DLBNode cur = root;
        DLBNode temp;
        for(int i = 0; i < length; i++){
            temp = cur.getChild(word.charAt(i));
            if(temp == null){
                cur.addChild(word.charAt(i));
                temp = cur.getChild(word.charAt(i));
            }
            cur = temp;
        }
        cur.word = word;   //Store word at the last node
        cur.setCount(0);   //Set the amount of times the word has been accessed
        cur.exists = true; //Mark end of a word
    }

    public void addWord(String word, int j){ //Add word for userHistory
        int length = word.length();
        if (root == null){  //Creates pointer to first letter
            root = new DLBNode();
        }
        DLBNode cur = root;
        DLBNode temp;
        for(int i = 0; i < length; i++){
            temp = cur.getChild(word.charAt(i));
            if(temp == null){
                cur.addChildCount(word.charAt(i), j);
                temp = cur.getChild(word.charAt(i));
            }
            cur = temp;
        }
        cur.word = word;
        if(cur.getCount() == 0){
            cur.setCount(j);
        } else{
            cur.setCount(cur.getCount() + j);
        }
        cur.exists = true; //Mark end of a word
    }


    public DLBNode getSubTrie(String word){ //Returns the DLBNode at the top of the sub-trie
        if(root == null){
            return null;
        }
        int length = word.length();
        DLBNode curr = root;
        for(int i = 0; i < length; i++) {
            if(curr.hasChild(word.charAt(i))){
                curr = curr.getChild(word.charAt(i));
            }
            else{
                return null;
            }
        }
        return curr;
    }
}
