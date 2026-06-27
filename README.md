# 🚀 EnginFlow - Backend

## 📖 Description

**EnginFlow** est une application web développée dans le cadre d'un stage d'initiation réalisé au sein de **Digital Manufacturing – OCP Jorf Lasfar**.

Ce dépôt contient le **backend** de l'application, développé avec **Spring Boot**. Il expose une API REST sécurisée permettant de gérer l'ensemble du processus d'affectation des engins industriels.

Le backend est responsable de :

* l'authentification et l'autorisation des utilisateurs ;
* la gestion des demandes d'engins ;
* la gestion des pièces jointes ;
* le workflow de validation ;
* la traçabilité des actions ;
* la communication avec la base de données PostgreSQL.

---

# ✨ Fonctionnalités

### Authentification

* Connexion sécurisée avec Spring Security
* Gestion des rôles (DEMANDEUR / ADMINISTRATEUR)
* Chiffrement des mots de passe (BCrypt)

### Gestion des demandes

* Création d'une demande
* Consultation des demandes
* Validation
* Refus
* Modification
* Clôture automatique

### Gestion documentaire

* Téléversement des ordres de mission
* Téléversement des plans de maintenance
* Consultation des documents

### Historique

* Enregistrement de toutes les actions effectuées sur une demande

---

# 🏗️ Architecture

```
Frontend (React)
        │
        ▼
 REST Controllers
        │
        ▼
    Services
(Logique métier)
        │
        ▼
 Repositories (JPA)
        │
        ▼
 PostgreSQL
```

Les échanges entre le backend et le frontend sont réalisés via une **API REST** au format **JSON**.

---

# 🛠️ Technologies

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* PostgreSQL
* Maven
* Swagger / OpenAPI

---

# 📂 Structure du projet

```
src/main/java/com/stage/EnginFlow/

├── config/
├── controller/
├── dto/
├── model/
├── repository/
├── security/
├── service/
└── EnginFlowApplication.java
```

---

# 📚 Documentation API

Après le démarrage du projet :

```
http://localhost:8080/swagger-ui/index.html
```

---

# ⚙️ Installation

### Cloner le projet

```bash
git clone <URL_DU_DEPOT_BACKEND>
```

### Configurer PostgreSQL

Créer une base de données :

```sql
CREATE DATABASE engin_db;
```

Configurer ensuite le fichier :

```
src/main/resources/application.properties
```

### Lancer l'application

```bash
mvn spring-boot:run
```

---

# 🔐 Comptes de test

| Rôle           | Email                                     | Mot de passe |
| -------------- | ----------------------------------------- | ------------ |
| Administrateur | [admin@email.com](mailto:admin@email.com) | admin123     |
| Demandeur      | [jean@email.com](mailto:jean@email.com)   | 123456       |

---

# 🔗 Dépôt Frontend

Le frontend du projet est disponible ici :

**👉 https://github.com/nadamellouli01-jpg/enginflow-frontend**

---

# 👩‍💻 Auteur

**Nada MELLOULI**

Étudiante à l'École Supérieure de Technologie de Salé

Stage d'initiation – Digital Manufacturing – OCP Jorf Lasfar
