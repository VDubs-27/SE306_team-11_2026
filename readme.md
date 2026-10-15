# Mainframe Interface: Project 2 Starter

Welcome to the second project for SOFTENG 306.

The project specification is available [on Canvas](https://canvas.auckland.ac.nz/courses/142477/pages/project-2-overview?module_item_id=3007942). The four deliverables for this project are:
* [Proposal](https://canvas.auckland.ac.nz/courses/142477/assignments/514280)
* [Presentation](https://canvas.auckland.ac.nz/courses/142477/assignments/514279)
* [Code](https://canvas.auckland.ac.nz/courses/142477/assignments/514277)
* [Documentation](https://canvas.auckland.ac.nz/courses/142477/assignments/514278)

## Team 11

* Jack Preston - GitHub Login
* Vikram Varadarajan - GitHub Login
* Aidan Gleeson - GitHub Login
* Carson May - GitHub Login

## Running the application

**Requirements:** JDK 17 or later. Maven does not need to be installed; the
project includes the Maven Wrapper, which downloads the correct version
automatically on first use.

From the project root:

| Task | macOS / Linux | Windows |
|---|---|---|
| Run the application | `./mvnw javafx:run` | `.\mvnw.cmd javafx:run` |
| Run the tests | `./mvnw test` | `.\mvnw.cmd test` |
| Clean build | `./mvnw clean javafx:run` | `.\mvnw.cmd clean javafx:run` |

Use these commands (or IntelliJ's Maven panel: **Plugins → javafx → javafx:run**)
rather than IntelliJ's run arrow next to `main`, which fails with
"JavaFX runtime components are missing".

### Signing in

The application opens on the login screen and signs in through the mainframe, so the
mainframe server must be reachable before any other screen can be used.

* **Test account:** username `test`, password `test`.
* **Server:** by default the application uses the same test server as the tests. To use
  another one, pass its address when starting:

  | macOS / Linux | Windows |
  |---|---|
  | `./mvnw javafx:run -Dlavs.mainframe.url=https://example.com/` | `.\mvnw.cmd javafx:run "-Dlavs.mainframe.url=https://example.com/"` |

* **"Could not reach the mainframe"** means the server is down or unreachable; nothing
  past the login screen works until it is back.
* **"Could not load the list values from the mainframe"** means the sign-in itself
  worked but loading the mainframe's list values then failed; try signing in again.

## Continuous integration

Every pull request runs two GitHub Actions jobs:

* **build** (Windows): compiles the code, runs the tests, runs
  Checkstyle (complexity, nesting depth, fan-out and naming rules) and
  fails if JaCoCo line or branch coverage of `lavs.logic` is below 95%.
  The coverage report is written to `target/site/jacoco/index.html`.
  `ArchitectureTests` also fails the build if any class outside
  `lavs.logic.adapter` uses `mainframe.client`, or if `lavs.ui` uses
  `lavs.logic.adapter`.
* **metrics** (Ubuntu): runs CK on the `lavs` package, fails if the
  depth of inheritance (DIT) or number of children (NOC) targets are
  broken, and adds a CBO, WMC, fan-in and fan-out table to the job summary.

Run the same checks locally before pushing:

    .\mvnw.cmd verify        (Windows)
    ./mvnw verify            (macOS / Linux)

**Live-server tests:** the provided tests in `mainframe.client.messages`
call the shared course mainframe, so they can fail because of other
teams' data. They run in every build but do not fail it. To run only
those tests:

    .\mvnw.cmd test -Dtest="mainframe.client.messages.*Tests"

Name your own test classes `*Tests` (e.g. `CustomerServiceTests`),
otherwise Maven will not run them.

## Project structure

* `src/main/java/lavs/App.java`: application entry point.
* `src/main/java/lavs/ui/`: user interface layer (screens and controllers).
  Must not use `mainframe.client` or `lavs.logic.adapter`; call services instead.
* `src/main/java/lavs/logic/`: business logic layer (services, validation,
  domain objects, mainframe adapter). Only `lavs.logic.adapter` may use
  `mainframe.client`; services call `MainframeAdapter` instead.
* `src/main/resources/lavs/ui/`: FXML layouts and CSS.
* `src/main/java/mainframe/client/`: provided mainframe interface. Do not edit.

## Initial Repository Code

This repository initially contains the code for the mainframe interface and the unit tests. The following code is supplied:
* [mainframe](src/main/java/mainframe/client/): the interfaces for sending requests to and receiving responses from the mainframe. You will need to use the [Connection](src/main/java/mainframe/client/Connection.java) interface in your code. We have provided an implementation that connects to a web server in [HttpConnection](src/main/java/mainframe/client/HttpConnection.java).
* [tests](src/test/java/mainframe/client/): the unit tests. Most of these connect to the web server, but there is an [example unit test](src/test/java/mainframe/client/SimulateListCustomers.java) for connecting to a stub.