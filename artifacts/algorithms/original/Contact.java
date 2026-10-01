public class Contact {
    private final String contactId;
    private String firstName;
    private String lastName;
    private String phone;   // exactly 10 digits
    private String address; // <= 30 chars

    public Contact(String contactId, String firstName, String lastName, String phone, String address) {
        validateId(contactId);
        this.contactId = contactId;

        setFirstName(firstName);
        setLastName(lastName);
        setPhone(phone);
        setAddress(address);
    }

    // ----- Getters -----
    public String getContactId() { return contactId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }

    // ----- Setters with validation (ID is immutable) -----
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.length() > 10) {
            throw new IllegalArgumentException("firstName must be non-null and <= 10 chars");
        }
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.length() > 10) {
            throw new IllegalArgumentException("lastName must be non-null and <= 10 chars");
        }
        this.lastName = lastName;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.length() != 10 || !phone.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("phone must be exactly 10 digits");
        }
        this.phone = phone;
    }

    public void setAddress(String address) {
        if (address == null || address.length() > 30) {
            throw new IllegalArgumentException("address must be non-null and <= 30 chars");
        }
        this.address = address;
    }

    // ----- Validation helpers -----
    private static void validateId(String id) {
        if (id == null || id.length() > 10) {
            throw new IllegalArgumentException("contactId must be non-null and <= 10 chars");
        }
    }
}
