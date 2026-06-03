package com.ibdgs.service;

import com.ibdgs.model.Bus;
import com.ibdgs.model.Driver;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;

/**
 * Validation for all bus-related business rules (B1-B5).
 *  B1 - busID must be exactly 8 digits (uniqueness is enforced by
 *       {@code BusRepository} which has access to the stored data).
 *  B2- on update, capacity may decrease or stay equal but must not
 *       increase (see {@link #isValidCapacityUpdate(int, int)}).
 *   B3 - drivers older than 50 cannot drive buses with capacity age; 50.
 *  B4 - only drivers with &ge; 5 years experience can drive electric buses.
 * B5 - only Heavy / PublicTransport licence holders can operate
 *       electric and hybrid buses.
 * The driver/bus compatibility rules (B3-B5) accept an optional reference date
 * so age can be computed deterministically in tests; the no-argument overloads
 * use {@link LocalDate#now()}. Invalid data is reported via
 * {@link IllegalArgumentException}.</p>
 */
public final class BusValidator {

    private static final List<String> VALID_FUEL_TYPES =
            Arrays.asList("Diesel", "Hybrid", "Electricity");

    private static final DateTimeFormatter BIRTHDATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private BusValidator() {
    }

    // ---------------- B1: Bus ID format ----------------

    /** B1 (format only): the id must be exactly 8 characters, all digits. */
    public static boolean isValidBusId(String id) {
        if (id == null || id.length() != 8) {
            return false;
        }
        for (int i = 0; i < id.length(); i++) {
            if (!Character.isDigit(id.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidFuelType(String fuelType) {
        return VALID_FUEL_TYPES.contains(fuelType);
    }

    /** Field-level checks used by the repository on add (B1 + basic sanity). */
    public static void validateFields(Bus b) {
        if (b == null) {
            throw new IllegalArgumentException("Bus must not be null");
        }
        if (!isValidBusId(b.getBusID())) {
            throw new IllegalArgumentException(
                    "B1 violation: busID must be exactly 8 digits, got '" + b.getBusID() + "'");
        }
        if (b.getCapacity() <= 0) {
            throw new IllegalArgumentException("capacity must be a positive number");
        }
        if (b.getFuelLevel() < 0) {
            throw new IllegalArgumentException("fuelLevel must not be negative");
        }
        if (!isValidFuelType(b.getFuelType())) {
            throw new IllegalArgumentException("Invalid fuelType '" + b.getFuelType() + "'");
        }
    }

    // ---------------- B2: Capacity update restriction ----------------

    /** B2: a capacity update is valid only if it does not increase and stays positive. */
    public static boolean isValidCapacityUpdate(int oldCapacity, int newCapacity) {
        return newCapacity > 0 && newCapacity <= oldCapacity;
    }

    // ---------------- B3/B4/B5: Driver/bus compatibility ----------------

    /** Calculates the driver's age in whole years on {@code referenceDate}. */
    public static int ageInYears(String birthdate, LocalDate referenceDate) {
        LocalDate dob = LocalDate.parse(birthdate, BIRTHDATE_FORMAT);
        return Period.between(dob, referenceDate).getYears();
    }

    /** Returns {@code true} if the driver may operate the bus (B3, B4, B5). */
    public static boolean canDriverOperate(Driver driver, Bus bus, LocalDate referenceDate) {
        return reasonCannotOperate(driver, bus, referenceDate) == null;
    }

    public static boolean canDriverOperate(Driver driver, Bus bus) {
        return canDriverOperate(driver, bus, LocalDate.now());
    }

    /**
     * Returns a human-readable reason the driver cannot operate the bus, or
     * {@code null} if the assignment is permitted.
     */
    public static String reasonCannotOperate(Driver driver, Bus bus, LocalDate referenceDate) {
        int age = ageInYears(driver.getBirthdate(), referenceDate);
        String fuel = bus.getFuelType();
        boolean electric = "Electricity".equals(fuel);
        boolean hybrid = "Hybrid".equals(fuel);
        String licence = driver.getLicenseType();

        // B3: age vs capacity.
        if (age > 50 && bus.getCapacity() >= 50) {
            return "B3 violation: driver older than 50 cannot drive a bus with capacity >= 50";
        }
        // B4: electric buses require at least 5 years of experience.
        if (electric && driver.getExperienceYears() < 5) {
            return "B4 violation: electric buses require at least 5 years of experience";
        }
        // B5: electric & hybrid buses require a Heavy or PublicTransport licence.
        if ((electric || hybrid)
                && !("Heavy".equals(licence) || "PublicTransport".equals(licence))) {
            return "B5 violation: electric/hybrid buses require a Heavy or PublicTransport licence";
        }
        return null;
    }

    /** Throws {@link IllegalArgumentException} if the assignment is not permitted. */
    public static void validateAssignment(Driver driver, Bus bus, LocalDate referenceDate) {
        String reason = reasonCannotOperate(driver, bus, referenceDate);
        if (reason != null) {
            throw new IllegalArgumentException(reason);
        }
    }

    public static void validateAssignment(Driver driver, Bus bus) {
        validateAssignment(driver, bus, LocalDate.now());
    }
}
