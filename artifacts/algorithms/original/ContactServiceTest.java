import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ContactServiceTest {

    @Test
    void add_delete_update_flow() {
        ContactService svc = new ContactService();
        Contact c1 = new Contact("ID100", "Max", "Kim", "5125550000", "1 North St");
        svc.addContact(c1);
        assertTrue(svc.getAll().containsKey("ID100"));

        // Unique ID enforcement
        assertThrows(IllegalArgumentException.class, () -> svc.addContact(new Contact("ID100", "Ana", "Q", "5125551111", "X")));

        // Update fields
        svc.updateFirstName("ID100", "Maxine");
        svc.updateLastName("ID100", "K.");
        svc.updatePhone("ID100", "5125552222");
        svc.updateAddress("ID100", "2 South St");

        assertEquals("Maxine", svc.getAll().get("ID100").getFirstName());
        assertEquals("K.", svc.getAll().get("ID100").getLastName());
        assertEquals("5125552222", svc.getAll().get("ID100").getPhone());
        assertEquals("2 South St", svc.getAll().get("ID100").getAddress());

        // Delete
        svc.deleteContact("ID100");
        assertFalse(svc.getAll().containsKey("ID100"));
    }

    @Test
    void update_invalid_inputs_throw() {
        ContactService svc = new ContactService();
        svc.addContact(new Contact("ID1", "Ava", "J", "2105559999", "Addr"));
        assertThrows(IllegalArgumentException.class, () -> svc.updateFirstName("ID1", null));
        assertThrows(IllegalArgumentException.class, () -> svc.updateLastName("ID1", "ABCDEFGHIJK")); // 11
        assertThrows(IllegalArgumentException.class, () -> svc.updatePhone("ID1", "1234"));
        assertThrows(IllegalArgumentException.class, () -> svc.updateAddress("ID1", null));
    }

    @Test
    void delete_or_update_missing_id_throws() {
        ContactService svc = new ContactService();
        assertThrows(IllegalArgumentException.class, () -> svc.deleteContact("NOPE"));
        assertThrows(IllegalArgumentException.class, () -> svc.updateFirstName("NOPE", "X"));
    }
}
