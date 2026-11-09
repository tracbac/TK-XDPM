# Serenity Cucumber Order Demo

A Java 21 demo application for order cancellation and payment, with business rules
tested through unit tests and Serenity BDD/Cucumber acceptance scenarios.

## Project structure

```text
src/
  main/java/starter/
    domain/             Order entities, statuses, and business rules
    application/        Use cases, request/response models, and ports
    adapters/            In-memory/demo implementations of application ports
    presentation/        Swing views, controllers, presenters, and observers
    bootstrap/           Demo application composition roots
  test/
    java/starter/
      unittests/         Fast unit tests for domain and use-case behavior
      stepdefinitions/   Cucumber glue and test fixtures
      acceptancetests/   Serenity Cucumber integration-test runner
    resources/
      features/          Business-readable acceptance scenarios
```

The application follows dependency inversion: domain code does not depend on
application or I/O code; use cases depend on domain objects and application
ports; adapters and presentation code implement or use those ports.

## Requirements

- JDK 21
- Maven 3.9+ or the included Gradle wrapper

## Build and test

Maven is used by the CI workflows:

```shell
mvn clean verify
```

The build runs unit tests in the test phase and Serenity/Cucumber acceptance
tests in the integration-test phase. The Serenity report is generated under
`target/site/serenity`.

The Gradle wrapper can also run the same unit and acceptance tests:

```shell
./gradlew clean test
```

On Windows, run `gradlew.bat clean test`.

## Demo applications

Run `starter.bootstrap.PaymentDemoApplication` or
`starter.bootstrap.CancelOrderDemoApplication` from the IDE. They use in-memory
demo adapters; the payment adapter is simulated and does not process real
payments.
