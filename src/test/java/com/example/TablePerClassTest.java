package com.example;

import com.example.model.tableperclass.Electronique;
import com.example.model.tableperclass.Livre;
import com.example.model.tableperclass.Produit;
import org.junit.*;

import javax.persistence.*;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests JUnit pour la stratégie d'héritage TABLE_PER_CLASS.
 * Chaque classe concrète a sa propre table complète (pas de table parente).
 * Les requêtes polymorphiques utilisent UNION ALL entre les tables.
 */
public class TablePerClassTest {

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
    public void testPersisterLivre() {
        em.getTransaction().begin();

        Livre livre = new Livre(
                "Le Petit Prince", 9.99, "Un conte philosophique",
                "Antoine de Saint-Exupéry", "978-2070612758", 96, "Gallimard"
        );
        em.persist(livre);

        em.getTransaction().commit();

        assertNotNull("L'ID du livre doit être généré après persist", livre.getId());
    }

    @Test
    public void testPersisterElectronique() {
        em.getTransaction().begin();

        Electronique elec = new Electronique(
                "iPhone 15", 1199.0, "Smartphone Apple",
                "Apple", "15 Pro", 24, "A17 Pro, 256Go, 6Go RAM"
        );
        em.persist(elec);

        em.getTransaction().commit();

        assertNotNull("L'ID du produit électronique doit être généré", elec.getId());
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests READ (Requêtes polymorphiques UNION ALL)
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testRequetePolymorphique_TousLesProduits() {
        em.getTransaction().begin();
        em.persist(new Livre("Dune", 14.99, "SF classique",
                "Frank Herbert", "978-2266320931", 896, "Pocket"));
        em.persist(new Electronique("Galaxy Tab S9", 799.0, "Tablette Samsung",
                "Samsung", "Tab S9", 12, "AMOLED 11\", 128Go"));
        em.getTransaction().commit();
        em.clear();

        // Requête UNION ALL sur tous les produits (Livre + Electronique)
        List<Produit> produits = em.createQuery("SELECT p FROM Produit p", Produit.class).getResultList();

        assertFalse("La liste des produits ne doit pas être vide", produits.isEmpty());
        boolean aLivre = produits.stream().anyMatch(p -> p instanceof Livre);
        boolean aElec  = produits.stream().anyMatch(p -> p instanceof Electronique);
        assertTrue("Doit contenir au moins un Livre", aLivre);
        assertTrue("Doit contenir au moins un produit Electronique", aElec);
    }

    @Test
    public void testRequeteParSousType_Livre() {
        em.getTransaction().begin();
        em.persist(new Livre("1984", 8.99, "Dystopie",
                "George Orwell", "978-2070368228", 384, "Gallimard"));
        em.getTransaction().commit();
        em.clear();

        List<Livre> livres = em.createQuery("SELECT l FROM Livre l", Livre.class).getResultList();

        assertFalse("La liste des livres ne doit pas être vide", livres.isEmpty());
        livres.forEach(l -> assertNotNull("Chaque livre doit avoir un auteur", l.getAuteur()));
    }

    @Test
    public void testRequeteParSousType_Electronique() {
        em.getTransaction().begin();
        em.persist(new Electronique("MacBook Pro", 2499.0, "Ordinateur portable Apple",
                "Apple", "M3 Pro", 36, "M3 Pro, 18Go RAM, 512Go SSD"));
        em.getTransaction().commit();
        em.clear();

        List<Electronique> elecs = em.createQuery("SELECT e FROM Electronique e", Electronique.class).getResultList();

        assertFalse("La liste des électroniques ne doit pas être vide", elecs.isEmpty());
        elecs.forEach(e -> assertTrue("La garantie doit être positive", e.getGarantieMois() > 0));
    }

    @Test
    public void testFindParId_Livre() {
        em.getTransaction().begin();
        Livre livre = new Livre("Sapiens", 19.99, "Histoire de l'humanité",
                "Yuval Noah Harari", "978-2226257017", 512, "Albin Michel");
        em.persist(livre);
        em.getTransaction().commit();

        Long id = livre.getId();
        em.clear();

        Livre found = em.find(Livre.class, id);
        assertNotNull("Le livre doit être retrouvé par son ID", found);
        assertEquals("Sapiens", found.getNom());
        assertEquals("Yuval Noah Harari", found.getAuteur());
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests UPDATE (Mise à jour)
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testMettreAJourPrix_Livre() {
        em.getTransaction().begin();
        Livre livre = new Livre("Test Livre", 20.0, "Description",
                "Auteur Test", "123-456", 200, "Editeur Test");
        em.persist(livre);
        em.getTransaction().commit();

        Long id = livre.getId();
        em.clear();

        em.getTransaction().begin();
        Livre found = em.find(Livre.class, id);
        found.setPrix(15.0);
        em.getTransaction().commit();
        em.clear();

        Livre updated = em.find(Livre.class, id);
        assertEquals("Le prix mis à jour doit être 15.0", 15.0, updated.getPrix(), 0.001);
    }

    @Test
    public void testMettreAJourGarantie_Electronique() {
        em.getTransaction().begin();
        Electronique elec = new Electronique("TV Test", 500.0, "Télévision",
                "LG", "OLED55", 12, "4K OLED, 55 pouces");
        em.persist(elec);
        em.getTransaction().commit();

        Long id = elec.getId();
        em.clear();

        em.getTransaction().begin();
        Electronique found = em.find(Electronique.class, id);
        found.setGarantieMois(24);
        em.getTransaction().commit();
        em.clear();

        Electronique updated = em.find(Electronique.class, id);
        assertEquals("La garantie mise à jour doit être 24 mois", Integer.valueOf(24), updated.getGarantieMois());
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests DELETE (Suppression)
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testSupprimerLivre() {
        em.getTransaction().begin();
        Livre livre = new Livre("Livre à supprimer", 5.0, "Test suppression",
                "Auteur", "000-000", 100, "Editeur");
        em.persist(livre);
        em.getTransaction().commit();

        Long id = livre.getId();
        em.clear();

        em.getTransaction().begin();
        Livre found = em.find(Livre.class, id);
        em.remove(found);
        em.getTransaction().commit();
        em.clear();

        Livre deleted = em.find(Livre.class, id);
        assertNull("Le livre supprimé ne doit plus exister", deleted);
    }

    @Test
    public void testSupprimerElectronique() {
        em.getTransaction().begin();
        Electronique elec = new Electronique("Produit à supprimer", 100.0, "Test",
                "Marque", "Modele", 6, "Specs");
        em.persist(elec);
        em.getTransaction().commit();

        Long id = elec.getId();
        em.clear();

        em.getTransaction().begin();
        Electronique found = em.find(Electronique.class, id);
        em.remove(found);
        em.getTransaction().commit();
        em.clear();

        assertNull("L'électronique supprimé ne doit plus exister", em.find(Electronique.class, id));
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests des attributs spécifiques
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testAttributsSpecifiquesLivre() {
        em.getTransaction().begin();
        Livre livre = new Livre("Clean Code", 35.0, "Guide de programmation",
                "Robert C. Martin", "978-0132350884", 431, "Prentice Hall");
        em.persist(livre);
        em.getTransaction().commit();

        Long id = livre.getId();
        em.clear();

        Livre found = em.find(Livre.class, id);
        assertEquals("Robert C. Martin", found.getAuteur());
        assertEquals("978-0132350884", found.getIsbn());
        assertEquals(Integer.valueOf(431), found.getNombrePages());
        assertEquals("Prentice Hall", found.getEditeur());
    }

    @Test
    public void testAttributsSpecifiquesElectronique() {
        em.getTransaction().begin();
        Electronique elec = new Electronique("PlayStation 5", 549.99, "Console de jeu",
                "Sony", "PS5 Slim", 24, "4K, 1To SSD, Ray Tracing");
        em.persist(elec);
        em.getTransaction().commit();

        Long id = elec.getId();
        em.clear();

        Electronique found = em.find(Electronique.class, id);
        assertEquals("Sony", found.getMarque());
        assertEquals("PS5 Slim", found.getModele());
        assertEquals(Integer.valueOf(24), found.getGarantieMois());
        assertNotNull(found.getCaracteristiques());
    }

    @Test
    public void testDateCreationAutoGeneree() {
        em.getTransaction().begin();
        Livre livre = new Livre("Date Test", 10.0, "Test",
                "Auteur", "ISBN", 50, "Editeur");
        em.persist(livre);
        em.getTransaction().commit();

        assertNotNull("La date de création doit être auto-générée", livre.getDateCreation());
    }
}
