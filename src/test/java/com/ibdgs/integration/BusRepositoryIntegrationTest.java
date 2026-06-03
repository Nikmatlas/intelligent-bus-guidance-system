package com.ibdgs.integration;

import com.ibdgs.model.Bus;
import com.ibdgs.model.Driver;
import com.ibdgs.repository.BusRepository;
import com.ibdgs.service.BusValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Task 4 - Bus integration tests.
 *
 * <p>Every test uses a real JSON file and the real {@link BusRepository} (no
 * mocks/stubs), verifying the four required behaviours: valid buses are stored,
 * invalid buses are rejected, updates are persisted, and record counts stay
 * correct. Two extra tests confirm reload-from-disk and an end-to-end driver/bus
 * compatibility check. 6 integration test cases in total (requirement: at least 4).</p>
 */
@DisplayName("Bus integration tests")
class BusRepositoryIntegrationTest {

    // (1) valid buses are stored correctly
    @Test
    @DisplayName("valid buses are stored correctly and are retrievable")
    void validBusStored(@TempDir Path dir) {
        BusRepository repo = new BusRepository(dir.resolve("buses.json"));
        repo.add(new Bus("12345678", 50, 80.0, "Diesel"));

        Bus loaded = repo.retrieve("12345678").orElseThrow();
        assertEquals(50, loaded.getCapacity());
        assertEquals("Diesel", loaded.getFuelType());
        assertEquals(80.0, loaded.getFuelLevel());
        assertEquals(1, repo.count());
    }

    // (2) invalid buses are rejected
    @Test
    @DisplayName("invalid buses are rejected and not stored")
    void invalidBusRejected(@TempDir Path dir) {
        BusRepository repo = new BusRepository(dir.resolve("buses.json"));

        assertThrows(IllegalArgumentException.class,
                () -> repo.add(new Bus("ABC12345", 50, 80.0, "Diesel"))); // B1 non-digit
        assertThrows(IllegalArgumentException.class,
                () -> repo.add(new Bus("1234", 50, 80.0, "Diesel")));      // B1 short
        assertThrows(IllegalArgumentException.class,
                () -> repo.add(new Bus("12345678", 50, 80.0, "Petrol")));  // bad fuel type

        assertEquals(0, repo.count());
    }

    // (3) updates are persisted correctly
    @Test
    @DisplayName("updates are persisted correctly (B2 decrease) and reloadable")
    void updatePersisted(@TempDir Path dir) {
        Path file = dir.resolve("buses.json");
        BusRepository repo = new BusRepository(file);
        repo.add(new Bus("12345678", 60, 80.0, "Diesel"));

        repo.update(new Bus("12345678", 45, 55.5, "Hybrid"));

        // Reload from disk with a fresh instance to prove persistence.
        BusRepository reloaded = new BusRepository(file);
        Bus updated = reloaded.retrieve("12345678").orElseThrow();
        assertEquals(45, updated.getCapacity());
        assertEquals(55.5, updated.getFuelLevel());
        assertEquals("Hybrid", updated.getFuelType());
    }

    // (4) record counts are updated correctly
    @Test
    @DisplayName("record counts are updated correctly | add two valid buses -> count 2")
    void countsUpdated(@TempDir Path dir) {
        BusRepository repo = new BusRepository(dir.resolve("buses.json"));
        assertEquals(0, repo.count());

        repo.add(new Bus("11111111", 30, 50.0, "Diesel"));
        repo.add(new Bus("22222222", 40, 60.0, "Hybrid"));
        assertEquals(2, repo.count());

        // A rejected duplicate must NOT change the count.
        assertThrows(IllegalArgumentException.class,
                () -> repo.add(new Bus("11111111", 25, 10.0, "Diesel")));
        assertEquals(2, repo.count());
    }

    // Extra: B2 increase rejected after reload from disk.
    @Test
    @DisplayName("B2 increase is rejected after reloading from disk")
    void b2AfterReload(@TempDir Path dir) {
        Path file = dir.resolve("buses.json");
        BusRepository repo = new BusRepository(file);
        repo.add(new Bus("12345678", 50, 80.0, "Diesel"));

        BusRepository reloaded = new BusRepository(file);
        assertEquals(1, reloaded.count());
        assertThrows(IllegalArgumentException.class,
                () -> reloaded.update(new Bus("12345678", 70, 80.0, "Diesel")));
        assertEquals(50, reloaded.retrieve("12345678").orElseThrow().getCapacity());
    }

    // Extra: end-to-end driver + bus stored in a real file, then compatibility check (B3-B5).
    @Test
    @DisplayName("end-to-end: stored bus is human-readable and checked for compatibility")
    void driverBusAssignment(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("buses.json");
        BusRepository busRepo = new BusRepository(file);
        Bus electric = new Bus("87654321", 40, 90.0, "Electricity");
        busRepo.add(electric);

        // File on disk is human-readable JSON.
        String busFile = Files.readString(file);
        assertTrue(busFile.contains("\"busID\""));
        assertTrue(busFile.contains("87654321"));

        Driver d = new Driver("56@#abXyAB", "Eve", 6, "PublicTransport",
                "12|Main St|Melbourne|VIC|Australia", "01-01-2000");
        Bus storedBus = busRepo.retrieve("87654321").orElseThrow();

        LocalDate ref = LocalDate.of(2025, 1, 1);
        // 6 years experience + PublicTransport licence + age 25 -> allowed (B3,B4,B5).
        assertTrue(BusValidator.canDriverOperate(d, storedBus, ref));
    }
}
