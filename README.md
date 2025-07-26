<p align="center">
  <a href="" rel="noopener">
 <img width=200px height=200px src="https://mlafrkaocppdojtsyysc.supabase.co/storage/v1/object/public/files//unify_icon_2.svg" alt="Unify Backend Logo"></a>
</p>

<h3 align="center">Unify Backend</h3>

<div align="center">

[![Status](https://img.shields.io/badge/status-active-success.svg)](https://github.com/unify-app/unify-backend)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](/LICENSE)
[![Java Version](https://img.shields.io/badge/java-21-brightgreen.svg)](https://www.oracle.com/java/technologies/downloads/#jdk21)
[![Spring Boot Version](https://img.shields.io/badge/spring%20boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker Image](https://img.shields.io/badge/docker%20image-minhdangdev%2Funify--backend-blue.svg)](https://hub.docker.com/r/minhdangdev/unify-backend)

</div>

---

<p align="center">
  🚀 The robust backend service for the Unify application, providing core functionalities for real-time communication, data persistence, and secure API access.
</p>

## 📝 Table of Contents

- [About](#about)
- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting_started)
- [Prerequisites](#prerequisites)
- [Local Development](#local_development)
- [API Documentation](#api_documentation)
- [Configuration](#configuration)
- [Building and Deployment](#building_and_deployment)
- [Testing](#testing)
- [Built With](#built_with)
- [Contributing](#contributing)
- [Authors](#authors)
- [Acknowledgments](#acknowledgment)

## 🧐 About <a name = "about"></a>

The **Unify Backend** is a comprehensive Spring Boot application designed to power the Unify platform. It provides a secure, scalable, and highly available set of APIs and services for managing user interactions, real-time messaging, and persistent data storage. Built with modern Java technologies, it leverages Spring's ecosystem to deliver a robust and maintainable solution.

## ✨ Features <a name = "features"></a>

- **RESTful API Services:** Exposes well-defined API endpoints for client-side consumption.
- **User Authentication & Authorization:** Implements robust security using Spring Security and OAuth2 Resource Server.
- **Real-time Communication:** Utilizes WebSockets for live interactions and data synchronization.
- **Data Persistence:** Integrates with PostgreSQL for relational data and MongoDB for document-based storage.
- **Caching Layer:** Leverages Redis for efficient data caching, improving performance.
- **Messaging Capabilities:** Supports secure messaging and real-time events.
- **Health Monitoring:** Provides actuator endpoints for application monitoring and management.
- **API Documentation:** Automatically generated API documentation using SpringDoc OpenAPI.

## 🏛️ Architecture <a name = "architecture"></a>

The Unify Backend follows a microservice-oriented architecture, focusing on clear separation of concerns and scalability. It's built on Spring Boot, providing an opinionated approach to application development. Key architectural components include:

- **Spring WebFlux:** For reactive programming and handling high concurrency.
- **Spring Data JPA & MongoDB:** For flexible data storage solutions.
- **Spring Security:** For comprehensive authentication and authorization.
- **WebSocket:** For bi-directional, real-time communication.
- **MapStruct:** For efficient and type-safe object mapping.
- **Lombok:** To reduce boilerplate code.

## 📁 Project Structure <a name = "project_structure"></a>

```
src/main/java/com/unify/app/
├── comments/        # Handles all comment-related logic and APIs
│   ├── domain/      # Core business logic for comments (models, services, repositories)
│   └── web/         # REST controllers for comments
├── common/          # Shared utilities, exceptions, and common models
│   ├── exceptions/  # Custom exception classes and global exception handler
│   └── models/      # Common data structures (e.g., PagedResult)
├── config/          # Application-wide configuration classes (e.g., caching, OpenAPI)
├── followers/       # Logic for user following functionality
│   └── domain/
│   └── web/
├── groups/          # Manages user groups and memberships
│   ├── domain/
│   └── web/
├── hashtags/        # Handles hashtag creation, indexing, and retrieval
│   ├── domain/
│   └── web/
├── media/           # Services for media (e.g., image/video) uploads and management
│   ├── config/
│   ├── domain/
│   └── web/
├── messages/        # Direct and group messaging functionality
│   └── domain/
│   └── web/
├── notifications/   # Handles user notifications
│   ├── domain/
│   └── web/
├── posts/           # Core logic for user posts and associated media
│   ├── domain/
│   └── liked/       # Manages liked posts and related data
│   └── saved/       # Manages saved posts and related data
│   └── web/
├── reports/         # Functionality for user reporting
│   └── domain/
│   └── web/
├── users/           # User authentication, profiles, and management
│   └── domain/
│   └── web/
│       └── controllers/
│       └── webhook/ # Handlers for incoming webhooks (e.g., LiveKit events)
└──...

```

## 🏁 Getting Started <a name = "getting_started"></a>

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites <a name = "prerequisites"></a>

Ensure you have the following installed:

- **Java Development Kit (JDK) 21**:
  ```bash
  # Verify Java version
  java -version
  ```
- **Maven 3.8+**:
  ```bash
  # Verify Maven version
  mvn -v
  ```
- **Docker & Docker Compose** (Optional, for running services locally):
  ```bash
  # Verify Docker version
  docker version
  docker compose version
  ```
- **PostgreSQL**: A running instance for relational data.
- **MongoDB**: A running instance for document data.
- **Redis**: A running instance for caching.

### Local Development <a name = "local_development"></a>

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/mdang-dev/unify-backend.git
    cd unify-backend
    ```
2.  **Configure environment variables:**
    Create a `.env` file in the project root based on `application.yml` and add your database credentials, LiveKit API keys, and other necessary configurations.
3.  **Build the project:**
    ```bash
    mvn clean install
    ```
4.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```
    The application will start on port `8080` by default.

## 🤝 Git Workflow & Conventions <a name = "git_workflow"></a>

To maintain a clean and effective version control history, please follow these guidelines when contributing:

### Branching Strategy

We use a feature-branch workflow. All development work should occur on dedicated branches, not directly on `main`.

- **`main` branch:** Represents the latest stable and deployable version of the application. Only pull requests are merged into `main`.
- **Feature Branches:**
  - **Naming Convention:** Use `feature/<short-descriptive-name>` (e.g., `feature/user-profile-editing`).
  - **Purpose:** For developing new features.
  - **Creation:** Branch off from `main`.
- **Bugfix Branches:**
  - **Naming Convention:** Use `bugfix/<issue-description>` (e.g., `bugfix/login-error-fix`).
  - **Purpose:** For fixing bugs.
  - **Creation:** Branch off from `main`.

### Commit Message Guidelines

Clear and descriptive commit messages are crucial for understanding the project's history. Please follow the Conventional Commits specification (or a similar, consistent style):

- **`<type>`:** Mandatory. Must be one of:
  - `feat`: A new feature
  - `fix`: A bug fix
  - `docs`: Documentation only changes
  - `style`: Changes that do not affect the meaning of the code (white-space, formatting, missing semicolons, etc.)
  - `refactor`: A code change that neither fixes a bug nor adds a feature
  - `perf`: A code change that improves performance
  - `test`: Adding missing tests or correcting existing tests
  - `build`: Changes that affect the build system or external dependencies (maven, docker, etc.)
  - `ci`: Changes to our CI configuration files and scripts
  - `chore`: Other changes that don't modify src or test files
  - `revert`: Reverts a previous commit
- **`<scope>` (optional):** The scope of the commit, e.g., `users`, `posts`, `auth`, `db`. If applicable, specify the module or part of the system affected.
- **`<short summary>`:** A very brief summary (under 50 characters) of the change.
- **`<body>` (optional):** A more detailed explanation of the change. Use imperative mood and explain the "why" and "how".
- **`<footer>` (optional):** Reference issues (e.g., `Closes #123`, `Fixes #456`).

**Examples:**

```sh
 git add .
 git commit -m "feat(auth): add JWT login support"
 # or
 git commit -m "fix(posts): handle null title in post creation"
```

### Pull Request (PR) Guidelines

When your feature or bugfix branch is ready, create a Pull Request (PR) against the `main` branch.

1.  **Rebase before PR:** Ensure your branch is up-to-date with `main` by rebasing (do not merge `main` into your branch).
    ```bash
    git checkout main
    git pull origin main
    git checkout <your-branch-name>
    git rebase main
    ```
    Resolve any conflicts during the rebase.
2.  **Meaningful Title:** Use a descriptive PR title, ideally following the Conventional Commits style.
3.  **Clear Description:** Provide a comprehensive description of the changes.
    - What problem does this PR solve?
    - How does it solve it?
    - Any specific areas for review.
    - Screenshots or GIFs if applicable (for UI changes).
4.  **Link Issues:** Reference any related issues (e.g., `Closes #XYZ`).
5.  **Request Review:** Request reviews from relevant team members.
6.  **Address Feedback:** Be responsive to feedback and make necessary adjustments. Once approved, your PR can be merged.

## 📄 API Documentation <a name = "api_documentation"></a>

API documentation is automatically generated using SpringDoc OpenAPI and can be accessed at:

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Docs:** `http://localhost:8080/v3/api-docs`

## ⚙️ Configuration <a name = "configuration"></a>

The application's configuration is primarily managed through `application.yml` and environment variables. Key properties to consider:

- **Database Connections:**
  - `spring.datasource.*` for PostgreSQL
  - `spring.data.mongodb.*` for MongoDB
  - `spring.data.redis.*` for Redis
- **Security:**
  - JWT configurations.
- **LiveKit Integration:**
  - `livekit.api-key`, `livekit.api-secret`, `livekit.url` for real-time video/audio.
- **Email Service:**
  - SMTP server details for `org.apache.commons.mail`.

## 📦 Building and Deployment <a name = "building_and_deployment"></a>

### Building a Docker Image (Spring Boot Plugin)

The `pom.xml` is configured to build a Docker image using the Spring Boot Maven Plugin.

```bash
mvn spring-boot:build-image
```

### Or Build with Dockerfile

```bash
docker build -t unify-backend .
```

### Run the Docker Container

```bash
docker run -p 8000:8000 unify-backend
```
