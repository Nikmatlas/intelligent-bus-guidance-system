package com.ibdgs.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ibdgs.model.Driver;
import com.ibdgs.service.DriverValidator;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DriverRepository {

    private final DriverValidator validator =
        new DriverValidator();

    private static final String FILE_PATH = "data/drivers.json";

    private final Gson gson =
            new GsonBuilder().setPrettyPrinting().create();

            private List<Driver> loadDrivers() {

    try {

        FileReader reader = new FileReader(FILE_PATH);

        Type listType =
                new TypeToken<List<Driver>>() {}.getType();

        List<Driver> drivers =
                gson.fromJson(reader, listType);

        reader.close();

        return drivers != null
                ? drivers
                : new ArrayList<>();

    } catch (Exception e) {

        return new ArrayList<>();
    }
}

private void saveDrivers(List<Driver> drivers) {

    try {

        FileWriter writer =
                new FileWriter(FILE_PATH);

        gson.toJson(drivers, writer);

        writer.flush();
        writer.close();

    } catch (Exception e) {

        e.printStackTrace();
    }
}

public boolean addDriver(Driver driver) {
if (!validator.isValidDriverId(driver.getDriverID())
        || !validator.isValidAddress(driver.getAddress())
        || !validator.isValidBirthdate(driver.getBirthdate())) {

    return false;
}
    List<Driver> drivers = loadDrivers();

    for (Driver existingDriver : drivers) {

        if (existingDriver.getDriverID()
                .equals(driver.getDriverID())) {

            return false;
        }
    }

    drivers.add(driver);

    saveDrivers(drivers);

    return true;
}

public Driver retrieveDriver(String driverId) {

    List<Driver> drivers = loadDrivers();

    for (Driver driver : drivers) {

        if (driver.getDriverID().equals(driverId)) {
            return driver;
        }
    }

    return null;
}

public int countDrivers() {

    return loadDrivers().size();
}

public boolean updateDriver(Driver updatedDriver) {

    List<Driver> drivers = loadDrivers();

    for (int i = 0; i < drivers.size(); i++) {

        if (drivers.get(i).getDriverID()
                .equals(updatedDriver.getDriverID())) {

            drivers.set(i, updatedDriver);

            saveDrivers(drivers);

            return true;
        }
    }

    return false;
}

}