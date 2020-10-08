# Price Basket

A simplistic command-line tool for pricing item baskets.
Extensible by design by relying on data files for external configuration and Java's Service Provider platform for special offer implementations.

## Building & Testing

This project relies on the [errorprone](https://errorprone.info/) compiler plugin for static analysis.
For building the project, running its tests and performing static analysis only the `build` task needs to be executed.
```shell
./gradlew build
``` 

## How to use

This project uses Gradle as a build system and has no external dependencies so several ways of running it exist.

### Running through Gradle

The tool can be ran directly from Gradle by using the `run` task (optionally with arguments):
```shell
./gradlew run --args 'apples milk bread'
```

### Packaged application

A distribution archive can be built with the `distZip` or `distTar` tasks.
The application can be executed by using one of the launcher scripts in the packaged `bin` directory.
Alternatively, the unpacked contents can be used directly by using the `installDist` task instead.
Built archives can be found in the `build/distributions` directory, the installed contents in the `build/install` directory.
```shell
./gradlew installDist
build/install/price-basket/bin/price-basket apples milk bread
```

### Running the JAR

An executable JAR can be built by using the `jar` task.
This JAR can then be executed directly due to the lack of external dependencies of the application.
The built JAR can be found in the `build/libs` directory.
```shell
./gradlew jar
java -jar build/libs/price-basket-0.1.0.jar apples milk bread
```

## Configuring

All of the data files are text based.
Two data files exist: [catalog.list](src/main/resources/catalog.list) and [offers.list](src/main/resources/offers.list).
The former configures which items are available in our "shop" and their prices.
The latter configures which special offers are currently available.

For the special offers there is no hard-coded format for the file.
Instead, every line can have its own format since each offer is specified as a rule (and the rules can be of varying complexities).
By default two rules are packaged, a [direct discount](src/main/java/dev/vacant/pricebasket/DiscountOffer.java) and a [bundle offer](src/main/java/dev/vacant/pricebasket/BundleOffer.java).
However, the parsers for the special offers are read through Service Providers.
Therefore, more special rule formats can be added to the application just by extending the classpath.

## Potential Improvements

1. Improve formatting of negative monetary amounts (currently formatted as £-1.00);
2. Add a time component to the offers, so that they can be added in advance / automatically become unavailable past a certain time;
3. Use JDBC for retrieving the configuration from a database instead of text files;
4. Use [picocli](https://picocli.info/) for a more standard/feature complete CLI;
5. Automatically extend the classpath from a pre-defined directory (a more plugin-based approach);
6. Create a separate API module to separate the command-line application from the required API for developing special offers;
