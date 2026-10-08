package com.example;

import com.example.model.joined.Developpeur;
import com.example.model.joined.Employe;
import com.example.model.joined.Manager;
import com.example.model.singletable.Moto;
import com.example.model.singletable.Vehicule;
import com.example.model.singletable.Voiture;
import com.example.model.tableperclass.Electronique;
import com.example.model.tableperclass.Livre;
import com.example.model.tableperclass.Produit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import org.h2.tools.Server;

public class App {
    public static void main(String[] args) {

        Server h2WebServer = null;
        try {
            // ─── Démarrage de la console web H2 ───────────────────────────
            h2WebServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.println("\n╔══════════════════════════════════════════════════╗");
            System.out.println("║  Console H2 démarrée : http://localhost:8082     ║");
            System.out.println("║  JDBC URL : jdbc:h2:./testdb;AUTO_SERVER=TRUE    ║");
            System.out.println("║  User : sa   |   Password : (vide)               ║");
            System.out.println("╚══════════════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("Impossible de démarrer le serveur H2 : " + e.getMessage());
        }

        // Création de l'EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-inheritance");

        try {
            // Test de la stratégie SINGLE_TABLE
            System.out.println("\n=== Test de la stratégie SINGLE_TABLE ===");
            testSingleTable(emf);

            // Test de la stratégie JOINED
            System.out.println("\n=== Test de la stratégie JOINED ===");
            testJoined(emf);

            // Test de la stratégie TABLE_PER_CLASS
            System.out.println("\n=== Test de la stratégie TABLE_PER_CLASS ===");
            testTablePerClass(emf);

            // ─── Pause : laisser l'utilisateur explorer la console H2 ──────
            System.out.println("\n╔══════════════════════════════════════════════════╗");
            System.out.println("║  Données insérées ! Ouvrez la console H2 :       ║");
            System.out.println("║  http://localhost:8082                           ║");
            System.out.println("║  Appuyez sur ENTRÉE pour fermer l'application.  ║");
            System.out.println("╚══════════════════════════════════════════════════╝");
            new Scanner(System.in).nextLine();

        } finally {
            emf.close();
            if (h2WebServer != null) {
                h2WebServer.stop();
                System.out.println("Console H2 fermée.");
            }
        }
    }

    private static void testSingleTable(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Création des véhicules
            System.out.println("Création des véhicules...");

            Voiture voiture1 = new Voiture(
                    "Renault", "Clio", LocalDate.of(2020, 5, 15), 15000.0,
                    5, true, "Essence"
            );

            Voiture voiture2 = new Voiture(
                    "Peugeot", "308", LocalDate.of(2019, 3, 10), 18000.0,
                    5, true, "Diesel"
            );

            Moto moto1 = new Moto(
                    "Honda", "CBR", LocalDate.of(2021, 7, 20), 12000.0,
                    600, "Manuelle"
            );

            // Persistance des véhicules
            em.persist(voiture1);
            em.persist(voiture2);
            em.persist(moto1);

            em.getTransaction().commit();
            System.out.println("Véhicules créés avec succès !");

            // Requêtes polymorphiques
            em.clear();

            System.out.println("\nRécupération de tous les véhicules :");
            List<Vehicule> vehicules = em.createQuery("SELECT v FROM Vehicule v", Vehicule.class)
                    .getResultList();
            vehicules.forEach(System.out::println);

            System.out.println("\nRécupération de toutes les voitures :");
            List<Voiture> voitures = em.createQuery("SELECT v FROM Voiture v", Voiture.class)
                    .getResultList();
            voitures.forEach(System.out::println);

            System.out.println("\nRécupération de toutes les motos :");
            List<Moto> motos = em.createQuery("SELECT m FROM Moto m", Moto.class)
                    .getResultList();
            motos.forEach(System.out::println);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static void testJoined(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Création des employés
            System.out.println("Création des employés...");

            Developpeur dev1 = new Developpeur(
                    "Dupont", "Jean", "jean.dupont@example.com", LocalDate.of(2019, 5, 15),
                    "Java", "Backend", 5
            );

            Developpeur dev2 = new Developpeur(
                    "Martin", "Sophie", "sophie.martin@example.com", LocalDate.of(2020, 3, 10),
                    "JavaScript", "Frontend", 3
            );

            Manager manager1 = new Manager(
                    "Dubois", "Pierre", "pierre.dubois@example.com", LocalDate.of(2018, 7, 20),
                    "IT", 10, 5000.0
            );

            // Persistance des employés
            em.persist(dev1);
            em.persist(dev2);
            em.persist(manager1);

            em.getTransaction().commit();
            System.out.println("Employés créés avec succès !");

            // Requêtes polymorphiques
            em.clear();

            System.out.println("\nRécupération de tous les employés :");
            List<Employe> employes = em.createQuery("SELECT e FROM Employe e", Employe.class)
                    .getResultList();
            employes.forEach(System.out::println);

            System.out.println("\nRécupération de tous les développeurs :");
            List<Developpeur> devs = em.createQuery("SELECT d FROM Developpeur d", Developpeur.class)
                    .getResultList();
            devs.forEach(System.out::println);

            System.out.println("\nRécupération de tous les managers :");
            List<Manager> managers = em.createQuery("SELECT m FROM Manager m", Manager.class)
                    .getResultList();
            managers.forEach(System.out::println);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static void testTablePerClass(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Création des produits
            System.out.println("Création des produits...");

            Livre livre1 = new Livre(
                    "Le Seigneur des Anneaux", 25.99, "Un classique de fantasy",
                    "J.R.R. Tolkien", "978-2075134729", 1200, "Pocket"
            );

            Livre livre2 = new Livre(
                    "Harry Potter", 19.99, "Roman fantastique pour jeunes",
                    "J.K. Rowling", "978-2070643028", 800, "Gallimard"
            );

            Electronique elec1 = new Electronique(
                    "Smartphone Galaxy S21", 899.99, "Smartphone haut de gamme",
                    "Samsung", "S21", 24, "Écran 6.2\", 128Go, 8Go RAM"
            );

            // Persistance des produits
            em.persist(livre1);
            em.persist(livre2);
            em.persist(elec1);

            em.getTransaction().commit();
            System.out.println("Produits créés avec succès !");

            // Requêtes polymorphiques
            em.clear();

            System.out.println("\nRécupération de tous les produits :");
            List<Produit> produits = em.createQuery("SELECT p FROM Produit p", Produit.class)
                    .getResultList();
            produits.forEach(System.out::println);

            System.out.println("\nRécupération de tous les livres :");
            List<Livre> livres = em.createQuery("SELECT l FROM Livre l", Livre.class)
                    .getResultList();
            livres.forEach(System.out::println);

            System.out.println("\nRécupération de tous les produits électroniques :");
            List<Electronique> elecs = em.createQuery("SELECT e FROM Electronique e", Electronique.class)
                    .getResultList();
            elecs.forEach(System.out::println);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}