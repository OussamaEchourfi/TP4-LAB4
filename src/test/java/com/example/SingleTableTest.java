package com.example;

import com.example.model.singletable.Moto;
import com.example.model.singletable.Vehicule;
import com.example.model.singletable.Voiture;
import org.junit.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests JUnit pour la stratégie d'héritage SINGLE_TABLE.
 * Une seule table "vehicules" contient tous les types de véhicules.
 * Une colonne discriminante "type_vehicule" distingue les sous-types.
 */
public class SingleTableTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    @BeforeClass
    public static void initFactory() {
        emf = Persistence.createEntityManagerFactory("hibernate-inheritance-test");
    }

    @AfterClass
    public static void closeFactory() {
        if (emf != null) emf.close();
    }

    @Before
    public void initEm() {
        em = emf.createEntityManager();
    }

    @After
    public void closeEm() {
        if (em != null) em.close();
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests CREATE (Persistance)
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testPersisterVoiture() {
        em.getTransaction().begin();

        Voiture v = new Voiture("Renault", "Clio",
                LocalDate.of(2021, 1, 10), 15000.0, 5, true, "Essence");
        em.persist(v);

        em.getTransaction().commit();

        assertNotNull("L'ID doit être généré après persist", v.getId());
    }

    @Test
    public void testPersisterMoto() {
        em.getTransaction().begin();

        Moto m = new Moto("Honda", "CBR600",
                LocalDate.of(2022, 3, 15), 9000.0, 600, "Manuelle");
        em.persist(m);

        em.getTransaction().commit();

        assertNotNull("L'ID de la moto doit être généré", m.getId());
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests READ (Requêtes polymorphiques)
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testRequetePolymorphique_TousLesVehicules() {
        // Insertion préalable
        em.getTransaction().begin();
        em.persist(new Voiture("Peugeot", "308", LocalDate.of(2020, 6, 1), 18000.0, 5, false, "Diesel"));
        em.persist(new Moto("Yamaha", "MT-07", LocalDate.of(2021, 9, 20), 7500.0, 700, "Manuelle"));
        em.getTransaction().commit();
        em.clear();

        // Requête sur la classe parente → doit ramener TOUS les véhicules
        List<Vehicule> tous = em.createQuery("SELECT v FROM Vehicule v", Vehicule.class).getResultList();

        assertFalse("La liste des véhicules ne doit pas être vide", tous.isEmpty());
        // Vérifie qu'on trouve bien les deux sous-types
        boolean aVoiture = tous.stream().anyMatch(v -> v instanceof Voiture);
        boolean aMoto    = tous.stream().anyMatch(v -> v instanceof Moto);
        assertTrue("Doit contenir au moins une Voiture", aVoiture);
        assertTrue("Doit contenir au moins une Moto", aMoto);
    }

    @Test
    public void testRequeteParSousType_Voiture() {
        em.getTransaction().begin();
        em.persist(new Voiture("BMW", "Serie3", LocalDate.of(2019, 4, 5), 35000.0, 4, true, "Essence"));
        em.getTransaction().commit();
        em.clear();

        List<Voiture> voitures = em.createQuery("SELECT v FROM Voiture v", Voiture.class).getResultList();

        assertFalse("La liste des voitures ne doit pas être vide", voitures.isEmpty());
        voitures.forEach(v -> assertNotNull("Chaque voiture doit avoir une marque", v.getMarque()));
    }

    @Test
    public void testRequeteParSousType_Moto() {
        em.getTransaction().begin();
        em.persist(new Moto("Kawasaki", "Z900", LocalDate.of(2023, 1, 1), 10000.0, 900, "Automatique"));
        em.getTransaction().commit();
        em.clear();

        List<Moto> motos = em.createQuery("SELECT m FROM Moto m", Moto.class).getResultList();

        assertFalse("La liste des motos ne doit pas être vide", motos.isEmpty());
        motos.forEach(m -> assertTrue("La cylindrée doit être positive", m.getCylindree() > 0));
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests UPDATE (Mise à jour)
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testMettreAJourVoiture() {
        em.getTransaction().begin();
        Voiture v = new Voiture("Fiat", "500", LocalDate.of(2020, 2, 14), 12000.0, 3, false, "Essence");
        em.persist(v);
        em.getTransaction().commit();

        Long id = v.getId();
        em.clear();

        // Mise à jour du prix
        em.getTransaction().begin();
        Voiture found = em.find(Voiture.class, id);
        found.setPrix(11000.0);
        em.getTransaction().commit();
        em.clear();

        Voiture updated = em.find(Voiture.class, id);
        assertEquals("Le prix mis à jour doit être 11000.0", 11000.0, updated.getPrix(), 0.001);
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests DELETE (Suppression)
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testSupprimerVoiture() {
        em.getTransaction().begin();
        Voiture v = new Voiture("Opel", "Corsa", LocalDate.of(2018, 11, 30), 8000.0, 5, true, "Diesel");
        em.persist(v);
        em.getTransaction().commit();

        Long id = v.getId();
        em.clear();

        // Suppression
        em.getTransaction().begin();
        Voiture found = em.find(Voiture.class, id);
        em.remove(found);
        em.getTransaction().commit();
        em.clear();

        Voiture deleted = em.find(Voiture.class, id);
        assertNull("La voiture supprimée ne doit plus exister", deleted);
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests des attributs spécifiques
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testAttributsSpecifiquesVoiture() {
        em.getTransaction().begin();
        Voiture v = new Voiture("Toyota", "Yaris", LocalDate.of(2022, 5, 10), 16000.0, 5, true, "Hybride");
        em.persist(v);
        em.getTransaction().commit();

        Long id = v.getId();
        em.clear();

        Voiture found = em.find(Voiture.class, id);
        assertEquals("Hybride", found.getTypeCarburant());
        assertEquals(Integer.valueOf(5), found.getNombrePortes());
        assertTrue(found.getClimatisation());
    }

    @Test
    public void testAttributsSpecifiquesMoto() {
        em.getTransaction().begin();
        Moto m = new Moto("Ducati", "Monster", LocalDate.of(2021, 7, 7), 15000.0, 937, "Manuelle");
        em.persist(m);
        em.getTransaction().commit();

        Long id = m.getId();
        em.clear();

        Moto found = em.find(Moto.class, id);
        assertEquals(Integer.valueOf(937), found.getCylindree());
        assertEquals("Manuelle", found.getTypeTransmission());
    }
}
