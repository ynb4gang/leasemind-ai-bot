# LeaseMind AI -  Telegram Assistant
![Java](https://img.shields.io/badge/Java-21-orange) ![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![Docker](https://img.shields.io/badge/Docker-Containerized-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

AI-powered Telegram bot that helps users ask questions, receive
AI-generated answers, and interact with expert knowledge through a
structured command system.

The project is built with **Spring Boot**, **Spring AI**, and
**PostgreSQL**, and is designed to run inside **Docker containers** for
simple cloud deployment.

------------------------------------------------------------------------

# Features

-   AI-powered answers via LLM
-   Telegram Bot integration
-   Command-based interaction
-   Event-driven architecture
-   User session state management
-   Localization support (EN / RU)
-   Liquibase database migrations
-   Docker container deployment

------------------------------------------------------------------------

# Tech Stack

### Backend

-   Java 21
-   Spring Boot
-   Spring AI
-   Spring Data JDBC

### Infrastructure

-   PostgreSQL
-   Liquibase
-   Docker
-   Docker Compose

### AI

-   OpenRouter (LLM Gateway)

### Messaging

-   Telegram Bot API

------------------------------------------------------------------------

# System Architecture

``` mermaid
flowchart LR

User[Telegram User]
Telegram[Telegram Servers]

Bot[Spring Boot Bot]
Handler[Command Handler]

Commands[Command Layer]

Events[Application Events]
Listener[Event Listener]

AI[AI Service]
LLM[OpenRouter LLM]

DB[(PostgreSQL)]
Liquibase[Liquibase]

Docker[Docker Container]
Cloud[Cloud Server]

User --> Telegram
Telegram --> Bot

Bot --> Handler
Handler --> Commands

Commands --> Events
Events --> Listener

Listener --> Telegram

Commands --> AI
AI --> LLM

Commands --> DB
Liquibase --> DB

Bot --> Docker
Docker --> Cloud
```

## Level 1  System Context

``` mermaid
flowchart LR

User[Telegram User]

Telegram[Telegram Platform]

LeaseMind[LeaseMind AI System]

LLM[LLM Provider]

User --> Telegram
Telegram --> LeaseMind
LeaseMind --> LLM
LeaseMind --> Telegram
```

------------------------------------------------------------------------

## Level 2  Container Diagram

``` mermaid
flowchart LR

User[Telegram User]

Telegram[Telegram API]

Bot[Spring Boot Telegram Bot]

AI[AI Service]

Events[Event System]

DB[(PostgreSQL)]

LLM[OpenRouter / LLM]

User --> Telegram
Telegram --> Bot

Bot --> Events
Events --> AI

AI --> LLM

Bot --> DB
```

------------------------------------------------------------------------

## Level 3  Component Diagram

``` mermaid
flowchart LR

MainBot[MainBot]

CommandHandler[CommandHandler]

Commands[Command Layer]

Events[Application Events]

Listener[EventsListener]

Services[Service Layer]

DB[(PostgreSQL)]

MainBot --> CommandHandler
CommandHandler --> Commands

Commands --> Events
Events --> Listener

Commands --> Services
Services --> DB
```

------------------------------------------------------------------------

# Project Structure

    spring-bot
    │
    ├── src
    │   ├── main
    │   │   ├── java/com.spring_bot
    │   │   │
    │   │   ├── bot
    │   │   │   ├── MainBot.java
    │   │   │
    │   │   ├── commands
    │   │   │   ├── AboutCommand.java
    │   │   │   ├── AskCommand.java
    │   │   │   ├── AskExpertCommand.java
    │   │   │   ├── ExpertsCommand.java
    │   │   │   ├── FactCommand.java
    │   │   │   ├── LanguageCommand.java
    │   │   │   ├── StartCommand.java
    │   │   │   ├── QuestionHandlerCommand.java
    │   │   │
    │   │   ├── config
    │   │   │   ├── TelegramConfig.java
    │   │   │   ├── LocaleConfig.java
    │   │   │
    │   │   ├── events
    │   │   │   ├── EventsListener.java
    │   │   │   ├── MessageEvent.java
    │   │   │   ├── MemberEvent.java
    │   │   │
    │   │   ├── model
    │   │   │   ├── UserSession.java
    │   │   │   ├── UserState.java
    │   │   │
    │   │   ├── repo
    │   │   │   ├── UserSessionRepository.java
    │   │   │
    │   │   ├── rest.controller
    │   │   │   ├── AiController.java
    │   │   │
    │   │   ├── service
    │   │   │   ├── AiService.java
    │   │   │   ├── KeyboardService.java
    │   │   │   ├── LocalizationService.java
    │   │   │   ├── MessageTrackerService.java
    │   │   │   ├── UserSessionService.java
    │   │
    │   └── resources
    │       ├── application.properties
    │       ├── messages_en.properties
    │       ├── messages_ru.properties
    │       │
    │       └── db.changelog
    │           ├── db.changelog-master.xml
    │           └── initial.xml
    │
    ├── Dockerfile
    ├── docker-compose.yml
    └── README.md

------------------------------------------------------------------------

# Deployment

The application is containerized and ready for cloud deployment.

### Build image

``` bash
docker build -t spring-bot .
```

### Run containers

``` bash
docker-compose up -d
```

------------------------------------------------------------------------

# Environment Variables

    TELEGRAM_BOT_TOKEN=

    OPENROUTER_API_KEY=

    SPRING_DATASOURCE_URL=
    SPRING_DATASOURCE_USERNAME=
    SPRING_DATASOURCE_PASSWORD=

------------------------------------------------------------------------

# Example Bot Interaction

User:

    What are the main responsibilities of a tenant?

Bot:

    A tenant is typically responsible for paying rent on time, maintaining the property,
    and following the terms specified in the lease agreement.

------------------------------------------------------------------------

# Future Improvements

-   Vector database for knowledge retrieval (RAG)
-   Admin dashboard
-   Conversation memory
-   Analytics
-   Multi-model LLM routing

------------------------------------------------------------------------

# License

MIT
