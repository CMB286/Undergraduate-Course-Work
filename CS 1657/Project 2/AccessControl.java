import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.util.regex.*;

public class AccessControl {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        Scanner input = new Scanner(System.in);
        String userInput = "", query = "";
        int userInt;

        System.out.println("Hello! Welcome to our Restaurant! Please Enter a Value Corresponding to Your Requested Type of Query: ");
        System.out.println("    1: Can [user] access [menu/kitchen/dining room/reservation/pizza oven/pasta dough/fish station/meat station/pastry station/cancel reservation] with [read/write] permission");
        System.out.println("    2: Is [user] a member of domain [owner/headchef/manager/chef/customer]");
        System.out.println("    3: Is [user] a [pizza/pasta/fish/meat/pastry] chef");
        System.out.println("    4: Run Test");
        System.out.println("    5: EXIT PROGRAM");
        userInput = input.nextLine();

        if(userInput.equals("5")){
            input.close();
            return;
        }

        do{
            while(!userInput.equals("1") && !userInput.equals("2") && !userInput.equals("3") && !userInput.equals("4") && !userInput.equals("5")){
                System.out.println(userInput + " is not between 1-5! Enter a proper input:");
                System.out.println("\n    1: Can [user] access [menu/kitchen/dining room/reservation/pizza oven/pasta dough/fish station/meat station/pastry station/cancel reservation] with [read/write] permission");
                System.out.println("    2: Is [user] a member of domain [owner/headchef/manager/chef/customer]");
                System.out.println("    3: Is [user] a [pizza/pasta/fish/meat/pastry] chef");
                System.out.println("    4: Run Test");
                System.out.println("    5: EXIT PROGRAM");
                userInput = input.nextLine();
            }

            userInt = Integer.parseInt(userInput);
            //Based on the case, user's will enter their queries in the specifying format
            switch(userInt){
                case 1:
                    System.out.println("\nEnter your query in the format, including the []'s: Can [user] access [menu/kitchen/dining room/reservation/pizza oven/pasta dough/fish station/meat station/pastry station/cancel reservation] with [read/write] permission");
                    query = input.nextLine();
                    accessControl(query, userInt, 0);
                    break;
                case 2:
                    System.out.println("\nEnter your query in the format, including the []'s: Is [user] a member of domain [owner/headchef/manager/chef/customer]");
                    query = input.nextLine();
                    accessControl(query, userInt, 0);
                    break;
                case 3:
                    System.out.println("\nEnter your query in the format, including the []'s: Is [user] a [pizza/pasta/fish/meat/pastry] chef");
                    query = input.nextLine();
                    accessControl(query, userInt, 0);
                    break;
                case 4:
                    runTest();
                    break;
                case 5:
                    input.close();
                    return;
            }

            System.out.println("\n    1: Can [user] access [menu/kitchen/dining room/reservation/pizza oven/pasta dough/fish station/meat station/pastry station/cancel reservation] with [read/write] permission");
            System.out.println("    2: Is [user] a member of domain [owner/headchef/manager/chef/customer]");
            System.out.println("    3: Is [user] a [pizza/pasta/fish/meat/pastry] chef");
            System.out.println("    4: Run Test");
            System.out.println("    5: EXIT PROGRAM");
            userInput = input.nextLine();
        } while(true);
    }


    /*
    *   1: Find user, look in what checking access for, if found, check access type
    *   2: Find user
    *   3: Find user, if chef, check type 
    */
    public static void accessControl(String query, int userInt, int testMode) throws ParserConfigurationException, SAXException, IOException{
        //Split query into information we need
        String name, role, entity, accessLevel, type, verdict;
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(query);
        String[] attribute, temp;


        if(userInt == 1){
            matcher.find();
            name = matcher.group(1);
            matcher.find();
            entity = matcher.group(1).replaceAll("\\s", "");
            matcher.find();
            accessLevel = matcher.group(1);

            attribute = findRole(name);
            role = attribute[0];
            temp = findRole(role);

            if(!temp[0].equals("User Not Found!")){
                verdict = findAccess(temp, entity, accessLevel);
                if(verdict.equals("y") && testMode == 0){
                    System.out.println("\n" + name + " has " + accessLevel + " access to " + entity);
                    return;
                }
            }
            
            verdict = findAccess(attribute, entity, accessLevel);
            if(verdict.equals("y") && testMode == 0){
                System.out.println("\n" + name + " has " + accessLevel + " access to " + entity);
                return;
            }
            if(verdict.equals("n") && testMode == 0){
                System.out.println("\n" + name + " does not have " + accessLevel + " access to " + entity);
            }
        }
        if(userInt == 2){
            matcher.find();
            name = matcher.group(1);
            matcher.find();
            role = matcher.group(1).replaceAll("\\s", "");
            
            temp = findRole(name);
            if(temp[0].equals("User Not Found!") && testMode == 0){
                System.out.println("\n" + name + " is NOT a Member of " + role + "!");
                return;
            }

            if(temp[0].equals(role) && testMode == 0){
                System.out.println("\n" + name + " is a Member of " + role + "!");
                return;
            }
            if(testMode == 0){
                System.out.println("\n" + name + " is NOT a Member of " + role + "!");
            }
        }
        if(userInt == 3){
            matcher.find();
            name = matcher.group(1);
            matcher.find();
            type = matcher.group(1);
            role = "chef";
            temp = findRole(name);
            
            if(role.equals(temp[0]) && type.equals(temp[1]) && testMode == 0){
                System.out.println("\n" + name + " is a " + type + " chef!");
                return;
            }
            if(testMode == 0){
                System.out.println("\n" + name + " is a NOT " + type + " chef!");
            }
        }
    }

    public static String[] findRole(String user) throws ParserConfigurationException, SAXException, IOException{
        File xmlFile;
        String name, domain, type;
        String[] attribute = new String[2];
        try {
             xmlFile = new File("entity.xml");
        }
        catch(Exception e) {
            attribute[0] = "File Exception Thrown!";
            return attribute;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = factory.newDocumentBuilder();

        Document doc = dBuilder.parse(xmlFile);

        doc.getDocumentElement().normalize();
        Element rootElement = doc.getDocumentElement();
        NodeList nList = rootElement.getElementsByTagName("attribute");

        for (int j = 0; j < nList.getLength(); j++){
            Node nNode = nList.item(j);
            nList.getLength();
            domain = nNode.getAttributes().getNamedItem("domain").getNodeValue();
            name = nNode.getAttributes().getNamedItem("constant").getNodeValue();
            if(name.toLowerCase().equals(user.toLowerCase())){
                try{
                    type = nNode.getAttributes().getNamedItem("type").getNodeValue();
                    attribute[0] = domain;
                    attribute[1] = type;
                    return attribute;
                }
                catch(Exception e){
                    attribute[0] = domain;
                    return attribute;
                }
            }
        }
        attribute[0] = "User Not Found!";
        return attribute;
    }

    public static String findAccess(String[] role, String entity, String accessLevel) throws ParserConfigurationException, SAXException, IOException{
        File xmlFile;
        String searchRole = "", searchAccessLevel = "", searchDomain = "", searchType = "";
        
        try {
             xmlFile = new File("policy.xml");
        }
        catch(Exception e) {
            return "file not found";
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = factory.newDocumentBuilder();

        Document doc = dBuilder.parse(xmlFile);

        doc.getDocumentElement().normalize();
        Element rootElement = doc.getDocumentElement();
        NodeList nList = rootElement.getElementsByTagName("attribute");

        for (int j = 0; j < nList.getLength(); j++){
            Node nNode = nList.item(j);
            nList.getLength();
            searchDomain = nNode.getAttributes().getNamedItem("domain").getNodeValue();
            searchRole = nNode.getAttributes().getNamedItem("name").getNodeValue();
            searchAccessLevel = nNode.getAttributes().getNamedItem("accessLevel").getNodeValue();

            if(searchDomain.toLowerCase().equals(entity.toLowerCase()) && searchRole.toLowerCase().equals(role[0].toLowerCase()) && searchAccessLevel.toLowerCase().equals(accessLevel.toLowerCase())){
                try{
                    searchType = nNode.getAttributes().getNamedItem("type").getNodeValue();
                    if(role[1] != null && searchType.toLowerCase().equals(role[1].toLowerCase())){
                        return "y";
                    }
                    else{
                        return "n";
                    }
                }
                catch(Exception e){
                    return "y";
                }
            }
        }


        return "n";
    }

    public static String findType(String user, String role) throws ParserConfigurationException, SAXException, IOException {
        File xmlFile;
        String name, domain, type;

        try {
             xmlFile = new File("entity.xml");
        }
        catch(Exception e) {
            return "File not Found exception!";
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = factory.newDocumentBuilder();
            
        Document doc = dBuilder.parse(xmlFile);

        doc.getDocumentElement().normalize();
        Element rootElement = doc.getDocumentElement();
        NodeList nList = rootElement.getElementsByTagName("attribute");

        for (int j = 0; j < nList.getLength(); j++){
            Node nNode = nList.item(j);
            nList.getLength();
            name = nNode.getAttributes().getNamedItem("constant").getNodeValue();
            domain = nNode.getAttributes().getNamedItem("domain").getNodeValue();

            if(name.toLowerCase().equals(user.toLowerCase())){
                try{
                    type = nNode.getAttributes().getNamedItem("type").getNodeValue();
                    return type;
                }
                catch(Exception e){

                }
            }
        }
        
        return "User Not Found!";
    }

    public static void runTest() throws ParserConfigurationException, SAXException, IOException {
        String test1, test2, test3, test4, test5, test6;
        long startTime, endTime;
        long[] timesTestOne = new long[20], timesTestTwo = new long[20], timesTestThree = new long[20], timesTestFour = new long[20], timesTestFive = new long[20], timesTestSix = new long[20];
        long avgOne = 0, avgTwo = 0, avgThree = 0, avgFour = 0, avgFive = 0, avgSix = 0;
        
        test1 = "Can [Gordon Ramsey] access [menu] with [write] permission";    //Tests direct policy access 
        test2 = "Can [Gordon Ramsey] access [menu] with [read] permission";     //Tests attribute delegation
        test3 = "Can [Fettuccine Barilla] access [pasta dough] with [write] permission"; //Tests parameterized role
        test4 = "Is [Oskar] a member of domain [owner]";    //Test getting is a member of
        test5 = "Is [Pillsbury Doughboy] a [pastry] chef";  //Test getting parameter
        test6 = "Can [Christina Wilson] access [menu] with [read] permission";
        

        for(int i = 0; i < 20; i++){
            startTime = System.nanoTime();
            accessControl(test1, 1, 1);
            endTime = System.nanoTime();
            timesTestOne[i] = (endTime - startTime);

            startTime = System.nanoTime();
            accessControl(test2, 1, 1);
            endTime = System.nanoTime();
            timesTestTwo[i] = (endTime - startTime);

            startTime = System.nanoTime();
            accessControl(test3, 1, 1);
            endTime = System.nanoTime();
            timesTestThree[i] = (endTime - startTime);

            startTime = System.nanoTime();
            accessControl(test4, 2, 1);
            endTime = System.nanoTime();
            timesTestFour[i] = (endTime - startTime);

            startTime = System.nanoTime();
            accessControl(test5, 3, 1);
            endTime = System.nanoTime();
            timesTestFive[i] = (endTime - startTime);

            startTime = System.nanoTime();
            accessControl(test6, 1, 1);
            endTime = System.nanoTime();
            timesTestSix[i] = (endTime - startTime);
        }

        for(int i = 0; i < 20; i++){
            avgOne += timesTestOne[i];
            avgTwo += timesTestTwo[i];
            avgThree += timesTestThree[i];
            avgFour += timesTestFour[i];
            avgFive += timesTestFive[i];
            avgSix += timesTestSix[i];
        }

        avgOne /= 20;
        avgTwo /= 20;
        avgThree /= 20;
        avgFour /= 20;
        avgFive /= 20;
        avgSix /= 20;

        System.out.println("Average Time of Test One: " + avgOne / 1000000);
        System.out.println("Average Time of Test Two: " + avgTwo / 1000000);
        System.out.println("Average Time of Test Three: " + avgThree / 1000000);
        System.out.println("Average Time of Test Four: " + avgFour / 1000000);
        System.out.println("Average Time of Test Five: " + avgFive / 1000000);
        System.out.println("Average Time of Test Six: " + avgSix / 1000000);
    }

}