import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactService {

    private final Map<String, Contact> contacts = new HashMap<>();

    public Map<String, Contact> getAll() {
        return Collections.unmodifiableMap(contacts);
    }

    // Add contact with unique ID
    public void addContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact cannot be null");
        }

        String id = contact.getContactId();

        if (contacts.containsKey(id)) {
            throw new IllegalArgumentException(
                    "contactId already exists: " + id);
        }

        contacts.put(id, contact);
    }

    // Delete contact by ID
    public void deleteContact(String contactId) {
        if (contactId == null || !contacts.containsKey(contactId)) {
            throw new IllegalArgumentException("contactId not found");
        }

        contacts.remove(contactId);
    }

    // Update first name
    public void updateFirstName(String contactId, String firstName) {
        Contact c = getExisting(contactId);
        c.setFirstName(firstName);
    }

    // Update last name
    public void updateLastName(String contactId, String lastName) {
        Contact c = getExisting(contactId);
        c.setLastName(lastName);
    }

    // Update phone number
    public void updatePhone(String contactId, String phone) {
        Contact c = getExisting(contactId);
        c.setPhone(phone);
    }

    // Update address
    public void updateAddress(String contactId, String address) {
        Contact c = getExisting(contactId);
        c.setAddress(address);
    }

    // Search contacts by first or last name
    // Results are sorted alphabetically by last name,
    // then by first name.
    public List<Contact> searchByName(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "search name cannot be null or empty");
        }

        String searchName = name.trim().toLowerCase();
        List<Contact> matches = new ArrayList<>();

        for (Contact contact : contacts.values()) {

            if (contact.getFirstName()
                    .toLowerCase()
                    .contains(searchName)
                    ||
                contact.getLastName()
                    .toLowerCase()
                    .contains(searchName)) {

                matches.add(contact);
            }
        }

        matches.sort(
            Comparator.comparing(
                    Contact::getLastName,
                    String.CASE_INSENSITIVE_ORDER)
                .thenComparing(
                    Contact::getFirstName,
                    String.CASE_INSENSITIVE_ORDER)
        );

        return matches;
    }

    // Find an existing contact by ID
    private Contact getExisting(String contactId) {

        if (contactId == null) {
            throw new IllegalArgumentException(
                    "contactId cannot be null");
        }

        Contact c = contacts.get(contactId);

        if (c == null) {
            throw new IllegalArgumentException(
                    "contactId not found: " + contactId);
        }

        return c;
    }
}