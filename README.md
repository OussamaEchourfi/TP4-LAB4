# TP4 / LAB4 : Stratégies de Mapping d'Héritage avec JPA et Hibernate

Ce projet a pour objectif d'explorer, d'implémenter et de tester les trois stratégies majeures de mapping d'héritage en **JPA / Hibernate** avec une base de données en mémoire **H2**, des validations via **Hibernate Validator** et une suite de tests unitaires sous **JUnit 4**.

---

## 📋 Table des Matières

- [1. Technologies Utilisées](#1-technologies-utilisées)
- [2. Les Trois Stratégies d'Héritage](#2-les-trois-stratégies-dhéritage)
  - [A. SINGLE_TABLE (Une seule table)](#a-single_table-une-seule-table)
  - [B. JOINED (Table par classe avec jointure)](#b-joined-table-par-classe-avec-jointure)
  - [C. TABLE_PER_CLASS (Table par classe concrète)](#c-table_per_class-table-par-classe-concrète)
- [3. Extraits de Résultats et Captures d'Écran](#3-extraits-de-résultats-et-captures-décran)
  - [Résultat 1 : Table unique `VEHICULES` (SINGLE_TABLE)](#résultat-1--table-unique-vehicules-single_table)
  - [Résultat 2 : Jointure `EMPLOYES` & `DEVELOPPEURS` (JOINED)](#résultat-2--jointure-employes--developpeurs-joined)
  - [Résultat 3 : Table `LIVRES` (TABLE_PER_CLASS)](#résultat-3--table-livres-table_per_class)
  - [Résultat 4 : Table `ELECTRONIQUES` (TABLE_PER_CLASS)](#résultat-4--table-electroniques-table_per_class)
  - [Résultat 5 : Exécution des Tests Unitaires JUnit (34/34 Tests Réussis)](#résultat-5--exécution-des-tests-unitaires-junit-3434-tests-réussis)
- [4. Instructions d'Exécution](#4-instructions-dexécution)

---

## 1. Technologies Utilisées

- **Java 21**
- **Maven** (Gestionnaire de dépendances et de build)
- **JPA 2.2** (`javax.persistence-api`)
- **Hibernate Core 5.6.5.Final** (Implémentation ORM)
- **Hibernate Validator 6.2.0.Final** (Validation des contraintes Bean Validation)
- **H2 Database 2.1.214** (Base de données relationnelle embarquée avec console Web)
- **JUnit 4.13.2** (Tests unitaires)

---

## 2. Les Trois Stratégies d'Héritage

### A. SINGLE_TABLE (Une seule table)
- **Principe :** Toutes les classes de la hiérarchie sont persistées dans une seule et unique table (`VEHICULES`).
- **Modèle :** `Vehicule` (classe mère abstraite), `Voiture` et `Moto` (sous-classes).
- **Mécanisme :** Utilisation d'une colonne discriminante (`type_vehicule`) pour distinguer le type de chaque enregistrement.
- **Avantages / Inconvénients :**
  - ✅ Excellentes performances (pas de jointures nécessaires).
  - ⚠️ Les colonnes spécifiques aux sous-classes doivent obligatoirement accepter les valeurs `NULL`.

### B. JOINED (Table par classe avec jointure)
- **Principe :** Chaque classe (abstraite ou concrète) possède sa propre table dans la base de données (`EMPLOYES`, `DEVELOPPEURS`, `MANAGERS`).
- **Modèle :** `Employe` (classe mère), `Developpeur` et `Manager` (sous-classes).
- **Mécanisme :** Les tables des sous-classes partagent la même clé primaire que la table mère (`id`), agissant également comme clé étrangère.
- **Avantages / Inconvénients :**
  - ✅ Schéma relationnel normalisé (3FN) sans colonnes `NULL` inutiles.
  - ⚠️ Nécessite des requêtes avec `JOIN` pour récupérer les entités complètes.

### C. TABLE_PER_CLASS (Table par classe concrète)
- **Principe :** Chaque sous-classe concrète possède une table autonome contenant tous les attributs (hérités et spécifiques) : `LIVRES` et `ELECTRONIQUES`.
- **Modèle :** `Produit` (classe mère abstraite), `Livre` et `Electronique` (sous-classes concrètes).
- **Avantages / Inconvénients :**
  - ✅ Pas de colonnes `NULL`, structure indépendante par table.
  - ⚠️ Les requêtes polymorphiques sur la classe mère (`Produit`) nécessitent des `UNION` coûteux.

---

## 3. Extraits de Résultats et Captures d'Écran

Les données ont été insérées en exécutant la classe [App.java](src/main/java/com/example/App.java) puis visualisées depuis la console H2 (`http://localhost:8082`).

### Résultat 1 : Table unique `VEHICULES` (SINGLE_TABLE)
La table `VEHICULES` regroupe l'ensemble des voitures et des motos avec la colonne discriminante `TYPE_VEHICULE`. Les colonnes non applicables contiennent `null`.

```sql
SELECT * FROM VEHICULES;
```

![Console H2 - SINGLE_TABLE VEHICULES](src/main/Screenshot%202026-10-08%20180315.png)

---

### Résultat 2 : Jointure `EMPLOYES` & `DEVELOPPEURS` (JOINED)
La requête effectue une jointure entre la table de base `EMPLOYES` et la table spécialisée `DEVELOPPEURS` sur leur identifiant commun `ID`.

```sql
SELECT e.*, d.LANGAGE, d.SPECIALITE 
FROM EMPLOYES e 
JOIN DEVELOPPEURS d ON e.ID = d.ID;
```

![Console H2 - JOINED EMPLOYES et DEVELOPPEURS](src/main/Screenshot%202026-10-08%20180335.png)

---

### Résultat 3 : Table `LIVRES` (TABLE_PER_CLASS)
La table `LIVRES` contient à la fois les attributs hérités de `Produit` (`ID`, `NOM`, `PRIX`, `DESCRIPTION`, `DATECREATION`) et ses attributs propres (`AUTEUR`, `EDITEUR`, `ISBN`, `NOMBREPAGES`).

```sql
SELECT * FROM LIVRES;
```

![Console H2 - TABLE_PER_CLASS LIVRES](src/main/Screenshot%202026-10-08%20180350.png)

---

### Résultat 4 : Table `ELECTRONIQUES` (TABLE_PER_CLASS)
De manière analogue, la table `ELECTRONIQUES` contient l'ensemble complet des attributs pour les produits électroniques sans aucune colonne `NULL`.

```sql
SELECT * FROM ELECTRONIQUES;
```

![Console H2 - TABLE_PER_CLASS ELECTRONIQUES](src/main/Screenshot%202026-10-08%20180403.png)

---

### Résultat 5 : Exécution des Tests Unitaires JUnit (34/34 Tests Réussis)
L'ensemble des 34 tests unitaires couvrant les opérations CRUD, les requêtes polymorphiques et les validations Bean Validation sur les 3 stratégies s'exécutent avec succès (`BUILD SUCCESS`).

```bash
mvn clean test
```

![Maven Test Results - Build Success](src/main/Screenshot%202026-10-08%20181608.png)

---
