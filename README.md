# Intelligent Bus Driver Guidance System

This project is for Assignment 4 of ISYS3413/ISYS3475/ISYS1118.

The system is a Java Maven project that implements and tests core functions for an Intelligent Bus Driver Guidance System. The project focuses on driver and bus data management, validation rules, file-based storage, unit testing, integration testing, GitHub version control, and GitHub Actions CI/CD.

## Assignment Overview

The Intelligent Bus Driver Guidance System is designed to support public bus drivers with operational functions such as route guidance, stop alerts, route deviation detection, detour guidance, hazard reporting, and trip event confirmation.

For Assignment 4, the project focuses on implementing and testing the core driver and bus management functions.

The system must support:

- Adding drivers
- Retrieving drivers
- Updating drivers
- Counting stored drivers
- Adding buses
- Retrieving buses
- Updating buses
- Counting stored buses
- Storing and retrieving data using human-readable JSON files
- Unit testing using JUnit 5
- Integration testing using real JSON files
- Running automated tests using GitHub Actions

## Technologies Used

- Java 17
- Maven
- JUnit 5
- Gson
- GitHub
- GitHub Actions
- JSON file storage

## Project Structure

```text
intelligent-bus-guidance-system/
├── data/
│   ├── drivers.json
│   └── buses.json
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── ibdgs/
│   │               ├── model/
│   │               │   ├── Driver.java
│   │               │   └── Bus.java
│   │               ├── repository/
│   │               │   ├── DriverRepository.java
│   │               │   └── BusRepository.java
│   │               └── service/
│   │                   ├── DriverValidator.java
│   │                   └── BusValidator.java
│   └── test/
│       └── java/
│           └── com/
│               └── ibdgs/
│                   ├── unit/
│                   │   ├── DriverValidatorTest.java
│                   │   └── BusValidatorTest.java
│                   └── integration/
│                       ├── DriverRepositoryIntegrationTest.java
│                       └── BusRepositoryIntegrationTest.java
├── .github/
│   └── workflows/
│       └── maven.yml
├── .gitignore
├── pom.xml
└── README.md
```

## Main Classes

### Driver.java

The `Driver` class represents a bus driver.

It stores:

- Driver ID
- Name
- Experience years
- Licence type
- Address
- Birthdate

### Bus.java

The `Bus` class represents a bus.

It stores:

- Bus ID
- Capacity
- Fuel level
- Fuel type

### DriverRepository.java

The `DriverRepository` class handles driver storage operations.

It supports:

- Add driver
- Retrieve driver
- Update driver
- Count drivers

### BusRepository.java

The `BusRepository` class handles bus storage operations.

It supports:

- Add bus
- Retrieve bus
- Update bus
- Count buses

### DriverValidator.java

The `DriverValidator` class validates driver-related rules.

### BusValidator.java

The `BusValidator` class validates bus-related rules.

## Driver Conditions

The system must satisfy the following driver conditions.

### D1. Driver ID Rules

The driver ID must:

- Be unique
- Be exactly 10 characters long
- Have the first two characters as digits between 2 and 9
- Have at least two special characters between characters 3 and 8
- Have the last two characters as uppercase letters from A to Z

### D2. Address Format

The driver address must follow this format:

```text
Street Number|Street Name|City|State|Country
```

### D3. Birthdate Format

The birthdate must follow this format:

```text
DD-MM-YYYY
```

### D4. Licence Update Restriction

If a driver has more than 10 years of experience, their licence type cannot be changed during update operations.

### D5. Immutable Fields

The following fields cannot be changed during update operations:

- Driver ID
- Name

## Bus Conditions

The system must satisfy the following bus conditions.

### B1. Bus ID Rules

The bus ID must:

- Be unique
- Be exactly 8 characters long
- Contain digits only

### B2. Capacity Update Restriction

The bus capacity cannot increase during update operations.

However, the bus capacity is allowed to decrease.

### B3. Driver Age Restriction

Drivers older than 50 years cannot drive buses with a capacity of 50 or more.

### B4. Electric Bus Restriction

Only drivers with at least 5 years of experience can drive electric buses.

This applies when:

```text
fuelType = Electricity
```

### B5. Driver Licence Restriction

Only drivers with a Heavy or PublicTransport licence can operate electric and hybrid buses.

This applies when:

```text
fuelType = Electricity
fuelType = Hybrid
```

## Testing Requirements

The project uses JUnit 5 for testing.

### Driver Unit Tests

At least 15 driver unit tests are required.

Each driver condition must have at least 3 test cases:

- D1: Driver ID rules
- D2: Address format
- D3: Birthdate format
- D4: Licence update restriction
- D5: Immutable fields

The tests should include:

- Normal cases
- Invalid inputs
- Edge cases

### Bus Unit Tests

At least 15 bus unit tests are required.

Each bus condition must have at least 3 test cases:

- B1: Bus ID rules
- B2: Capacity update restriction
- B3: Driver age restriction
- B4: Electric bus experience restriction
- B5: Driver licence restriction

The tests should include:

- Normal cases
- Invalid inputs
- Edge cases

### Driver Integration Tests

At least 4 driver integration tests are required.

The tests should verify:

- Valid drivers are stored correctly
- Invalid drivers are rejected
- Updates are persisted correctly
- Driver record counts are updated correctly

### Bus Integration Tests

At least 4 bus integration tests are required.

The tests should verify:

- Valid buses are stored correctly
- Invalid buses are rejected
- Updates are persisted correctly
- Bus record counts are updated correctly

## How to Run the Project

This project does not require a graphical user interface or a menu system.

The main way to check the project is by running the tests.

## How to Run Tests Locally

Run this command in the terminal:

```bash
mvn test
```

If Maven is not installed, install it first:

```bash
brew install maven
```

Then check Maven:

```bash
mvn -v
```

Then run the tests again

## GitHub Actions

This project uses GitHub Actions to automatically run tests.
The workflow file is located at:

```text
.github/workflows/maven.yml
```

The workflow runs automatically when:

- Code is pushed to the repository
- A pull request is created

The workflow runs:

```bash
mvn test
```

## Git Workflow

Team members should not push directly to the `main` branch.
Each person should work on their own branch.

### Branch Names

Nico:
```bash
git checkout -b nico-core-model
```

An:
```bash
git checkout -b an-driver-section
```

Quoc:
```bash
git checkout -b quoc-bus-section
```

### Basic Git Commands

Before starting work:

```bash
git checkout main
git pull
```

Create a new branch:

```bash
git checkout -b branch-name
```

After editing files:

```bash
git add .
git commit -m "Write a clear message about your changes"
git push -u origin branch-name
```

After pushing, create a pull request on GitHub.

## Team Task Division

### Nico — GitHub / Integration / Core Model

Main responsibility: make sure the whole project connects and the final submission works.

Nico is responsible for:

- Creating and managing the GitHub repository
- Setting up the Maven project structure
- Setting up GitHub Actions
- Checking branches and pull requests
- Reviewing and merging work
- Running final `mvn test`
- Fixing integration issues
- Creating and maintaining `Driver.java`
- Creating and maintaining `Bus.java`
- Creating the README
- Recording or organising the 3-minute video
- Preparing the final ZIP submission
- Checking the contribution form

### An — Driver Section

Main responsibility: driver implementation and driver testing.

An is responsible for:

- `DriverValidator.java`
- `DriverRepository.java`
- `DriverValidatorTest.java`
- `DriverRepositoryIntegrationTest.java`

An must cover:

- D1. Driver ID rules
- D2. Address format
- D3. Birthdate format
- D4. Licence update restriction
- D5. Immutable driver ID and name

Required tests from An:

- At least 15 driver unit tests
- At least 4 driver integration tests

### Quoc — Bus Section + User Stories

Main responsibility: bus implementation, bus testing, and user stories.

Quoc is responsible for:

- `BusValidator.java`
- `BusRepository.java`
- `BusValidatorTest.java`
- `BusRepositoryIntegrationTest.java`
- 8 user stories
- 24 acceptance criteria

Quoc must cover:

- B1. Bus ID rules
- B2. Capacity update restriction
- B3. Driver age restriction
- B4. Electric bus experience restriction
- B5. Licence restriction for electric and hybrid buses

Required tests from Quoc:

- At least 15 bus unit tests
- At least 4 bus integration tests

## User Stories

The assignment also requires 8 user stories and 24 acceptance criteria.

Each user story should follow this format:

```text
As a [type of user],
I want to [perform an action],
so that [benefit].
```

Each user story must have 3 acceptance criteria.

Suggested user story topics:

- Secure driver login
- Real-time route guidance
- Bus stop approach alert
- Arrival confirmation
- Route deviation detection
- Automatic detour guidance
- Delay or hazard reporting
- Trip event confirmation

## Final Submission Checklist

The final ZIP file should include:

- Word or PDF document
- Test case tables
- 8 user stories
- 24 acceptance criteria
- Complete Java Maven project
- Source code
- Unit tests
- Integration tests
- `pom.xml`
- JSON data files
- GitHub Actions workflow file
- Recorded video
- Completed contribution form

## Video Requirements

The recorded video must show:

- The project linked to the GitHub repository
- Code changes being pushed to GitHub
- GitHub Actions running automatically
- Tests running through GitHub Actions
- Confirmation that all tests pass
- GitHub commit history showing team contributions

Video requirements:

- Maximum length: 3 minutes
- Maximum file size: 100 MB
- Minimum resolution: 720p
- Face does not need to be visible
- One team member can present on behalf of the group

## Team Members

- Nico: GitHub setup, Maven setup, GitHub Actions, model classes, integration, README, video, and final submission
- An: Driver implementation, driver validation, driver repository, driver unit tests, and driver integration tests
- Quoc: Bus implementation, bus validation, bus repository, bus unit tests, bus integration tests, user stories, and acceptance criteria
