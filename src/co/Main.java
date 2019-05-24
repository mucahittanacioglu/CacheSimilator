package co;

import java.io.File;
import java.util.Scanner;

public class Main {
    private static L2Cache l2;
    private static L1DataCache l1d;
    private static L1InstrcCache l1I;
    private static String inputString,url;
    private static File Ram = new File("RAM.dat");

    public static void main(String[] args) throws Exception {
        l2= initL2Cache(args);
        l1d = (L1DataCache)initL1Cache(args,true);
        l1I= (L1InstrcCache) initL1Cache(args,false);

        url= "traces/"+args[args.length-1];
        File input = new File(url);
        Scanner reader = new Scanner(input);


        while(reader.hasNextLine()) {
            inputString = reader.nextLine();
            if(!checkCache(inputString))
                addCache(inputString);
        }
        reader.close();
    }

    private static boolean checkCache(String inputString) {
        char instruction= inputString.charAt(0);
        String[] input = inputString.substring(1).trim().split(",");
        for(int i = 0; i < (int)Math.pow(2,l1d.getSetBit()) ; i++){
            for(int j = 0; j < l1d.numberOfline ; j++){
            if(instruction=='I')
            }
        }
        return false;
    }

    private static void addCache(String inputString) {
    }

    private static L2Cache initL2Cache(String[] args) {
        int setSize=0,blockOffset=0,linePerSet=0;
        for(int i = 0; i<args.length;i++){
            if(args[i].equalsIgnoreCase("-L2s"))
                setSize=Integer.parseInt(args[i+1]);
            if(args[i].equalsIgnoreCase("-L2E"))
                linePerSet=Integer.parseInt(args[i+1]);
            if(args[i].equalsIgnoreCase("-L2b"))
                blockOffset = Integer.parseInt(args[i+1]);

        }
        L2Cache temp = new L2Cache(blockOffset,setSize,linePerSet);
        return temp;
    }
    private static Object initL1Cache(String[] args,boolean isData) {

        int setSize=0,blockOffset=0,linePerSet=0;
        for(int i = 0; i<args.length;i++){
            if(args[i].equalsIgnoreCase("-L1s"))
                setSize=Integer.parseInt(args[i+1]);
            if(args[i].equalsIgnoreCase("-L1E"))
                linePerSet=Integer.parseInt(args[i+1]);
            if(args[i].equalsIgnoreCase("-L1b"))
                blockOffset=Integer.parseInt(args[i+1]);
        }

            L1DataCache l1DataCache = new L1DataCache(blockOffset,setSize,linePerSet);
            L1InstrcCache l1InstrcCache = new L1InstrcCache(blockOffset,setSize,linePerSet);

            return isData ? l1DataCache:l1InstrcCache;
    }

    static class L2Cache{
        private int blockOffset;
        private int setBit;
        private int numberOfline;
        private String[][][] cacheTable;
        L2Cache(){};
        L2Cache(int blockOffset,int setBit,int numberOfline){
            this.setBlockOffset(blockOffset);
            this.setSetBit(setBit);
            this.setNumberOfline(numberOfline);
            cacheTable = new String[(int)Math.pow(2,setBit)][numberOfline][3];
        }
        public int getBlockOffset() {
            return blockOffset;
        }

        public void setBlockOffset(int blockOffset) {
            this.blockOffset = blockOffset;
        }

        public int getSetBit() {
            return setBit;
        }

        public void setSetBit(int setBit) {
            this.setBit = setBit;
        }

        public int getNumberOfline() {
            return numberOfline;
        }

        public void setNumberOfline(int numberOfline) {
            this.numberOfline = numberOfline;
        }


    }
    static class L1DataCache{
        private int blockOffset;
        private int setBit;
        private int numberOfline;
        private String[][][] cacheTable;
        L1DataCache(){};
        L1DataCache(int blockOffset,int setBit,int numberOfline){
            this.setBlockOffset(blockOffset);
            this.setSetBit(setBit);
            this.setNumberOfline(numberOfline);
            cacheTable = new String[(int)Math.pow(2,setBit)][numberOfline][3];

        }
        public int getBlockOffset() {
            return blockOffset;
        }

        public void setBlockOffset(int blockOffset) {
            this.blockOffset = blockOffset;
        }

        public int getSetBit() {
            return setBit;
        }

        public void setSetBit(int setBit) {
            this.setBit = setBit;
        }

        public int getNumberOfline() {
            return numberOfline;
        }

        public void setNumberOfline(int numberOfline) {
            this.numberOfline = numberOfline;
        }

    }
    static class L1InstrcCache{
        private int blockOffset;
        private int setBit;
        private int numberOfline;
        private String[][][] cacheTable;
        L1InstrcCache(){};
        L1InstrcCache(int blockOffset,int setBit,int numberOfline){
            this.setBlockOffset(blockOffset);
            this.setSetBit(setBit);
            this.setNumberOfline(numberOfline);
            cacheTable = new String[(int)Math.pow(2,setBit)][numberOfline][3];
        }

        public int getBlockOffset() {
            return blockOffset;
        }

        public void setBlockOffset(int blockOffset) {
            this.blockOffset = blockOffset;
        }

        public int getSetBit() {
            return setBit;
        }

        public void setSetBit(int setBit) {
            this.setBit = setBit;
        }

        public int getNumberOfline() {
            return numberOfline;
        }

        public void setNumberOfline(int numberOfline) {
            this.numberOfline = numberOfline;
        }
    }
}
