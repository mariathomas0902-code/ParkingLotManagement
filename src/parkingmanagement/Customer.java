package parkingmanagement;

public class Customer {
    private int customerId;
    private String name;
    private String phoneNumber;
    private String email;

    public Customer(int customerId, String name,
                    String phoneNumber, String email) {

        this.customerId = customerId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Customer ID: " + customerId
                + ", Name: " + name
                + ", Phone: " + phoneNumber
                + ", Email: " + email;
    }
}
