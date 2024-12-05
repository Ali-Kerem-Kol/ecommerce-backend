# E-Commerce Backend Java Project

## Project Description

This project implements the backend for an e-commerce platform built using Spring Boot. It provides an API for users to add products to their shopping cart, manage their cart, and place orders. Security is implemented using JWT (JSON Web Token), ensuring user authentication. It also includes necessary services and database structures for managing user accounts, shopping carts, and orders.

---

## Technologies Used

- **Java 17** (or later versions)
- **Spring Boot** (Web, Data, Security, JPA)
- **JWT** (JSON Web Token) for authentication
- **Spring Security** for securing API endpoints
- **PostgreSQL** (or another preferred database)
- **Lombok** for reducing boilerplate code
- **Maven** for dependency management
- **JPA / Hibernate** for database interactions
- **Postman** for API testing

---

## Features

- User registration and login
- JWT-based security and authentication
- Shopping cart management
  - Add, remove, and update product quantities in the cart
  - View items in the cart
  - Clear the cart
- Order creation
- Admin role for managing users and products
- Error handling with user-friendly error messages
- Secured API endpoints with Spring Security

---

## Setup

1. Clone the repository to your local machine.

    ```bash
    git clone https://github.com/your-username/E-Commerce-Backend-Java-Project.git
    ```

2. Open the project in your IDE (e.g., IntelliJ IDEA or Eclipse).

3. Add the necessary properties in the `application.properties` file. Specifically, ensure you update the following fields with your own information:

    - **Database**:
      - `spring.datasource.url`: Set your database URL.
      - `spring.datasource.username`: Your database username.
      - `spring.datasource.password`: Your database password.
      
    - **Email**:
      - `spring.mail.username`: Your email address (for sending confirmation emails).
      - `spring.mail.password`: Application-specific password for your email account.

    **Warning:** Make sure to replace the placeholders in the `application.properties` file with your actual credentials:
    
    ```properties
    spring.datasource.url=[Your database URL]
    spring.datasource.username=[Your username]
    spring.datasource.password=[Your password]
    
    spring.mail.username=[Your email address (for sending confirmation emails)]
    spring.mail.password=[Application-specific password for your email account]
    ```

4. Run the project.

    ```bash
    ./mvnw spring-boot:run
    ```

5. Open your browser and access the API at `http://localhost:8080`.

---

## Postman Workspace
You can also test the API through the shared Postman workspace:
[<img src="https://run.pstmn.io/button.svg" alt="Run In Postman" style="width: 128px; height: 32px;">](https://god.gw.postman.com/run-collection/37739159-d3965a51-94ac-419c-8068-b965338288db?action=collection%2Ffork&source=rip_markdown&collection-url=entityId%3D37739159-d3965a51-94ac-419c-8068-b965338288db%26entityType%3Dcollection%26workspaceId%3Ddbeb00ea-a61f-4017-acac-c435de319aa6)

## Contribution

Feel free to fork this repository, make improvements, and submit a pull request. Contributions are always welcome!

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
