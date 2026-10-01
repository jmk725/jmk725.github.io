import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ContactServiceTest {

    @Test
    void add_delete_update_flow() {
        ContactService svc = new ContactService();
        Contact c1 = new Contact(
                "ID100", "Max", "Kim",
                "5125550000", "1 North St");

        svc.addContact(c1);
        assertTrue(svc.getAll().containsKey("ID100"));

        // Unique ID enforcement
        assertThrows(
                IllegalArgumentException.class,
                () -> svc.addContact(
                        new Contact(
                                "ID100", "Ana", "Q",
                                "5125551111", "X")));

        // Update fields
        svc.updateFirstName("ID100", "Maxine");
        svc.updateLastName("ID100", "K.");
        svc.updatePhone("ID100", "5125552222");
        svc.updateAddress("ID100", "2 South St");

        assertEquals(
                "Maxine",
                svc.getAll().get("ID100").getFirstName());

        assertEquals(
                "K.",
                svc.getAll().get("ID100").getLastName());

        assertEquals(
                "5125552222",
                svc.getAll().get("ID100").getPhone());

        assertEquals(
                "2 South St",
                svc.getAll().get("ID100").getAddress());

        // Delete
        svc.deleteContact("ID100");
        assertFalse(svc.getAll().containsKey("ID100"));
    }

    @Test
    void update_invalid_inputs_throw() {
        ContactService svc = new ContactService();

        svc.addContact(
                new Contact(
                        "ID1", "Ava", "J",
                        "2105559999", "Addr"));

        assertThrows(
                IllegalArgumentException.class,
                () -> svc.updateFirstName("ID1", null));

        assertThrows(
                IllegalArgumentException.class,
                () -> svc.updateLastName(
                        "ID1", "ABCDEFGHIJK"));

        assertThrows(
                IllegalArgumentException.class,
                () -> svc.updatePhone("ID1", "1234"));

        assertThrows(
                IllegalArgumentException.class,
                () -> svc.updateAddress("ID1", null));
    }

    @Test
    void delete_or_update_missing_id_throws() {
        ContactService svc = new ContactService();

        assertThrows(
                IllegalArgumentException.class,
                () -> svc.deleteContact("NOPE"));

        assertThrows(
                IllegalArgumentException.class,
                () -> svc.updateFirstName("NOPE", "X"));
    }

    // NEW ENHANCEMENT TESTS

    @Test
    void search_by_name_finds_first_and_last_names() {
        ContactService svc = new ContactService();

        svc.addContact(
                new Contact(
                        "ID1", "Jean", "Karst",
                        "2105551111", "1 Main St"));

        svc.addContact(
                new Contact(
                        "ID2", "Maria", "Jean",
                        "2105552222", "2 Main St"));

        svc.addContact(
                new Contact(
                        "ID3", "Robert", "Smith",
                        "2105553333", "3 Main St"));

        List<Contact> results = svc.searchByName("Jean");

        assertEquals(2, results.size());
    }

    @Test
    void search_by_name_is_case_insensitive() {
        ContactService svc = new ContactService();

        svc.addContact(
                new Contact(
                        "ID1", "Jean", "Karst",
                        "2105551111", "1 Main St"));

        List<Contact> results = svc.searchByName("jEaN");

        assertEquals(1, results.size());
        assertEquals(
                "Jean",
                results.get(0).getFirstName());
    }

    @Test
    void search_by_name_no_match_returns_empty_list() {
        ContactService svc = new ContactService();

        svc.addContact(
                new Contact(
                        "ID1", "Jean", "Karst",
                        "2105551111", "1 Main St"));

        List<Contact> results =
                svc.searchByName("Williams");

        assertTrue(results.isEmpty());
    }

    @Test
    void search_results_are_sorted_by_last_name() {
        ContactService svc = new ContactService();

        svc.addContact(
                new Contact(
                        "ID1", "Amy", "Smith",
                        "2105551111", "1 Main St"));

        svc.addContact(
                new Contact(
                        "ID2", "Amy", "Anderson",
                        "2105552222", "2 Main St"));

        svc.addContact(
                new Contact(
                        "ID3", "Amy", "Brown",
                        "2105553333", "3 Main St"));

        List<Contact> results =
                svc.searchByName("Amy");

        assertEquals("Anderson",
                results.get(0).getLastName());

        assertEquals("Brown",
                results.get(1).getLastName());

        assertEquals("Smith",
                results.get(2).getLastName());
    }

    @Test
    void duplicate_last_names_are_sorted_by_first_name() {
        ContactService svc = new ContactService();

        svc.addContact(
                new Contact(
                        "ID1", "Zoe", "Smith",
                        "2105551111", "1 Main St"));

        svc.addContact(
                new Contact(
                        "ID2", "Amy", "Smith",
                        "2105552222", "2 Main St"));

        svc.addContact(
                new Contact(
                        "ID3", "Michael", "Smith",
                        "2105553333", "3 Main St"));

        List<Contact> results =
                svc.searchByName("Smith");

        assertEquals("Amy",
                results.get(0).getFirstName());

        assertEquals("Michael",
                results.get(1).getFirstName());

        assertEquals("Zoe",
                results.get(2).getFirstName());
    }

    @Test
    void empty_or_null_search_throws() {
        ContactService svc = new ContactService();

        assertThrows(
                IllegalArgumentException.class,
                () -> svc.searchByName(""));

        assertThrows(
                IllegalArgumentException.class,
                () -> svc.searchByName("   "));

        assertThrows(
                IllegalArgumentException.class,
                () -> svc.searchByName(null));
    }
}