package com.ibdgs.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.ibdgs.model.Driver;

/**
* Provides validation methods for Driver objects
* according to the Assignment 4 requirements.
  */
  public class DriverValidator {

  /**Validates Driver ID based on D1 rules.*/
    public boolean isValidDriverId(String driverId) {

    if (driverId == null) {
    return false;
    }

    if (driverId.length() != 10) {
    return false;
    }

    if (!Character.isDigit(driverId.charAt(0))
    || driverId.charAt(0) < '2'
    || driverId.charAt(0) > '9') {
    return false;
    }

    if (!Character.isDigit(driverId.charAt(1))
    || driverId.charAt(1) < '2'
    || driverId.charAt(1) > '9') {
    return false;
    }

    int specialCount = 0;

    for (int i = 2; i <= 7; i++) {

     char c = driverId.charAt(i);

     if (!Character.isLetterOrDigit(c)) {
         specialCount++;
     }

    }

    if (specialCount < 2) {
    return false;
    }

    if (!Character.isUpperCase(driverId.charAt(8))
    || !Character.isUpperCase(driverId.charAt(9))) {
    return false;
    }

    return true;
    }

  /**
  * Validates address format:
  * StreetNumber|StreetName|City|State|Country
    */
    public boolean isValidAddress(String address) {

    if (address == null || address.isBlank()) {
    return false;
    }

    String[] parts = address.split("\\|");

    if (parts.length != 5) {
    return false;
    }

    for (String part : parts) {

     if (part.trim().isEmpty()) {
         return false;
     }

    }

    return true;
    }

  /** Validates birthdate format DD-MM-YYYY. */
    public boolean isValidBirthdate(String birthdate) {

    if (birthdate == null || birthdate.isBlank()) {
    return false;
    }

    try {
     DateTimeFormatter formatter =
             DateTimeFormatter.ofPattern("dd-MM-yyyy");

     LocalDate.parse(birthdate, formatter);

     return true;

    } catch (DateTimeParseException e) {

     return false;

    }
    }

  /*
  * Checks whether a driver's licence type
  * can be changed during an update.
    */
    public boolean canUpdateLicense(Driver oldDriver, Driver newDriver) {

    if (oldDriver.getExperienceYears() > 10) {
    
     return oldDriver.getLicenseType()
             .equals(newDriver.getLicenseType());
    

    }

    return true;
    }

  /*** Ensures immutable fields remain unchanged.*/ 

    public boolean canUpdateImmutableFields(
    Driver oldDriver,
    Driver newDriver) {

    boolean sameId =
    oldDriver.getDriverID()
    .equals(newDriver.getDriverID());

    boolean sameName =
    oldDriver.getName()
    .equals(newDriver.getName());

    return sameId && sameName;
    }
    }
