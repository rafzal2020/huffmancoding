package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    private int countChar(char[] s, char c) {
        int count = 0; 
        for (int i = 0; i < s.length; i++) {
            if (s[i] == c)
                count++;
        }
        return count; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        StdIn.setFile(fileName);

	    /* Your code goes here */
        char[] chars = new char[128];
        double[] occs = new double[128];
        sortedCharFreqList = new ArrayList<>();
        String cs = "";
        while (StdIn.hasNextChar()) {
            char c = StdIn.readChar();
            cs = cs + c;
        }
        char[] charString = cs.toCharArray();
        for (char c : charString) {
            double count = countChar(charString, c)/(double)charString.length;
            occs[c] = count;
        }
        
        for (int i = 0; i < charString.length; i++) {
            for (int j = 0; j < chars.length; j++) {
                if (charString[i] == j){ 
                    chars[j] = charString[i];
                }
            }
        }

        for (int i = 0; i < occs.length; i++) {
            if (chars[i] != 0) {
                CharFreq cf = new CharFreq(chars[i], occs[i]);
                sortedCharFreqList.add(cf);
            }
        }
        if (sortedCharFreqList.size() == 1) {
            char q = sortedCharFreqList.get(0).getCharacter();
            char q1;
            if (q == (char)127) {
                q1 = (char)(0);
            } else {
                int qtemp = (int)q + 1;
                q1 = (char)qtemp;
            }
            CharFreq ncf = new CharFreq(q1, 0);
            sortedCharFreqList.add(ncf);
        }

        Collections.sort(sortedCharFreqList);
        
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {

	    /* Your code goes here */
        Queue<TreeNode> source = new Queue<TreeNode>();
        Queue<TreeNode> target = new Queue<TreeNode>();
        TreeNode[] tn = new TreeNode[2];
        TreeNode left = null;
        TreeNode right = null;
        
        for (CharFreq cf : sortedCharFreqList) {
            source.enqueue(new TreeNode(cf, null, null));
        }
        while (source.size() + target.size() > 1) {   
            for (int i = 0; i < 2; i++) {
                if (!source.isEmpty() && !target.isEmpty()) {
                    if (source.peek().getData().getProbOcc() > target.peek().getData().getProbOcc()) {
                        tn[i] = target.dequeue();
                    } else tn[i] = source.dequeue();
                } else if (target.isEmpty()) {
                    tn[i] = source.dequeue();
                } else if (source.isEmpty()) tn[i] = target.dequeue();
            }

            left = tn[0];
            right = tn[1];

            double freqSum = left.getData().getProbOcc() + right.getData().getProbOcc();

            CharFreq cf = new CharFreq(null, freqSum);
            TreeNode temp = new TreeNode(cf, left, right);
            target.enqueue(temp);
        }
        huffmanRoot = target.peek();
    }


    private void traverse(TreeNode root, String s, String[] bE) {
        if (root.getLeft() == null && root.getRight() == null) { // if the current root does not have a child.
            char temp = root.getData().getCharacter();
            int i = (int) temp;
            bE[i] = s; // insert the character at its respective integer equivalent index.
            return;
        } 
        traverse(root.getLeft(), s + 0, bE);
        traverse(root.getRight(), s + 1, bE);
            
    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {

	    /* Your code goes here */
        String bitCode = "";
        String[] bc = new String[128];
        traverse(huffmanRoot, bitCode, bc);
        encodings = bc;
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);

	    /* Your code goes here */
        String bitCode = "";
        while (StdIn.hasNextChar()) {
            char curr = StdIn.readChar();
            for (int i = 0; i < encodings.length; i++) {
                if (i == (int)curr) {
                    bitCode += encodings[i];
                }
            }
        }
        writeBitString(encodedFile, bitCode);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);

	    /* Your code goes here */
        String bitString = readBitString(encodedFile);
        TreeNode temp = huffmanRoot;
        for (int i = 0; i < bitString.length(); i++) {
            if (bitString.charAt(i) == '0') {
                temp = temp.getLeft();
                if (temp.getData().getCharacter() != null) {
                    StdOut.print(temp.getData().getCharacter());
                    temp = huffmanRoot;
                }
            } else if (bitString.charAt(i) == '1') {
                temp = temp.getRight();
                if (temp.getData().getCharacter() != null) {
                    StdOut.print(temp.getData().getCharacter());
                    temp = huffmanRoot;
                }
            }
        }

    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
