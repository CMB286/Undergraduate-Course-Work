/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

public class MyLZW {
    private static final int R = 256;        // number of input chars
    private static int L = 512;              // number of codewords = 2^W
    private static int W = 9;                // codeword width
    private static boolean resetMode = false;
    private static boolean monitorMode = false;
    private static boolean initialCompression = true;
    private static double initialCompressionRatio = 0, uncompressedSize = 0, compressedSize = 0, newCompressionRatio = 0, ratioOfRatios;

    public static void compress(String type) {
        if (type.equals("r")) {   //Check if we are in reset mode
            resetMode = true;
            BinaryStdOut.write('r', W);    //Write first letter to be r
        }

        if (type.equals("m")) {   //Check if we are in monitor mode
            monitorMode = true;
            BinaryStdOut.write('m', W);    //Write first letter to be m
            compressedSize += W;
        }

        String input = BinaryStdIn.readString();    //Puts the entire contents of the file into input
        TST<Integer> st = new TST<Integer>();

        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R + 1;  // R is codeword for EOF


        while (input.length() > 0) {    //Drive for compression
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();

            uncompressedSize += t * 8;  //Each uncompressed word is 8 bits
            compressedSize += W;        //Each compressed word will be W bits

            if (code == L && W != 16) {
                L *= 2;
                W++;
            }

            if (code == 65536 && monitorMode) {  //Begin to monitor the compression ratio
                //Find the initial compression ratio
                if (initialCompression) {
                    initialCompressionRatio = uncompressedSize / compressedSize;
                    initialCompression = false;

                } else {
                    newCompressionRatio = uncompressedSize / compressedSize;
                    ratioOfRatios = initialCompressionRatio / newCompressionRatio;

                    if (ratioOfRatios > 1.1) {       //Reset the codebook if ratio exceeds threshold
                        st = new TST<Integer>();
                        for (int i = 0; i < R; i++)
                            st.put("" + (char) i, i);
                        code = R + 1;
                        L = 512;
                        W = 9;
                        initialCompression = true;   //Need to reset count of compressed and uncompressed data
                        uncompressedSize = 0;
                        compressedSize = 0;
                    }
                }
            }


            if (code == 65536 && resetMode) { //If all possible values are used, reset dictionary back to initial state
                st = new TST<Integer>();
                for (int i = 0; i < R; i++)
                    st.put("" + (char) i, i);
                code = R + 1;
                L = 512;
                W = 9;
            }

            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            input = input.substring(t);            // Scan past s in input.
        }

        BinaryStdOut.write(R, W);   //Marks ENDOFFILE
        BinaryStdOut.close();
    }


    public static void expand() {

        String[] st = new String[65536];        //Set size to 65536 because that equals 2^16
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";               // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string (At EOF)
        String val = st[codeword];           // Find first letter
        compressedSize += W;

        if (val.equals("r")) {  //Handles if we see that we are in reset mode
            resetMode = true;
            codeword = BinaryStdIn.readInt(W);  //Need to read in next bit of data
            if (codeword == R) return;
            val = st[codeword];
        }

        if (val.equals("m")) {  //Handles if we see we are in monitor mode
            monitorMode = true;
            codeword = BinaryStdIn.readInt(W);  //Need to read in next bit of data
            if (codeword == R) return;
            val = st[codeword];
        }

        while (true) {
            if (i == L && W != 16) {    //Handles variable width expansion
                L *= 2;
                W++;
            }

            if (i == 65536 && resetMode) {    //Resets dictionary when we have used total number of codewords
                st = new String[65536];
                for (i = 0; i < R; i++)
                    st[i] = "" + (char) i;
                st[i++] = "";
                L = 512;
                W = 9;
            }

            if (i == 65536 && monitorMode) {
                if (initialCompression) {
                    initialCompressionRatio = uncompressedSize / compressedSize;
                    initialCompression = false;

                } else {
                    newCompressionRatio = uncompressedSize / compressedSize;
                    ratioOfRatios = initialCompressionRatio / newCompressionRatio;

                    if (ratioOfRatios > 1.1) {       //Reset the codebook if ratio exceeds threshold
                        st = new String[65536];
                        for (i = 0; i < R; i++)
                            st[i] = "" + (char) i;
                        st[i++] = "";
                        L = 512;
                        W = 9;

                        initialCompression = true;   //Need to reset count of compressed and uncompressed data
                        uncompressedSize = 0;
                        compressedSize = 0;
                    }
                }
            }


            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W); //Reads in W bits
            if (codeword == R) break;          //If at EOF
            String s = st[codeword];           //Get next character
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L) st[i++] = val + s.charAt(0);
            val = s;

            compressedSize += W;
            uncompressedSize += ((val.length()) * 8);
        }
        BinaryStdOut.close();
    }


    public static void main(String[] args) {
        if (args[0].equals("-")) compress(args[1]);
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
