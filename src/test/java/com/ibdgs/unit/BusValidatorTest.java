package com.ibdgs.unit;

import com.ibdgs.model.Bus;
import com.ibdgs.model.Driver;
import com.ibdgs.repository.BusRepository;
import com.ibdgs.service.BusValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Task 2 - Bus unit tests for conditions B1-B5.
 *
 * <p>The Test Data values below match the team's "1.2 Bus Unit Test Cases" table
 * exactly, so each documented row maps to one test here. 17 tests total
 * (>= 3 per condition). Age is computed from a birthdate against a fixed
 * reference date (2025-01-01); the comments show the resulting age.</p>
 */
@DisplayName("Bus unit tests (B1-B5)")
class BusValidatorTest {

    /** Fixed "today" used for age calculations in B3 tests. */
    private static final LocalDate REF = LocalDate.of(2025, 1, 1);

    /** Build a driver; birthdate controls age, the other fields control B4/B5. */
    private static Driver driver(int experienceYears, String licence, String birthdate) {
        return new Driver("23@#abXyAB", "Drv", experienceYears, licence,
                "12|Main St|Melbourne|VIC|Australia", birthdate);
    }

    // ============================================================
    // B1 - Bus ID rules (5 cases)
    // ============================================================
    @Nested
    @DisplayName("B1 - busID rules")
    class B1 {
        @Test
        @DisplayName("B1: Valid bus ID is accepted | 12345678")
        void validIdAccepted() {
            assertTrue(BusValidator.isValidBusId("12345678"));
        }

        @Test
        @DisplayName("B1: Duplicate bus ID is rejected | same ID used twice")
        void duplicateRejected(@TempDir Path dir) {
            BusRepository repo = new BusRepository(dir.resolve("buses.json"));
            repo.add(new Bus("12345678", 40, 80.0, "Diesel"));
            assertThrows(IllegalArgumentException.class,
                    () -> repo.add(new Bus("12345678", 30, 50.0, "Diesel")));
        }

        @Test
        @DisplayName("B1: Bus ID with letters is rejected | 1234ABCD")
        void lettersRejected() {
            assertFalse(BusValidator.isValidBusId("1234ABCD"));
        }

        @Test
        @DisplayName("B1: Bus ID shorter than 8 digits is rejected | 1234567")
        void shorterRejected() {
            assertFalse(BusValidator.isValidBusId("1234567"));
        }

        @Test
        @DisplayName("B1: Bus ID longer than 8 digits is rejected | 123456789")
        void longerRejected() {
            assertFalse(BusValidator.isValidBusId("123456789"));
        }
    }

    // ============================================================
    // B2 - Capacity update restriction (3 cases)
    // ============================================================
    @Nested
    @DisplayName("B2 - capacity update restriction")
    class B2 {
        @Test
        @DisplayName("B2: Capacity decrease is allowed | old 60, new 50")
        void decreaseAllowed() {
            assertTrue(BusValidator.isValidCapacityUpdate(60, 50));
        }

        @Test
        @DisplayName("B2: Capacity stays the same is allowed | old 50, new 50")
        void equalAllowed() {
            assertTrue(BusValidator.isValidCapacityUpdate(50, 50));
        }

        @Test
        @DisplayName("B2: Capacity increase is rejected | old 40, new 50")
        void increaseRejected() {
            assertFalse(BusValidator.isValidCapacityUpdate(40, 50));
        }
    }

    // ============================================================
    // B3 - Driver age restriction (3 cases) - buses are diesel so only age/capacity matter
    // ============================================================
    @Nested
    @DisplayName("B3 - driver age restriction")
    class B3 {
        @Test
        @DisplayName("B3: Driver aged 50 can drive bus capacity 50 | age 50, capacity 50")
        void age50Capacity50Allowed() {
            Driver d = driver(10, "Heavy", "01-01-1975"); // age 50 at REF (not > 50)
            Bus bus = new Bus("11111111", 50, 50.0, "Diesel");
            assertTrue(BusValidator.canDriverOperate(d, bus, REF));
        }

        @Test
        @DisplayName("B3: Driver older than 50 cannot drive capacity 50+ | age 51, capacity 50")
        void age51Capacity50Rejected() {
            Driver d = driver(10, "Heavy", "01-01-1974"); // age 51 at REF
            Bus bus = new Bus("22222222", 50, 50.0, "Diesel");
            assertFalse(BusValidator.canDriverOperate(d, bus, REF));
        }

        @Test
        @DisplayName("B3: Driver older than 50 can drive below capacity 50 | age 55, capacity 40")
        void age55Capacity40Allowed() {
            Driver d = driver(10, "Heavy", "01-01-1970"); // age 55 at REF
            Bus bus = new Bus("33333333", 40, 50.0, "Diesel");
            assertTrue(BusValidator.canDriverOperate(d, bus, REF));
        }
    }

    // ============================================================
    // B4 - Electric bus experience restriction (3 cases)
    // Drivers hold Heavy licence so B5 does not interfere; age young so B3 does not interfere.
    // ============================================================
    @Nested
    @DisplayName("B4 - electric bus experience restriction")
    class B4 {
        @Test
        @DisplayName("B4: 5 years experience can drive electric bus | exp 5, Electricity")
        void exp5ElectricAllowed() {
            Driver d = driver(5, "Heavy", "01-01-2000"); // age 25
            Bus electric = new Bus("44444444", 30, 50.0, "Electricity");
            assertTrue(BusValidator.canDriverOperate(d, electric, REF));
        }

        @Test
        @DisplayName("B4: Less than 5 years experience cannot drive electric | exp 4, Electricity")
        void exp4ElectricRejected() {
            Driver d = driver(4, "Heavy", "01-01-2000"); // age 25
            Bus electric = new Bus("44444444", 30, 50.0, "Electricity");
            assertFalse(BusValidator.canDriverOperate(d, electric, REF));
        }

        @Test
        @DisplayName("B4: Experience restriction does not apply to diesel | exp 2, Diesel")
        void exp2DieselAllowed() {
            Driver d = driver(2, "Light", "01-01-2000"); // age 25, low experience
            Bus diesel = new Bus("55555555", 30, 50.0, "Diesel");
            assertTrue(BusValidator.canDriverOperate(d, diesel, REF));
        }
    }

    // ============================================================
    // B5 - Licence restriction for electric/hybrid (3 cases)
    // Drivers have enough experience/young age so only the licence rule decides.
    // ============================================================
    @Nested
    @DisplayName("B5 - licence restriction for electric/hybrid")
    class B5 {
        @Test
        @DisplayName("B5: Heavy licence can operate hybrid bus | Heavy, Hybrid")
        void heavyHybridAllowed() {
            Driver d = driver(10, "Heavy", "01-01-2000"); // age 25
            Bus hybrid = new Bus("66666666", 30, 50.0, "Hybrid");
            assertTrue(BusValidator.canDriverOperate(d, hybrid, REF));
        }

        @Test
        @DisplayName("B5: PublicTransport licence can operate electric bus | PublicTransport, Electricity")
        void publicTransportElectricAllowed() {
            Driver d = driver(10, "PublicTransport", "01-01-2000"); // age 25, exp 10
            Bus electric = new Bus("77777777", 30, 50.0, "Electricity");
            assertTrue(BusValidator.canDriverOperate(d, electric, REF));
        }

        @Test
        @DisplayName("B5: Light licence cannot operate electric bus | Light, Electricity")
        void lightElectricRejected() {
            Driver d = driver(10, "Light", "01-01-2000"); // age 25, exp 10 (only licence fails)
            Bus electric = new Bus("88888888", 30, 50.0, "Electricity");
            assertFalse(BusValidator.canDriverOperate(d, electric, REF));
        }

        @Test
        @DisplayName("B5: Medium licence cannot operate hybrid bus | Medium, Hybrid")
        void mediumHybridRejected() {
            Driver d = driver(10, "Medium", "01-01-2000"); // age 25, exp 10 (only licence fails)
            Bus hybrid = new Bus("99990000", 30, 50.0, "Hybrid");
            assertFalse(BusValidator.canDriverOperate(d, hybrid, REF));
        }
    }
}