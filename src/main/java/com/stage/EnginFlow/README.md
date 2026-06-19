# EnginFlow - Backend

## 📋 Description

EnginFlow est une application de gestion des demandes d'affectation d'engins industriels (camions, grues, plateformes élévatrices) développée dans le cadre d'un stage de 1ère année chez **Digital Manufacturing** sur le site de **Jorf Lasfar**.

L'application permet de :

- Centraliser et formaliser les demandes d'engins
- Automatiser le workflow d'approbation
- Assurer une traçabilité complète des actions
- Gérer les rôles (DEMANDEUR / ADMINISTRATEUR)

---

## 🛠️ Stack Technique

| Composant | Technologie |
|-----------|-------------|
| **Backend** | Spring Boot 4.1.0 |
| **Base de données** | PostgreSQL 17 |
| **Sécurité** | Spring Security (Basic Auth) |
| **Build** | Maven |
| **Documentation API** | Swagger / OpenAPI |
| **Stockage fichiers** | Stockage local (dossier `uploads/`) |

---

## 📦 Prérequis

Avant de lancer l'application, assure-toi d'avoir installé :

- Java 17
- PostgreSQL 17
- Maven (ou utiliser le wrapper `mvnw` inclus)

---

## ⚙️ Installation et Configuration

### 1. Extraire le projet

```bash
cd C:\Users\Administrator\Documents\stage ocp\EnginFlow
```

### 2. Créer la base de données

Dans pgAdmin ou via psql :

```sql
CREATE DATABASE engin_db;
```

### 3. Configurer `application.properties`

Modifier le fichier `src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/engin_db
spring.datasource.username=postgres
spring.datasource.password=postgresql

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 4. Lancer l'application

```bash
.\mvnw spring-boot:run
```

L'application démarre sur `http://localhost:8080`

---

## 📚 Documentation API

### Swagger UI (Interface interactive)

```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI JSON (Documentation technique)

```
http://localhost:8080/v3/api-docs
```

---

## 👤 Comptes de test

| Utilisateur | Email | Mot de passe | Rôle |
|-------------|-------|--------------|------|
| Jean Dupont | `jean@email.com` | `123456` | DEMANDEUR |
| Sophie Martin | `sophie@email.com` | `123456` | DEMANDEUR |
| Admin Super | `admin@email.com` | `admin123` | ADMINISTRATEUR |

---

## 📋 Endpoints Principaux

### Espace Demandeur

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/demandes` | Créer une demande (avec 2 fichiers) |
| GET | `/api/demandes` | Voir ses demandes |
| GET | `/api/demandes/modifiees` | Notifications de modifications |
| PUT | `/api/demandes/{id}/utilisateur/accepter` | Accepter une modification |
| PUT | `/api/demandes/{id}/utilisateur/refuser` | Refuser une modification |
| GET | `/api/utilisateurs/me` | Profil utilisateur |

### Espace Administrateur

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/demandes` | Voir toutes les demandes (avec filtres) |
| GET | `/api/demandes/statistiques` | Dashboard statistiques |
| GET | `/api/demandes/{id}/historique` | Historique d'une demande |
| PUT | `/api/demandes/{id}/approuver` | Approuver une demande |
| PUT | `/api/demandes/{id}/refuser` | Refuser une demande |
| PUT | `/api/demandes/{id}/admin/modifier` | Modifier une demande |
| GET | `/api/utilisateurs` | Gestion des utilisateurs |

### Filtres disponibles (Admin)

| Filtre | Description | Exemple |
|--------|-------------|---------|
| `statut` | Statut de la demande | `EN_ATTENTE`, `APPROUVEE`, `REFUSEE`, `MODIFIEE`, `CLOTUREE` |
| `typeEngin` | Type d'engin | `CAMION`, `GRUE`, `PLATEFORME` |
| `dateCreation` | Date de création | `2026-06-19` |

---

## 🔄 Workflow d'approbation

```
1. DEMANDEUR crée une demande avec 2 fichiers → EN_ATTENTE
2. ADMIN traite la demande :
   - Approuve → APPROUVEE
   - Refuse avec motif → REFUSEE
   - Modifie → MODIFIEE
3. DEMANDEUR confirme la modification :
   - Accepte → APPROUVEE
   - Refuse → EN_ATTENTE
4. Clôture automatique après 24h → CLOTUREE
```

---

## 🏗️ Architecture

L'application suit une architecture en couches :

```
Controller (API REST)
    ↓
Service (Logique métier)
    ↓
Repository (Accès base de données)
    ↓
PostgreSQL (Base de données)
```

Les **DTO** (Data Transfer Objects) sont utilisés pour les échanges entre l'API et les clients afin de séparer les données exposées des entités JPA, garantissant ainsi la sécurité et l'intégrité des données.

---

## 📁 Structure du projet

```
EnginFlow/
├── src/
│   └── main/
│       ├── java/com/stage/EnginFlow/
│       │   ├── model/           # Entités JPA
│       │   ├── repository/      # Accès base de données
│       │   ├── service/         # Logique métier
│       │   ├── dto/             # Transfert de données
│       │   ├── controller/      # API REST
│       │   ├── security/        # Spring Security
│       │   └── config/          # Configurations (Swagger)
│       └── resources/
│           └── application.properties
├── uploads/                      # Fichiers uploadés
├── pom.xml                       # Dépendances Maven
├── README.md                     # Ce fichier
├── mvnw                          # Maven Wrapper
└── mvnw.cmd
```

---

## 🧪 Tests avec Postman

Importer la collection OpenAPI :

```
GET http://localhost:8080/v3/api-docs
→ Copier le JSON
→ Postman → Import → Paste JSON
```

---

## 🐛 Problèmes connus et solutions

### Erreur de connexion à PostgreSQL

```
spring.datasource.password=postgresql
```

→ Vérifier le mot de passe dans `application.properties`

### Les fichiers ne s'uploadent pas

→ Vérifier que le dossier `uploads/` existe et est accessible

### Erreur 403 Forbidden

→ Vérifier que l'utilisateur a le bon rôle (DEMANDEUR ou ADMINISTRATEUR)

### Erreur 401 Unauthorized

→ Vérifier les identifiants de connexion (Basic Auth)

---

## 📧 Contact

**Stagiaire :** Nada MELLOULI  
**Structure :** Digital Manufacturing - Jorf Lasfar  
**Email :** <nada.mellouli01@gmail.com>

---

## 📄 Licence

Projet réalisé dans le cadre d'un stage académique chez **Digital Manufacturing – Jorf Lasfar**.
