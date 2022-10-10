import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class AptTracker {

    public static void main(String[] args) throws IOException {
        int userInput = 0;
        String streetAddress, apartmentNumber, city, zipCode, apartmentHash;
        int monthlyCost, squareFootage;
        Apartments addApartment;
        Scanner input = new Scanner(System.in);
        PriorityQueue PQ = new PriorityQueue(32);

        //Read in data from apartments text file
        Scanner fileReader = new Scanner(new File("apartments.txt"));
        fileReader.useDelimiter(":|\n");
        fileReader.nextLine();
        while (fileReader.hasNext()) {
            streetAddress = fileReader.next();
            apartmentNumber = fileReader.next();
            city = fileReader.next();
            zipCode = fileReader.next();
            monthlyCost = fileReader.nextInt();
            squareFootage = fileReader.nextInt();
            addApartment = new Apartments(streetAddress, apartmentNumber, city, zipCode, monthlyCost, squareFootage);
            //Add apartment to the PQs
            PQ.insert(addApartment);
        }

        while (userInput != 8) {
            //Present the user with options
            System.out.println("Choose an Operation:\n");
            System.out.println("1. Add an Apartment");
            System.out.println("2. Update an Apartment");
            System.out.println("3. Remove a Specific Apartment");
            System.out.println("4. Retrieve the Lowest Rent Apartment");
            System.out.println("5. Retrieve the Highest Square Footage Apartment");
            System.out.println("6. Retrieve the Lowest Rent Apartment by City");
            System.out.println("7. Retrieve the Highest Square Footage Apartment by City");
            System.out.println("8. Exit Apartment Search");
            System.out.print("\nEnter Choice: ");
            userInput = input.nextInt();

            //Ensure user enters a value corresponding to options
            while (userInput <= 0 || userInput > 8) {
                System.out.println("Value does not correspond to an option!");
                System.out.print("Reenter input: ");
                userInput = input.nextInt();
            }

            //Handle what input the user entered and the proceeding operation
            switch (userInput) {
                case 1:
                    //Add an apartment
                    input.nextLine();
                    System.out.print("\nEnter the Street Address: ");
                    streetAddress = input.nextLine();
                    System.out.print("Enter the Apartment Number: ");
                    apartmentNumber = input.nextLine();
                    System.out.print("Enter the City: ");
                    city = input.nextLine();
                    System.out.print("Enter the Zip Code: ");
                    zipCode = input.nextLine();
                    System.out.print("Enter the Monthly Rent: ");
                    monthlyCost = input.nextInt();
                    System.out.print("Enter the Square Footage: ");
                    squareFootage = input.nextInt();
                    Apartments newApartment = new Apartments(streetAddress, apartmentNumber, city, zipCode, monthlyCost, squareFootage);
                    PQ.insert(newApartment);
                    break;
                case 2:
                    //Retrieve apartment, then update the rent
                    input.nextLine();
                    System.out.print("\nEnter the Street Address: ");
                    streetAddress = input.nextLine();
                    System.out.print("Enter the Apartment Number: ");
                    apartmentNumber = input.nextLine();
                    System.out.print("Enter the Zip Code: ");
                    zipCode = input.nextLine();
                    apartmentHash = streetAddress + ":" + apartmentNumber + ":" + zipCode;

                    System.out.print("Would you like to update the rent for the apartment? (Enter 1 for Yes, 0 for No): ");
                    int updateRent = input.nextInt();
                    if (updateRent == 0) {
                        System.out.println("\nCancelling Rent Update!\n");
                        break;
                    }

                    System.out.print("Enter the New Monthly Cost: ");
                    int newMonthlyCost = input.nextInt();
                    try {
                        PQ.updateRent(apartmentHash, newMonthlyCost);
                        System.out.println("\nThe Rent has been Updated!\n");
                    } catch (NullPointerException e) {
                        System.out.println("\nWe Could Not Find that Apartment to Update the Rent!\n");
                    }
                    break;
                case 3:
                    //Remove selected apartment from the PQ
                    input.nextLine();
                    System.out.print("\nEnter the Street Address: ");
                    streetAddress = input.nextLine();
                    System.out.print("Enter the Apartment Number: ");
                    apartmentNumber = input.nextLine();
                    System.out.print("Enter the Zip Code: ");
                    zipCode = input.nextLine();
                    apartmentHash = streetAddress + ":" + apartmentNumber + ":" + zipCode;
                    try {
                        PQ.delete(apartmentHash);
                        System.out.println("\nThat Apartment has been Removed!\n");
                    } catch (NullPointerException e) {
                        System.out.println("\nNo Apartment was Found at that Address!\n");
                    }
                    break;
                case 4:
                    //Retrieve Lowest Rent Apartment
                    System.out.println("\nThe Lowest Rent Apartment Is: " + PQ.minApartmentCost() + "\n");
                    break;
                case 5:
                    //Retrieve Highest Square Footage Apartment
                    System.out.println("\nThe Highest Square Footage Apartment Is: " + PQ.maxApartmentSquareFootage() + "\n");
                    break;
                case 6:
                    //Retrieve lowest rent apartment within city
                    input.nextLine();
                    System.out.print("Enter the City: ");
                    city = input.nextLine();
                    Apartments search = PQ.minApartmentCostCity(city);
                    if (search == null) {
                        System.out.println("\nNo Apartments were Found within " + city + "\n");
                        break;
                    }
                    System.out.print("\nThe Lowest Rent Apartment in " + city + " Is: " + search.toString() + "\n\n");
                    break;
                case 7:
                    //Retrieve highest square footage apartment within city
                    input.nextLine();
                    System.out.print("Enter the City: ");
                    city = input.nextLine();
                    search = PQ.maxApartmentSquareFootageCity(city);
                    if (search == null) {
                        System.out.println("\nNo Apartments were Found within " + city + "\n");
                        break;
                    }
                    System.out.print("\nThe Highest Square Footage Apartment in " + city + " Is: " + search.toString() + "\n\n");
                    break;
            }
        }
    }
}
