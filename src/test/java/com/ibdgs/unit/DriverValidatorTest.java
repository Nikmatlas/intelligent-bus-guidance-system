package com.ibdgs.unit;
import com.ibdgs.model.Driver;
import com.ibdgs.service.DriverValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DriverValidatorTest {

    DriverValidator validator = new DriverValidator();

    @Test
    void validDriverIdShouldPass() {

        String driverId = "23@@5678AB";

        assertTrue(validator.isValidDriverId(driverId));
    }

    @Test
    void driverIdWrongLengthShouldFail() {

        String driverId = "23@@567AB";

        assertFalse(validator.isValidDriverId(driverId));
}

    @Test
void driverIdWithoutEnoughSpecialCharactersShouldFail() {

    String driverId = "23456789AB";

    assertFalse(validator.isValidDriverId(driverId));
}

   @Test
void driverIdWithLowercaseEndingShouldFail() {

    String driverId = "23@@5678Ab";

    assertFalse(validator.isValidDriverId(driverId));
}

@Test
void validAddressShouldPass() {

    String address =
            "12|Nguyen Van Linh|HCMC|VIC|Australia";

    assertTrue(validator.isValidAddress(address));
}

@Test
void addressMissingFieldShouldFail() {

    String address =
            "12|Nguyen Van Linh|HCMC|Australia";

    assertFalse(validator.isValidAddress(address));
}

@Test
void addressWithEmptyFieldShouldFail() {

    String address =
            "12||HCMC|VIC|Australia";

    assertFalse(validator.isValidAddress(address));
}

@Test
void validBirthdateShouldPass() {

    String birthdate = "01-01-2000";

    assertTrue(validator.isValidBirthdate(birthdate));
}

@Test
void wrongBirthdateFormatShouldFail() {

    String birthdate = "2000-01-01";

    assertFalse(validator.isValidBirthdate(birthdate));
}

@Test
void invalidBirthdateShouldFail() {

    String birthdate = "32-13-2000";

    assertFalse(validator.isValidBirthdate(birthdate));
}

@Test
void experiencedDriverCannotChangeLicense() {

    Driver oldDriver = new Driver(
            "23@@5678AB",
            "John",
            12,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-1980"
    );

    Driver newDriver = new Driver(
            "23@@5678AB",
            "John",
            12,
            "PublicTransport",
            "12|Street|City|State|Country",
            "01-01-1980"
    );

    assertFalse(
            validator.canUpdateLicense(oldDriver, newDriver)
    );
}

@Test
void experiencedDriverKeepingLicenseShouldPass() {

    Driver oldDriver = new Driver(
            "23@@5678AB",
            "John",
            12,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-1980"
    );

    Driver newDriver = new Driver(
            "23@@5678AB",
            "John",
            12,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-1980"
    );

    assertTrue(
            validator.canUpdateLicense(oldDriver, newDriver)
    );
}

@Test
void driverWithTenYearsCanChangeLicense() {

    Driver oldDriver = new Driver(
            "23@@5678AB",
            "John",
            10,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-1980"
    );

    Driver newDriver = new Driver(
            "23@@5678AB",
            "John",
            10,
            "PublicTransport",
            "12|Street|City|State|Country",
            "01-01-1980"
    );

    assertTrue(
            validator.canUpdateLicense(oldDriver, newDriver)
    );
}

@Test
void changingDriverIdShouldFail() {

    Driver oldDriver = new Driver(
            "23@@5678AB",
            "John",
            5,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-2000"
    );

    Driver newDriver = new Driver(
            "99@@5678AB",
            "John",
            5,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-2000"
    );

    assertFalse(
            validator.canUpdateImmutableFields(
                    oldDriver,
                    newDriver
            )
    );
}

@Test
void changingNameShouldFail() {

    Driver oldDriver = new Driver(
            "23@@5678AB",
            "John",
            5,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-2000"
    );

    Driver newDriver = new Driver(
            "23@@5678AB",
            "David",
            5,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-2000"
    );

    assertFalse(
            validator.canUpdateImmutableFields(
                    oldDriver,
                    newDriver
            )
    );
}

@Test
void keepingDriverIdAndNameShouldPass() {

    Driver oldDriver = new Driver(
            "23@@5678AB",
            "John",
            5,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-2000"
    );

    Driver newDriver = new Driver(
            "23@@5678AB",
            "John",
            8,
            "PublicTransport",
            "99|New Street|Melbourne|VIC|Australia",
            "01-01-2000"
    );

    assertTrue(
            validator.canUpdateImmutableFields(
                    oldDriver,
                    newDriver
            )
    );
}
}