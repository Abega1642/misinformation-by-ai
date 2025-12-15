# UNFAKED API

> **Detecting deepfakes. Restoring trust.**

UNFAKED is a detection platform designed to identify **deepfakes and manipulated content** across **videos, images, audio, and textual information**. This repository contains the **backend API** powering UNFAKED, developed by the **PoopOverflow team**.

The goal of this project is to provide a **robust, secure, and scalable backend** capable of handling media processing, verification workflows, and security‑critical operations in a production‑grade environment.

---

## 🧠 What This Repository Is About

This repository hosts the **Spring Boot backend** of UNFAKED. It exposes APIs for:

* Media ingestion and processing (video, image, audio)
* Deepfake detection orchestration
* Textual information analysis
* Secure authentication and authorization
* Asynchronous processing and messaging
* Auditing, monitoring, and health checks

The backend is designed with **security, reliability, and maintainability** as first‑class concerns.

---

## 🛠️ Tech Stack (Backend)

**Core**

* Java **21**
* Spring Boot **3.5.x**
* Gradle

**Architecture & Infrastructure**

* Spring Web, Security, Validation
* Spring Data JPA + PostgreSQL
* Flyway for database migrations
* RabbitMQ (AMQP)
* AWS S3 (media storage)
* FFmpeg / Jaffree (media processing)

**Security**

* Spring Security
* JWT (jjwt)
* Bouncy Castle
* OWASP Encoder

**Quality & Tooling**

* Qodana & CodeQL (code review, security analysis)
* google‑java‑format (code formatting)
* Testcontainers (PostgreSQL, RabbitMQ, LocalStack)
* WireMock, GreenMail

**API Documentation**

* OpenAPI / Swagger (specification available in `/doc`)

---

## 📐 Code Quality & Standards

This project enforces **high engineering standards**:

* Automated static analysis (security & quality)
* Consistent formatting via `google-java-format`
* Strong test coverage with isolated, containerized environments
* Clear separation of concerns and layered architecture

Formatting is standardized and automated via the provided formatting script.

---

## 📂 Repository Scope

* **This repository contains only the backend API**
* Front‑end and mobile clients are maintained separately
* Installation and contribution details are intentionally not public

> 📩 **Interested in contributing or collaborating?** Please contact the team directly.

---

## 👥 PoopOverflow Team

**Abegà Razafindratelo**
Java Back‑Ops Engineer
GitHub: [Abega1642](https://github.com/Abega1642)

**Tsantniaina Kyle Raokotoarison**
Front‑End & Mobile Developer
GitHub: [tsanta22Kyle](https://github.com/tsanta22Kyle)

**Tiavina Ulrich Andriamamivony**
Front‑End & Sec‑Ops Developer
GitHub: [Tiavina-Andriamamivony](https://github.com/Tiavina-Andriamamivony)

**Mihago Tohiaina Ny Mendrika**
Front‑End Developer
GitHub: *TBA*

---

## 🚀 Project Vision

UNFAKED aims to become a **trusted verification layer** in a world increasingly affected by synthetic and manipulated content. This backend is built to evolve with new detection models, new media formats, and growing security challenges.

---

<sub>© PoopOverflow Team — UNFAKED</sub>
