public class Apartments {

    String streetAddress;
    String apartmentNumber;
    String city;
    String zipCode;
    int monthlyCost;
    int squareFootage;


    public Apartments(String streetAddress, String apartmentNumber, String city, String zipCode, int monthlyCost, int squareFootage) {
        this.streetAddress = streetAddress;
        this.apartmentNumber = apartmentNumber;
        this.city = city;
        this.zipCode = zipCode;
        this.monthlyCost = monthlyCost;
        this.squareFootage = squareFootage;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public int getMonthlyCost() {
        return monthlyCost;
    }

    public void setMonthlyCost(int monthlyCost) {
        this.monthlyCost = monthlyCost;
    }

    public int getSquareFootage() {
        return squareFootage;
    }

    public void setSquareFootage(int squareFootage) {
        this.squareFootage = squareFootage;
    }

    public String getHashString() {     //Sets a string that makes a street, apartment number, zip code combo unique for hash table
        StringBuilder hashString = new StringBuilder("");
        hashString.append(streetAddress + ":" + apartmentNumber + ":" + zipCode);
        return hashString.toString();
    }

    public String toString() {
        StringBuilder apartments = new StringBuilder("");
        apartments.append("\nStreet Address: " + streetAddress);
        apartments.append("\nApartment Number: " + apartmentNumber);
        apartments.append("\nCity: " + city);
        apartments.append("\nZip Code: " + zipCode);
        apartments.append("\nMonthly Cost: " + monthlyCost);
        apartments.append("\nSquare Footage: " + squareFootage);
        return apartments.toString();
    }
}
