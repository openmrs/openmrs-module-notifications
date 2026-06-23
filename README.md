# OpenMRS Notifications Module

An OpenMRS platform module that provides a notification API for clinical alerts, lab results, and general system messages. It supports event-driven notification creation, a priority-based rule engine, recipient resolution, in-app delivery, and a REST API for client consumption.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Build](#build)
- [Installation](#installation)
- [Configuration](#configuration)
- [REST API](#rest-api)
- [Usage](#usage)
- [Testing](#testing)
- [License](#license)

## Overview

The Notifications Module exposes a persistent `Notification` model and a service layer that allows clinical and administrative applications to create, query, read, review, and archive notifications. Notifications can be triggered automatically by OpenMRS events (for example, lab results created via `Obs`) or created programmatically through the service API.

The module is designed to be extended: new notification types, rule engines, recipient resolvers, and delivery channels can be added without changing the core model.

## Features

- **Notification model** with type, priority, message, status, timestamps, and metadata.
- **Status lifecycle**: `UNREAD` → `READ` → `REVIEWED` → `ARCHIVED`.
- **Notification service** for create, read, review, archive, and query operations.
- **Hibernate-backed DAO** with queries by status, recipient, and patient.
- **Event-driven listener** for lab result events (`Obs` CREATED/UPDATED).
- **Priority rule engine** that decides when to send a notification or silently update existing data.
- **Recipient resolver** that determines the users who should receive a notification based on an order or patient.
- **Pluggable delivery service** with an in-app implementation included.
- **RESTful API** exposed through the OpenMRS Web Services REST module.
- **Liquibase changeset** for automatic database schema creation.

## Architecture

```text
┌─────────────────────────────────────────────────────────┐
│                       REST Layer                        │
│              NotificationResource                       │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                    Service Layer                        │
│              NotificationsServiceImpl                   │
│                   NotificationRuleEngine                │
│                   RecipientResolver                     │
│                   DeliveryService                       │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                      DAO Layer                          │
│                   NotificationsDao                      │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                   Persistence Layer                     │
│               notifications_notification table          │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                    Event Listener                       │
│               LabResultEventListener                    │
│              (subscribed via module activator)          │
└─────────────────────────────────────────────────────────┘
```

### Key Components

- `Notification` — persistent domain object with `type`, `priority`, `message`, `status`, `readAt`, `reviewedBy`, `reviewedAt`, and `metadata`.
- `NotificationsService` / `NotificationsServiceImpl` — business logic and lifecycle transitions.
- `NotificationsDao` — Hibernate-based data access.
- `NotificationRuleEngine` — evaluates `LabResultContext` and returns a `NotificationDecision` (send or silent update, with priority).
- `RecipientResolver` — resolves the target users for a notification.
- `DeliveryService` / `InAppDeliveryServiceImpl` — delivers notifications to the configured channel.
- `LabResultEventListener` — processes `Obs` events using the rule engine and recipient resolver.
- `NotificationsActivator` — subscribes/unsubscribes the event listener on module start/shutdown.

## Project Structure

```text
openmrs-module-notifications/
├── api/
│   ├── src/main/java/org/openmrs/module/notifications/
│   │   ├── Notification.java
│   │   ├── NotificationsActivator.java
│   │   ├── NotificationsConfig.java
│   │   ├── api/
│   │   │   ├── NotificationsService.java
│   │   │   ├── DeliveryService.java
│   │   │   ├── RecipientResolver.java
│   │   │   ├── dao/NotificationsDao.java
│   │   │   ├── impl/NotificationsServiceImpl.java
│   │   │   ├── impl/InAppDeliveryServiceImpl.java
│   │   │   ├── listener/LabResultEventListener.java
│   │   │   └── rules/
│   │   │       ├── NotificationRuleEngine.java
│   │   │       ├── LabResultContext.java
│   │   │       └── NotificationDecision.java
│   ├── src/main/resources/
│   │   ├── moduleApplicationContext.xml
│   │   └── liquibase.xml
│   └── src/test/java/org/openmrs/module/notifications/
│       ├── NotificationsServiceTest.java
│       ├── dao/NotificationsDaoTest.java
│       └── api/rules/NotificationRuleEngineTest.java
├── omod/
│   ├── src/main/java/org/openmrs/module/notifications/web/resource/
│   │   └── NotificationResource.java
│   └── src/main/resources/config.xml
├── pom.xml
└── README.md
```

## Prerequisites

- JDK 8 or higher
- Maven 3.x
- OpenMRS Platform 2.0.0+ (module targets the `referenceapplication:2.4` BOM)
- Required modules (declared in `config.xml`):
  - `org.openmrs.module.webservices.rest` (REST Web Services)
  - `org.openmrs.module.event` (Event Module) version 2.8.0

## Build

Clone the repository and run:

```bash
cd openmrs-module-notifications
mvn clean install
```

To skip the integration tests (which require an in-memory OpenMRS context):

```bash
mvn clean install -DskipTests
```

## Installation

1. Build the module as described above.
2. Copy the generated OMOD file to the OpenMRS modules directory:
   ```bash
   cp omod/target/notifications-1.0.0-SNAPSHOT.omod ~/.OpenMRS/modules/
   ```
3. Restart OpenMRS.
4. Verify that the `notifications_notification` table is created by Liquibase.

## Configuration

### Required Modules

The module declares `webservices.rest` and `event` as required modules in `omod/src/main/resources/config.xml`:

```xml
<require_modules>
    <require_module>org.openmrs.module.webservices.rest</require_module>
    <require_module version="2.8.0">org.openmrs.module.event</require_module>
</require_modules>
```

### Event Listener Subscription

The `NotificationsActivator` subscribes the `LabResultEventListener` to `Obs` CREATED and UPDATED events on module startup, and unsubscribes on shutdown. The listener runs inside an OpenMRS daemon thread to ensure a valid authentication context.

### Database Schema

Liquibase creates the `notifications_notification` table with columns matching the `Notification` model: `type`, `priority`, `message`, `status`, `read_at`, `reviewed_by`, `reviewed_at`, `metadata`, and the standard OpenMRS audit/voiding columns.

## REST API

The module exposes a `Notification` resource under the REST Web Services module. Default resource path: `/ws/rest/v1/notification` (or as configured by the `resource` metadata).

### Supported Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET`  | `/ws/rest/v1/notification` | List notifications; filter by `recipient` UUID and/or `status` query parameters. |
| `GET`  | `/ws/rest/v1/notification/{uuid}` | Fetch a single notification by UUID. |
| `POST` | `/ws/rest/v1/notification` | Create a new notification. |
| `POST` | `/ws/rest/v1/notification/{uuid}/review` | Mark a notification as reviewed. |
| `PUT`  | `/ws/rest/v1/notification/{uuid}` | Update a notification; setting `status` to `READ`, `REVIEWED`, or `ARCHIVED` performs the corresponding lifecycle transition. |

### Query Parameters

- `recipient` — UUID of the user who should receive the notification.
- `status` — one of `UNREAD`, `READ`, `REVIEWED`, `ARCHIVED`.

### Example Payload (Create)

```json
{
  "type": "LAB_RESULT",
  "priority": "HIGH",
  "message": "Critical potassium value detected",
  "status": "UNREAD",
  "recipient": "<user-uuid>",
  "patient": "<patient-uuid>",
  "metadata": "{\"obsUuid\":\"<obs-uuid>\"}"
}
```

## Usage

### Creating a Notification Programmatically

```java
import org.openmrs.User;
import org.openmrs.Patient;
import org.openmrs.module.notifications.Notification;
import org.openmrs.module.notifications.api.NotificationsService;
import org.openmrs.api.context.Context;

NotificationsService service = Context.getService(NotificationsService.class);

Notification notification = new Notification();
notification.setType(Notification.Type.LAB_RESULT);
notification.setPriority(Notification.Priority.HIGH);
notification.setMessage("Critical potassium value detected");
notification.setRecipient(new User(1));
notification.setPatient(new Patient(1));
notification.setMetadata("{\"obsUuid\":\"abc-123\"}");

service.createNotification(notification);
```

### Marking a Notification as Reviewed

```java
Notification notification = service.getNotificationByUuid(uuid);
User reviewer = Context.getAuthenticatedUser();
service.markAsReviewed(notification, reviewer);
```

### Querying Notifications

```java
// All unread notifications
List<Notification> unread = service.getNotificationsByStatus(Notification.Status.UNREAD);

// Unread notifications for a specific user
List<Notification> forUser = service.getNotificationsByRecipient(user, Notification.Status.UNREAD);

// Notifications for a patient
List<Notification> forPatient = service.getNotificationsByPatient(patient);
```

### Lab Result Event Flow

1. An `Obs` is created or updated in OpenMRS.
2. The Event Module fires the event to `LabResultEventListener`.
3. The listener builds a `LabResultContext` from the observation/order.
4. `NotificationRuleEngine` evaluates the context and returns a `NotificationDecision`.
5. If the decision is to send, `RecipientResolver` determines the users to notify.
6. A notification is persisted via `NotificationsService` and delivered via `DeliveryService`.

## Testing

The module contains unit tests for the core components:

- `NotificationsServiceTest` — unit tests for `NotificationsServiceImpl` using Mockito.
- `NotificationRuleEngineTest` — unit tests for the rule engine decision tree.
- `NotificationsDaoTest` — context-sensitive integration test verifying Hibernate persistence and queries.

Run the tests with:

```bash
mvn test
```

Run a specific test class:

```bash
mvn test -Dtest=NotificationsServiceTest
mvn test -Dtest=NotificationRuleEngineTest
mvn test -Dtest=NotificationsDaoTest
```

## License

This module is licensed under the Mozilla Public License 2.0 (MPL 2.0). See the source files for the full license header.
