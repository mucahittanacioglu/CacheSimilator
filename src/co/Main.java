package co;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    private static Cache L1DataCache,L1InstructionCache,L2Cache;
    private static String ramString="";
    public static void main(String[] args) throws Exception {
        L1DataCache = new Cache();
        L1InstructionCache = new Cache();
        L2Cache = new Cache();
        L1DataCache.setName("L1DataCache");
        L1InstructionCache.setName("L1Instruction");
        L2Cache.setName("L2Cache");

        initCache(args,L1DataCache,L1InstructionCache,L2Cache);
        testCache(L1DataCache);
        testCache(L1InstructionCache);
        testCache(L2Cache);

        startReadingFile("traces/"+args[args.length-1],L1InstructionCache,L1DataCache,L2Cache);
        System.err.println("After------------------------------------------");

        testCache(L1DataCache);
        testCache(L1InstructionCache);
        testCache(L2Cache);
        printTotalHitMissAndEviction(L1DataCache);
        printTotalHitMissAndEviction(L1InstructionCache);
        printTotalHitMissAndEviction(L2Cache);
    }

    public static void startReadingFile(String arg,Cache L1I,Cache L1D,Cache L2)throws Exception {

        String inputStr;
        File ram= new File("ram.txt");
        Scanner reader = new Scanner(ram);
        while(reader.hasNextLine()){
            ramString+=reader.nextLine();
        }
        ramString=ramString.replaceAll("\\s+","");
        reader.close();

        File traceFile= new File(arg);
        Scanner reader2 = new Scanner(traceFile);
        while(reader2.hasNextLine()){
            inputStr=reader2.nextLine();
            checkCaches(inputStr,L1I,L1D,L2);
        }
        reader2.close();

    }

    public static void checkCaches(String inputStr,Cache L1I,Cache L1D,Cache L2) throws Exception {
        char type = inputStr.charAt(0);
        String adress="",size="",adressBinary="";
        int adressVal=0;
        size+=inputStr.charAt(12);

        for(int i =2; inputStr.charAt(i)!=',';i++){
            adress+=inputStr.charAt(i);
        }

        adressBinary=getBinaryForm(adress);

        switch (type){
            case 'I':
                checkDataTable(inputStr,L1InstructionCache,adressBinary,Integer.parseInt(size));
                checkDataTable(inputStr,L2Cache,adressBinary,Integer.parseInt(size));
                break;
            case 'L':
                checkDataTable(inputStr,L1DataCache,adressBinary,Integer.parseInt(size));
                checkDataTable(inputStr,L2Cache,adressBinary,Integer.parseInt(size));
                break;
            case 'S':
                checkDataTable(inputStr,L1DataCache,adressBinary,Integer.parseInt(size));
                checkDataTable(inputStr,L2Cache,adressBinary,Integer.parseInt(size));
                break;
            case 'M':
                checkDataTable(inputStr,L1DataCache,adressBinary,Integer.parseInt(size));
                checkDataTable(inputStr,L2Cache,adressBinary,Integer.parseInt(size));
                checkDataTable(inputStr,L1DataCache,adressBinary,Integer.parseInt(size));
                checkDataTable(inputStr,L2Cache,adressBinary,Integer.parseInt(size));
                break;
            default:
                System.err.println("There is no such a  insturction as "+type);
                break;
        }

    }

    private static String getBinaryForm(String adress)throws Exception {
        String temp="";
        for(int i = 0;i<adress.length();i++){

            switch(adress.charAt(i)){
                case '0':  temp+="0000"; break;
                case '1':  temp+="0001"; break;
                case '2':  temp+="0010"; break;
                case '3':  temp+="0011"; break;
                case '4':  temp+="0100"; break;
                case '5':  temp+="0101"; break;
                case '6':  temp+="0110"; break;
                case '7':  temp+="0111"; break;
                case '8':  temp+="1000"; break;
                case '9':  temp+="1001"; break;
                case 'a':  temp+="1010"; break;
                case 'b':  temp+="1011"; break;
                case 'c':  temp+="1100"; break;
                case 'd':  temp+="1101"; break;
                case 'e':  temp+="1110"; break;
                case 'f':  temp+="1111"; break;
            }
        }
        return temp;
    }

    private static void checkDataTable(String inputStr, Cache cache,String adressBinary,int size)throws Exception {
        String tag = adressBinary.substring(0,adressBinary.length()-getLog2(cache.getBlockSize())-getLog2(cache.getNumberOfSets()));
        int setIndex = binaryToInt(adressBinary.substring(tag.length(),adressBinary.length()-getLog2(cache.getBlockSize())));

        for(int i = 0; i< cache.getLinePerSet();i++){
            if((cache.getDataTable()[setIndex][i][0].equalsIgnoreCase(tag) && cache.getDataTable()[setIndex][i][1].equalsIgnoreCase("1"))){
                System.out.println(cache.getName()+" hit.");
                cache.setHits(cache.getHits()+1);
                return;
            }
        }

        System.out.println(cache.getName()+" miss.\n\n");
        cache.setMiss(cache.getMiss()+1);
        loadTocache(cache,tag,setIndex,inputStr,binaryToInt(adressBinary),size);
    }

    private static void loadTocache(Cache cache, String tag, int setIndex, String inputStr,int adressVal,int size)throws Exception {
        String data="";
        int loc;
        if(adressVal==0)
            loc=0;
        else
            loc= adressVal*2-2;
        for(int i = loc;i<loc+2*size;i+=2){
            data+=ramString.charAt(i);
        }
        for(int i = 0; i < cache.getLinePerSet();i++){
            if( cache.getDataTable()[setIndex][i][1].equalsIgnoreCase("0")){
                cache.getDataTable()[setIndex][i][0]=tag;
                cache.getDataTable()[setIndex][i][1]="1";
                cache.getDataTable()[setIndex][i][2]=data;
                return;
            }else{
                System.out.println(cache.getName()+" eviction\n\n");
                cache.setEviction(cache.getEviction()+1);
            }

        }


    }

    private static int binaryToInt(String binary){
        int val=0;
        for(int i = 0 ; i<binary.length();i++){
            if(binary.charAt(i)=='1'){
                val += Math.pow(2,binary.length()-1-i);
            }
        }
        return  val;
    }

    private static int getLog2(int number){
        int value=0;
        while(number>=2){
            number/=2;
            value++;
        }
        return value;
    }

    public static void initCache(String argv[],Cache L1Data,Cache L1Instruction,Cache L2){

        for(int i = 0; i < argv.length;i++) {
            if (argv[i].equalsIgnoreCase( "-L2s" ))
                L2.setNumberOfSets((int)Math.pow(2,Integer.parseInt(argv[i+1])));
            else if (argv[i].equalsIgnoreCase("-L2E"))
                L2.setLinePerSet(Integer.parseInt(argv[i + 1]));
            else if (argv[i].equalsIgnoreCase("-L2b"))
                L2.setBlockSize((int)Math.pow(2,Integer.parseInt(argv[i+1])));
            else if (argv[i].equalsIgnoreCase("-L1s")) {
                L1Data.setNumberOfSets((int)Math.pow(2,Integer.parseInt(argv[i+1])));
                L1Instruction.setNumberOfSets((int)Math.pow(2,Integer.parseInt(argv[i+1])));
            }else if (argv[i].equalsIgnoreCase("-L1E")){
                L1Data.setLinePerSet(Integer.parseInt(argv[i+1]));
                L1Instruction.setLinePerSet(Integer.parseInt(argv[i+1]));
            }else if(argv[i].equalsIgnoreCase("-L1b")) {
                L1Instruction.setBlockSize((int)Math.pow(2,Integer.parseInt(argv[i+1])));
                L1Data.setBlockSize((int)Math.pow(2,Integer.parseInt(argv[i+1])));
            }
        }
        initCacheTables(L1DataCache);
        initCacheTables(L1InstructionCache);
        initCacheTables(L2Cache);
        initCacheWith0(L1DataCache);
        initCacheWith0(L2Cache);
        initCacheWith0(L1Instruction);
    }

    public static void initCacheTables(Cache cache){

        int dim1=cache.getNumberOfSets(),dim2=cache.getLinePerSet();
        int lineSize =1+1+1+1;//1 for tag par 1 for valid bit,1 block(data gonna store here),1 for time its in;

        cache.setSizeOfEachLine(lineSize);

        cache.setDataTable(new String[dim1][dim2][lineSize]);
    }
    public static void initCacheWith0(Cache cache){
        for(int i = 0 ;i < cache.getNumberOfSets();i++){
            for(int j = 0 ; j < cache.getLinePerSet();j++){
                for(int k = 0 ; k < cache.getSizeOfEachLine();k++){
                    cache.getDataTable()[i][j][k]="0";
                }

            }
        }
    }
    public static void testCache(Cache cache){



        System.out.println(cache.getName()+"-----------\n");

        for(int i = 0 ;i < cache.getNumberOfSets();i++){
            System.out.println("Set:"+i+"----\n");
            for(int j = 0 ; j < cache.getLinePerSet();j++){
                System.out.println("Line:"+j+"\n\t");
                for(int k = 0 ; k < cache.getSizeOfEachLine();k++){
                    System.out.print("\t"+cache.getDataTable()[i][j][k]);

                }
                System.out.println("\n");
            }
        }
    }
    public static void printTotalHitMissAndEviction(Cache cache){
        System.out.println(cache.getName()+"----\n"+"Total Hits: "+cache.getHits()+"\nTotal Misses:"+cache.getMiss()+"\nTotal eviction:"+cache.getEviction());
    }

    public static class Cache{
        private String name;
        private String[][][] dataTable;
        private int sizeOfEachLine;
        private int tagSize;
        private int hits;
        private int miss;
        private int eviction;
        private int numberOfSets;
        private int linePerSet;
        private int blockSize;

        public Cache(String name, int numberOfSets, int linePerSet, int blockSize) {
            this.setName(name);
            this.setNumberOfSets(numberOfSets);
            this.setLinePerSet(linePerSet);
            this.setBlockSize(blockSize);
        }

        public Cache(){}//Default constructor to create cahches empty first

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String[][][] getDataTable() {
            return dataTable;
        }

        public void setDataTable(String[][][] dataTable) {
            this.dataTable = dataTable;
        }

        public int getSizeOfEachLine() {
            return sizeOfEachLine;
        }

        public void setSizeOfEachLine(int sizeOfEachLine) {
            this.sizeOfEachLine = sizeOfEachLine;
        }

        public int getTagSize() {
            return tagSize;
        }

        public void setTagSize(int tagSize) {
            this.tagSize = tagSize;
        }

        public int getHits() {
            return hits;
        }

        public void setHits(int hits) {
            this.hits = hits;
        }

        public int getMiss() {
            return miss;
        }

        public void setMiss(int miss) {
            this.miss = miss;
        }

        public int getEviction() {
            return eviction;
        }

        public void setEviction(int eviction) {
            this.eviction = eviction;
        }

        public int getNumberOfSets() {
            return numberOfSets;
        }

        public void setNumberOfSets(int numberOfSets) {
            this.numberOfSets = numberOfSets;
        }

        public int getLinePerSet() {
            return linePerSet;
        }

        public void setLinePerSet(int linePerSet) {
            this.linePerSet = linePerSet;
        }

        public int getBlockSize() {
            return blockSize;
        }

        public void setBlockSize(int blockSize) {
            this.blockSize = blockSize;
        }
    }
}


