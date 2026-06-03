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
 * 20 test cases in total (>= 3 per condition), covering normal, invalid and
 * edge inputs. Pure rule logic is exercised directly through {@link BusValidator};
 * the B1 uniqueness check needs stored data, so it goes through a temporary
 * {@link BusRepository}. Age-dependent rules (B3) use a fixed reference date so
 * results are deterministic regardless of when the suite runs.
 */
@DisplayName("Bus unit tests (B1-B5)")
class BusValidatorTest {

    /** Fixed "today" used for age calculations in B3 tests. */
    private static final LocalDate REF = LocalDate.of(2025, 1, 1);

    /** Helper to build a driver with the fields B3-B5 care about. */
    private static Driver driver(int experienceYears, String licence, String birthdate) {
        return new Driver("23@#abXyAB", "Drv", experienceYears, licence,
                "12|Main St|Melbourne|VIC|Australia", birthdate);
    }

    // ============================================================
    // B1 - Bus ID rules (4 cases)
    // ============================================================
    @Nested
    @DisplayName("B1 - busID rules")
    class B1 {
        @Test
        @DisplayName("normal: a valid 8-digit ID is accepted")
        void valid() {
            assertTrue(BusValidator.isValidBusId("12345678"));
        }

        @Test
        @DisplayName("invalid: an ID containing a non-digit is rejected")
        void nonDigit() {
            assertFalse(BusValidator.isValidBusId("1234567A"));
        }

        @Test
        @DisplayName("invalid: an ID of the wrong length is rejected")
        void wrongLength() {
            assertFalse(BusValidator.isValidBusId("1234567"));   // 7 digits
            assertFalse(BusValidator.isValidBusId("123456789")); // 9 digits
        }

        @Test
        @DisplayName("edge: a duplicate busID is rejected on add")
        void duplicateRejected(@TempDir Path dir) {
            BusRepository repo = new BusRepository(dir.resolve("buses.json"));
            repo.add(new Bus("12345678", 40, 80.0, "Diesel"));
            assertThrows(IllegalArgumentException.class,
                    () -> repo.add(new Bus("12345678", 30, 50.0, "Diesel")));
        }
    }

    // ============================================================
    // B2 - Capacity update restriction (4 cases)
    // ============================================================
    @Nested
    @DisplayName("B2 - capacity update restriction")
    class B2 {
        @Test
        @DisplayName("normal: decreasing capacity is allowed")
        void decreaseAllowed() {
            assertTrue(BusValidator.isValidCapacityUpdate(50, 40));
        }

        @Test
        @DisplayName("edge: keeping capacity equal is allowed")
        void equalAllowed() {
            assertTrue(BusValidator.isValidCapacityUpdate(50, 50));
        }

        @Test
        @DisplayName("invalid: increasing capacity is rejected")
        void increaseRejected() {
            assertFalse(BusValidator.isValidCapacityUpdate(50, 60));
        }

        @Test
        @DisplayName("invalid: a non-positive new capacity is rejected")
        void nonPositiveRejected() {
            assertFalse(BusValidator.isValidCapacityUpdate(50, 0));
        }
    }

    // ============================================================
    // B3 - Driver age restriction (4 cases)
    // ============================================================
    @Nested
    @DisplayName("B3 - driver age restriction")
    class B3 {
        @Test
        @DisplayName("invalid: driver older than 50 cannot drive capacity >= 50")
        void oldDriverBigBus() {
            Driver old = driver(20, "Heavy", "01-01-1970"); // age 55
            Bus big = new Bus("11111111", 60, 50.0, "Diesel");
            assertFalse(BusValidator.canDriverOperate(old, big, REF));
        }

        @Test
        @DisplayName("normal: driver older than 50 may drive capacity < 50")
        void oldDriverSmallBus() {
            Driver old = driver(20, "Heavy", "01-01-1970"); // age 55
            Bus small = new Bus("22222222", 49, 50.0, "Diesel");
            assertTrue(BusValidator.canDriverOperate(old, small, REF));
        }

        @Test
        @DisplayName("edge: a driver of exactly 50 may drive capacity >= 50")
        void exactlyFiftyBigBus() {
            Driver fifty = driver(20, "Heavy", "01-01-1975"); // age 50, not > 50
            Bus big = new Bus("11111111", 60, 50.0, "Diesel");
            assertTrue(BusValidator.canDriverOperate(fifty, big, REF));
        }

        @Test
        @DisplayName("edge: capacity of exactly 50 triggers the rule for an old driver")
        void capacityExactlyFifty() {
            Driver old = driver(20, "Heavy", "01-01-1970"); // age 55
            Bus boundary = new Bus("33333333", 50, 50.0, "Diesel");
            assertFalse(BusValidator.canDriverOperate(old, boundary, REF));
        }
    }

    // ============================================================
    // B4 - Electric bus experience restriction (3 cases)
    // ============================================================
    @Nested
    @DisplayName("B4 - electric bus experience restriction")
    class B4 {
        @Test
        @DisplayName("invalid: fewer than 5 years experience cannot drive electric")
        void tooInexperienced() {
            Driver d = driver(4, "Heavy", "01-01-2000");
            Bus electric = new Bus("44444444", 30, 50.0, "Electricity");
            assertFalse(BusValidator.canDriverOperate(d, electric, REF));
        }

        @Test
        @DisplayName("edge: exactly 5 years experience may drive electric")
        void exactlyFiveYears() {
            Driver d = driver(5, "Heavy", "01-01-2000");
            Bus electric = new Bus("44444444", 30, 50.0, "Electricity");
            assertTrue(BusValidator.canDriverOperate(d, electric, REF));
        }

        @Test
        @DisplayName("normal: an experienced driver may drive electric")
        void experienced() {
            Driver d = driver(12, "PublicTransport", "01-01-2000");
            Bus electric = new Bus("44444444", 30, 50.0, "Electricity");
            assertTrue(BusValidator.canDriverOperate(d, electric, REF));
        }
    }

    // ============================================================
    // B5 - Licence restriction for electric/hybrid (5 cases)
    // ============================================================
    @Nested
    @DisplayName("B5 - licence restriction for electric/hybrid")
    class B5 {
        @Test
        @DisplayName("invalid: a Light licence cannot drive an electric bus")
        void lightElectric() {
            Driver d = driver(10, "Light", "01-01-2000");
            Bus electric = new Bus("44444444", 30, 50.0, "Electricity");
            assertFalse(BusValidator.canDriverOperate(d, electric, REF));
        }

        @Test
        @DisplayName("invalid: a Medium licence cannot drive a hybrid bus")
        void mediumHybrid() {
            Driver d = driver(10, "Medium", "01-01-2000");
            Bus hybrid = new Bus("55555555", 30, 50.0, "Hybrid");
            assertFalse(BusValidator.canDriverOperate(d, hybrid, REF));
        }

        @Test
        @DisplayName("normal: a Heavy licence may drive a hybrid bus")
        void heavyHybrid() {
            Driver d = driver(10, "Heavy", "01-01-2000");
            Bus hybrid = new Bus("55555555", 30, 50.0, "Hybrid");
            assertTrue(BusValidator.canDriverOperate(d, hybrid, REF));
        }

        @Test
        @DisplayName("normal: a PublicTransport licence may drive an electric bus")
        void publicTransportElectric() {
            Driver d = driver(10, "PublicTransport", "01-01-2000");
            Bus electric = new Bus("44444444", 30, 50.0, "Electricity");
            assertTrue(BusValidator.canDriverOperate(d, electric, REF));
        }

        @Test
        @DisplayName("edge: B5 does not apply to a diesel bus (Light licence is ok)")
        void lightDiesel() {
            Driver d = driver(10, "Light", "01-01-2000");
            Bus diesel = new Bus("66666666", 30, 50.0, "Diesel");
            assertTrue(BusValidator.canDriverOperate(d, diesel, REF));
        }
    }
}
