# Java Scaffold Backend

This is the Spring Boot scaffold application providing a REST API for student management (“Aluno”) and file handling (“Arquivo”). It integrates new JPA-based persistence and a legacy-database DAO layer for full feature coverage.

## Table of Contents

- [Getting Started](#getting-started)  
- [Configuration](#configuration)  
- [API Endpoints](#api-endpoints)  
- [Migration of Legacy Aluno Functionality](#migration-of-legacy-aluno-functionality)  
  - [Overview](#overview)  
  - [Migration Steps](#migration-steps)  
  - [Aluno Endpoints](#aluno-endpoints)  
  - [Service & Repository Layers](#service--repository-layers)  
  - [Legacy DAO Configuration](#legacy-dao-configuration)  
- [H2 Console](#h2-console)  
- [Tests](#tests)  

---

## Getting Started

1. Clone the repository.  
2. Configure `application.properties` with legacy DB credentials.  
3. Run `mvn spring-boot:run`.  

## Configuration

All DB and JPA settings live in `src/main/resources/application.properties`.  
- **H2 in-memory** for local scaffold data.  
- **Legacy Oracle/SQLServer** for historical Aluno tables.

