# SOPRA FS22 - Group 04 Backend

## Introduction

Group matching in university courses can be very tedious. Whether it is finding great group members, having similar group sizes
or simply having balanced skills across all groups.
In order to circumvent that we decided to create a group matcher which does exactly that: match groups according to customizable
traits and group sizes with the help of a matching quiz!

The matching quiz consists of single and multiple choice questions which are editable and predefined by the course administrators -
of which you can have as many as you need! The quiz will then be sent out via e-mail to a customizable list of students/participants
who can fill it out and submit their answers!

Depending on the settings you implemented (group size etc.) the groups will be matched accordingly once everybody has submitted
their answers or the deadline has been met - of course all participants will be notified via e-mail with their group and team members!

The goal of this application is to support both teachers and students in group related work and provide everyone with a quick, easy
and fair way of finding group members and organizing the surrounding structure.

## Technologies
- Java
- SpringBoot
- PostgreSQL
- JPA
- Thymeleaf
- Github Actions
- Heroku
- REST API

## High-level components
### Development components

- [Controller](src/main/java/ch/uzh/soprafs22/groupmatcher/controller)
- [DTO](src/main/java/ch/uzh/soprafs22/groupmatcher/dto)
- [Model](src/main/java/ch/uzh/soprafs22/groupmatcher/model)
- [Repository](src/main/java/ch/uzh/soprafs22/groupmatcher/repository)
- [Service](src/main/java/ch/uzh/soprafs22/groupmatcher/service)
- [Application](src/main/java/ch/uzh/soprafs22/groupmatcher/Application.java)

### Controller
The controller is mainly responsible for handling requests made a specific endpoints. It processes the incoming REST API requests, preparing a model, and returning the view to be rendered as a response.

### DTO
DTO is responsible for passing data with multiple attributes in one shot from client to server to avoid multiple calls while they also serve in hiding implementation details of domain objects.

### Model
The model defines a holder for model attributes and is primarily designed for adding attributes. It is responsible for holding application data which is displayed in the view.

### Repository
Repository is a mechanism for encapsulating storage, retrieval, and search behaviour which emulates a collection of objects 

### Service
The Service component contains the business logic that needs to be implemented in the application. It is used to provide functionalities to the Controller class to serve the requests that are made at the API endpoints.

### Application
The entry point for the entire application. One must run this class to start the backend server of the web application.

### Testing components
- [Controller Tests](src/test/java/ch/uzh/soprafs22/groupmatcher/controller)
Tests for controller components.

- [Repository Tests](src/test/java/ch/uzh/soprafs22/groupmatcher/repository)
Tests for repository components.

- [Service Tests](src/test/java/ch/uzh/soprafs22/groupmatcher/service)
Tests for service components.

## Launch and Development

### Install PostgreSQL
The application uses PostgreSQL as the database for performing all the CRUD (Create, Read, Update and Delete) operations.

You can download and install PostgreSQL on your computer [here](https://www.postgresql.org/download/)

If you are new to PostgreSQL you can learn more about it in their official documentation [here](https://www.postgresql.org/docs/)

### Setup email server
The application uses email server for communication with and between users (incl. admin and students).
You need to setup an email server before you run the application. 

You can find some tutorials to help you 
- [tutorial1](https://www.makeuseof.com/secure-email-server/) 
- [tutorial2](https://helpdeskgeek.com/how-to/how-to-set-up-your-own-email-server/)

### Clone the application.
You can clone the backend repository from GitHub using
- Clone the repo using git clone https://github.com/sopra-fs22-group-4/group-matcher-backend.git
- Download the [zip](https://github.com/sopra-fs22-group-4/group-matcher-backend/archive/refs/heads/main.zip)
- Open using github desktop [here](x-github-client://openRepo/https://github.com/sopra-fs22-group-4/group-matcher-backend)

### Configure the application
Edit the [application.yml](/src/main/resources/application.yml) to do the following:
- Set the credentials for PostgreSQL to access the database.
- Set the credentials for the email server you set up for the application.

Set up the URL for the frontend of the application via the ENV variable in the [application.yml](/src/main/resources/application.yml).

### Running the application
Once the configurations mentioned above you can run the [application.java](/src/main/java/ch/uzh/soprafs22/groupmatcher/Application.java) to start the backend of the web app.

## Illustrations

## Roadmap
Potential improvements or extensions may include:

- Extending the questionnaire to include text questions, more background questions
- Include more sophisticated logic for the matching process
- Instead of having an e-mail service include a direct application service with notifications through the
  application itself rather than e-mail

## Learn more
- Java Documentation [Java Documentation](https://docs.oracle.com/en/java/)
- Getting started with Spring [Spring Documentation](https://spring.io/guides/gs/spring-boot/)
- Introduction to Thymeleaf [Thymeleaf Documentation](https://www.baeldung.com/thymeleaf-in-spring-mvc)
- Learn PostgreSQL [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## Authors & Acknowledgement
>M. Guido, V. Herzl, H. Kim, A. A. Sirsikar, J. Meier

## License
Licensed under GNU General Public License v3.0
- See [License](LICENSE)
