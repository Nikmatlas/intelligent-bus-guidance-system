package com.ibdgs.integration;

import com.ibdgs.model.Driver;
import com.ibdgs.repository.DriverRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import java.io.FileWriter;

public class DriverRepositoryIntegrationTest {

    DriverRepository repository =
            new DriverRepository();

            @BeforeEach
void resetJsonFile() throws Exception {

    FileWriter writer =
            new FileWriter("data/drivers.json");

    writer.write("[]");

    writer.close();
}

            @Test
void validDriverShouldBeStored() {

    Driver driver = new Driver(
            "23@@5678AB",
            "John",
            5,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-2000"
    );

    assertTrue(repository.addDriver(driver));
}

@Test
void duplicateDriverShouldBeRejected() {

    Driver driver = new Driver(
            "23@@5678AB",
            "John",
            5,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-2000"
    );

    repository.addDriver(driver);

    assertFalse(repository.addDriver(driver));
}

@Test
void updatedDriverShouldBePersisted() {

    Driver driver = new Driver(
            "23@@5678AB",
            "John",
            5,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-2000"
    );

    repository.addDriver(driver);

    Driver updatedDriver = new Driver(
            "23@@5678AB",
            "John",
            8,
            "PublicTransport",
            "99|New Street|Melbourne|VIC|Australia",
            "01-01-2000"
    );

    assertTrue(
            repository.updateDriver(updatedDriver)
    );

    Driver retrievedDriver =
            repository.retrieveDriver("23@@5678AB");

    assertEquals(
            8,
            retrievedDriver.getExperienceYears()
    );
}

@Test
void driverCountShouldIncreaseCorrectly() {

    Driver driver1 = new Driver(
            "23@@5678AB",
            "John",
            5,
            "Heavy",
            "12|Street|City|State|Country",
            "01-01-2000"
    );

    Driver driver2 = new Driver(
            "34##5678CD",
            "David",
            3,
            "Heavy",
            "10|Street|City|State|Country",
            "02-02-2001"
    );

    repository.addDriver(driver1);
    repository.addDriver(driver2);

    assertEquals(
            2,
            repository.countDrivers()
    );
}
}