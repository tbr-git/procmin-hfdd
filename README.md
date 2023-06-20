# Process Comparison with Masking and Refinements
This project contains a web-based implementation of our process variant analysis tool which allows to compare processes by iteratively masking and refining differences. 

## Running the App
If Docker is installed, the **easiest way** to run the application is to use the provided docker-compose file.
To run the app, simply run

        docker-compose up

Afterward, you can open the app in your browser using the following URL:

        http://localhost:80

### Running Standalone and for Development
The frontend is implemented using Angular and can be run for development purposes with the provided docker-compose file.

The backend is implemented in Java using Spring Boot and ProM[^1] libraries for loading event logs.
It is built and can be run in standalone mode using gradle.


[^1]: [Homepage Process Mining Workbench](https://promtools.org/)
