import java.util.Collections;
import java.util.HashMap;
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
            throw new IllegalArgumentException("contactId already exists: " + id);
        }
        contacts.put(id, contact);
    }

    // Delete by ID
    public void deleteContact(String contactId) {
        if (contactId == null || !contacts.containsKey(contactId)) {
            throw new IllegalArgumentException("contactId not found");
        }
        contacts.remove(contactId);
    }

    // Update allowed fields per requirement
    public void updateFirstName(String contactId, String firstName) {
        Contact c = getExisting(contactId);
        c.setFirstName(firstName);
    }

    public void updateLastName(String contactId, String lastName) {
        Contact c = getExisting(contactId);
        c.setLastName(lastName);
    }

    public void updatePhone(String contactId, String phone) {
        Contact c = getExisting(contactId);
        c.setPhone(phone);
    }

    public void updateAddress(String contactId, String address) {
        Contact c = getExisting(contactId);
        c.setAddress(address);
    }

    private Contact getExisting(String contactId) {
        if (contactId == null) {
            throw new IllegalArgumentException("contactId cannot be null");
        }
        Contact c = contacts.get(contactId);
        if (c == null) {
            throw new IllegalArgumentException("contactId not found: " + contactId);
        }
        return c;
    }
}
