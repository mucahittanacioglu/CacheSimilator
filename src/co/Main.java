package co;

import java.io.*;
import java.util.Scanner;

public class Main {
    private static Cache L1DataCache,L1InstructionCache,L2Cache;
    private static String ramString="";
    private static long start = System.nanoTime();
    public static void main(String[] args) throws Exception {


        //initilazing Caches with names
        L1DataCache = new Cache("L1DataCache");
        L1InstructionCache = new Cache("L1InstructionCache");
        L2Cache = new Cache("L2Cache");




        initCache(args,L1DataCache,L1InstructionCache,L2Cache);//function to initilize caches base on given program argumants
        printCache(L1DataCache);
        printCache(L1InstructionCache);
        printCache(L2Cache);

        startReadingFile("traces/"+args[args.length-1],L1InstructionCache,L1DataCache,L2Cache);//after initilazing cache begins reading tracefile

        System.err.println("After------------------------------------------");

        printCache(L1DataCache);
        printCache(L1InstructionCache);
        printCache(L2Cache);
        printTotalHitMissAndEviction(L1DataCache);
        printTotalHitMissAndEviction(L1InstructionCache);//prints total miss hit and eviction of each cache
        printTotalHitMissAndEviction(L2Cache);

        //writes modified ram to txt file
        writeNewRam();

        writeCacheInfo(L1InstructionCache);//writes cache's info to text file
        writeCacheInfo(L1DataCache);
        writeCacheInfo(L2Cache);
    }

    private static void initCache(String argv[],Cache L1Data,Cache L1Instruction,Cache L2){
        //initilazing caches base on given program arguments
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
        //initilazing cache table with given s e and b values
        initCacheTables(L1Data);
        initCacheTables(L1Instruction);
        initCacheTables(L2);
        //setting 0 to cache table
        initCacheWith0(L1Data);
        initCacheWith0(L2);
        initCacheWith0(L1Instruction);
    }


    private static void startReadingFile(String arg,Cache L1I,Cache L1D,Cache L2)throws Exception {

        String inputStr;
        File ram= new File("ram.txt");
        Scanner reader = new Scanner(ram);
        //taking ram image to ramString variable
        while(reader.hasNextLine()){
            ramString+=reader.nextLine();
        }
        ramString=ramString.replaceAll("\\s+","");
        reader.close();

        //here begins read of trace file
        File traceFile= new File(arg);
        Scanner reader2 = new Scanner(traceFile);
        while(reader2.hasNextLine()){
            inputStr=reader2.nextLine();
            checkCaches(inputStr,L1I,L1D,L2);//after each line caches checking up
        }
        reader2.close();

    }

    private static void checkCaches(String inputStr,Cache L1I,Cache L1D,Cache L2) throws Exception {
        //first we seperate our instruction as adress , instraction type,size and data
        char type = inputStr.charAt(0);//type hold instruction type which is always at input's index 0
        String adress="",size="",adressBinary="",data="";
        int adressVal=0;
        size+=inputStr.charAt(12);//size info always on the same index inputs 12's

        // here if instruction Modify or Store we neeed data part for other instructions there is no data part.
        if(type=='M'||type=='S')
            for(int i = 15;i<inputStr.length();i++)
                data+=inputStr.charAt(i);

        //getting adress as hex.
        for(int i =2; inputStr.charAt(i)!=',';i++){
            adress+=inputStr.charAt(i);
        }
        //geting adres as binary array
        adressBinary=getBinaryForm(adress);
        // here checks cache's memorys for data base on instruction
        switch (type){
            case 'I':
                checkDataTable(inputStr,L1InstructionCache,adressBinary,Integer.parseInt(size),type,data);
                checkDataTable(inputStr,L2Cache,adressBinary,Integer.parseInt(size),type,data);
                break;
            case 'L':
                checkDataTable(inputStr,L1DataCache,adressBinary,Integer.parseInt(size),type,data);
                checkDataTable(inputStr,L2Cache,adressBinary,Integer.parseInt(size),type,data);
                break;
            case 'S':
                checkDataTable(inputStr,L1DataCache,adressBinary,Integer.parseInt(size),type,data);
                checkDataTable(inputStr,L2Cache,adressBinary,Integer.parseInt(size),type,data);
                break;
            case 'M'://on modifiy firs we load data then store modfied version
                checkDataTable(inputStr,L1DataCache,adressBinary,Integer.parseInt(size),'L',data);
                checkDataTable(inputStr,L2Cache,adressBinary,Integer.parseInt(size),'L',data);

                checkDataTable(inputStr,L1DataCache,adressBinary,Integer.parseInt(size),'S',data);
                checkDataTable(inputStr,L2Cache,adressBinary,Integer.parseInt(size),'S',data);

                break;
            default:
                System.err.println("There is no such a  insturction as "+type);
                break;
        }

    }

    private static void checkDataTable(String inputStr, Cache cache,String adressBinary,int size,char type,String data)throws Exception {

        String tag = adressBinary.substring(0,adressBinary.length()-getLog2(cache.getBlockSize())-getLog2(cache.getNumberOfSets())); // getting tag bit as binaryform of adress - blockoffset-setbits
        int setIndex = binaryToInt(adressBinary.substring(tag.length(),adressBinary.length()-getLog2(cache.getBlockSize())));//calculating set index as part  between tagbit and blockoffset

        for(int i = 0; i< cache.getLinePerSet();i++){
            if((cache.getDataTable()[setIndex][i][0].equalsIgnoreCase(tag) && cache.getDataTable()[setIndex][i][1].equalsIgnoreCase("1"))){//if tag bit are equal and valid bit is 1 its hit
                System.out.println(cache.getName()+" hit.");
                cache.setHits(cache.getHits()+1);
                if(type=='S') {//if instruction store we fallow  write-through rule
                    writeRam(adressBinary, data);
                    cache.getDataTable()[setIndex][i][2]=data;//updeting value in case of this store comes from Modify instruction
                }
                return;
            }
        }
        System.out.println(cache.getName()+" miss.\n\n");
        cache.setMiss(cache.getMiss()+1);

        if(type=='S') {//if instruction store we fallow  no write allocate  rule
            writeRam(adressBinary, data);
            return;
        }
        //for other instructions we load data from ram to cache
        loadTocache(cache,tag,setIndex,inputStr,binaryToInt(adressBinary),size);
    }

    private static void loadTocache(Cache cache, String tag, int setIndex, String inputStr,int adressVal,int size) {
        String data="";
        int loc;
        // calculating location of adress on ram image
        if(adressVal==0)
            loc=0;
        else
            loc= adressVal*2-2;
        //reading data from ram
        for(int i = loc;i<loc+2*size;i++){
            data+=ramString.charAt(i);
        }
        for(int i = 0; i < cache.getLinePerSet();i++){
            if( cache.getDataTable()[setIndex][i][1].equalsIgnoreCase("0")){//checking is there any empty line on given set
                cache.getDataTable()[setIndex][i][0]=tag;//sets tag bits
                cache.getDataTable()[setIndex][i][1]="1";//sets valid bit
                cache.getDataTable()[setIndex][i][2]=data;//sets data
                cache.getDataTable()[setIndex][i][3]= ((System.nanoTime()-start))/1000+"";//insertion time to fallow first in first out rule
                return;
            }
        }
        //if given set is full we need eviction
        System.out.println(cache.getName()+" eviction\n\n");
        cache.setEviction(cache.getEviction()+1);
        //here finding first inserted element among all element
        int minIndex=0;
        long min = Long.parseLong(cache.getDataTable()[setIndex][0][3]),temp;
        for(int i = 0; i < cache.getLinePerSet();i++){
            temp =Long.parseLong(cache.getDataTable()[setIndex][i][3]);
            if(temp < min){
                min =temp;
                minIndex=i;
            }
        }
        //after finding first inserted element we do eviction and set new values to table
        cache.getDataTable()[setIndex][minIndex][0]=tag;
        cache.getDataTable()[setIndex][minIndex][1]="1";
        cache.getDataTable()[setIndex][minIndex][2]=data;
        cache.getDataTable()[setIndex][minIndex][3]= ((System.nanoTime()-start))/1000+"";

    }

    //initilazing cache table
    private static void initCacheTables(Cache cache){

        int dim1=cache.getNumberOfSets(),dim2=cache.getLinePerSet();

        int lineSize =1+1+1+1;//1 for tag par 1 for valid bit,1 block(data gonna store here),1 for insertion;

        cache.setSizeOfEachLine(lineSize);

        cache.setDataTable(new String[dim1][dim2][lineSize]);
    }

    //setting 0 to table
    private static void initCacheWith0(Cache cache){
        for(int i = 0 ;i < cache.getNumberOfSets();i++){
            for(int j = 0 ; j < cache.getLinePerSet();j++){
                for(int k = 0 ; k < cache.getSizeOfEachLine();k++){
                    cache.getDataTable()[i][j][k]="0";
                }

            }
        }
    }

    //this method returns binary value of given hexadecimal string
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

    private static void writeRam(String adressBinary, String data) {
        int adressVal = binaryToInt(adressBinary);//calculating adress value as integer from binary array

        char[] newram = ramString.toCharArray();//copying current ram image to char array

        System.err.println("Updating ram...\n");
        //upaddting ram with given data
        for(int i = adressVal;i<adressVal+data.length();i++){
            newram[i]=data.charAt(i-adressVal);
        }
        ramString = String.valueOf(newram);//updating current ram with new ram image

    }

    //simple method that returns integer value of given binary array
    private static int binaryToInt(String binary){
        int val=0;
        for(int i = 0 ; i<binary.length();i++){
            if(binary.charAt(i)=='1'){
                val += Math.pow(2,binary.length()-1-i);
            }
        }
        return  val;
    }

    //simple method that returns log2 value of given integer
    private static int getLog2(int number){
        int value=0;
        while(number>=2){
            number/=2;
            value++;
        }
        return value;
    }

    private static void writeNewRam()throws Exception {
        File ram = new File("newRam.txt");
        ram.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(ram));
        int k = 0;
        for(int i = 0;i<ramString.length();i++) {
            if(k%4==0 && k!=0)//spliting 2byte blocks
                bw.write(" ");
            if (k % 32 == 0 && k!=0 )//making 8 number of 2 byte block per line
                bw.newLine();
            bw.write(ramString.charAt(i));
            k++;


        }
        bw.close();
    }

    //this method writes given cache's dataTable to text file which  has cachename.txt
    private static void writeCacheInfo(Cache cache)throws Exception{

        File cche = new File(cache.getName()+".txt");
        cche.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(cche));

        bw.write(cache.getName()+"-------------------------\n");

        for(int i = 0 ;i < cache.getNumberOfSets();i++){
            bw.write("\nSet:"+i+"----\n");
            for(int j = 0 ; j < cache.getLinePerSet();j++){
                bw.write("\n\tLine:"+j+"\n\t\t");
                for(int k = 0 ; k < cache.getSizeOfEachLine();k++){
                    switch (k){
                        case 0:
                            bw.write("Tag: ");
                            break;
                        case 1:
                            bw.write("Valid bit: ");
                            break;
                        case 2:
                            bw.write("Data: ");
                            break;
                        case 3:
                            bw.write("Insert time: ");
                            break;
                    }
                    bw.write(cache.getDataTable()[i][j][k]+"\t");

                }

            }
        }
        bw.write("\n\n"+cache.getName()+"----\n"+"Total Hits: "+cache.getHits()+"\nTotal Misses:"+cache.getMiss()+"\nTotal Evictions:"+cache.getEviction()+"\n\n");
        bw.close();
    }

    //This method prints given cache's data table to console
    private static void printCache(Cache cache){

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
    //prints cache hit miss and eviction values to console
    private static void printTotalHitMissAndEviction(Cache cache){
        System.out.println(cache.getName()+"----\n"+"Total Hits: "+cache.getHits()+"\nTotal Misses:"+cache.getMiss()+"\nTotal Evictions:"+cache.getEviction()+"\n\n");
    }
    // cache inner class
    private static class Cache{
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

        public Cache(String name) {
            this.setName(name);
        }

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


