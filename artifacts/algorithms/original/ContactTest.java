import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ContactTest {

    @Test
    void ctor_valid() {
        Contact c = new Contact("A123", "Alice", "Lee", "2105551234", "123 Main St");
        assertEquals("A123", c.getContactId());
        assertEquals("Alice", c.getFirstName());
        assertEquals("Lee", c.getLastName());
        assertEquals("2105551234", c.getPhone());
        assertEquals("123 Main St", c.getAddress());
    }

    @Test
    void id_null_or_too_long() {
        assertThrows(IllegalArgumentException.class, () -> new Contact(null, "A", "B", "2105551234", "addr"));
        assertThrows(IllegalArgumentException.class, () -> new Contact("01234567890", "A", "B", "2105551234", "addr")); // 11 chars
    }

    @Test
    void firstName_rules() {
        Contact c = new Contact("ID1", "Amy", "Bee", "2105551234", "addr");
        assertThrows(IllegalArgumentException.class, () -> c.setFirstName(null));
        assertThrows(IllegalArgumentException.class, () -> c.setFirstName("ABCDEFGHIJK")); // 11
    }

    @Test
    void lastName_rules() {
        Contact c = new Contact("ID1", "Amy", "Bee", "2105551234", "addr");
        assertThrows(IllegalArgumentException.class, () -> c.setLastName(null));
        assertThrows(IllegalArgumentException.class, () -> c.setLastName("ABCDEFGHIJK")); // 11
    }

    @Test
    void phone_rules() {
        Contact c = new Contact("ID1", "Amy", "Bee", "2105551234", "addr");
        assertThrows(IllegalArgumentException.class, () -> c.setPhone(null));
        assertThrows(IllegalArgumentException.class, () -> c.setPhone("123456789"));    // 9
        assertThrows(IllegalArgumentException.class, () -> c.setPhone("12345678901"));  // 11
        assertThrows(IllegalArgumentException.class, () -> c.setPhone("12345abcde"));   // not all digits
    }

    @Test
    void address_rules() {
        Contact c = new Contact("ID1", "Amy", "Bee", "2105551234", "addr");
        assertThrows(IllegalArgumentException.class, () -> c.setAddress(null));
        assertThrows(IllegalArgumentException.class, () -> c.setAddress("1234567890123456789012345678901")); // 31
    }
}
