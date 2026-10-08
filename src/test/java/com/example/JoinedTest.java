package com.example;

import com.example.model.joined.Developpeur;
import com.example.model.joined.Employe;
import com.example.model.joined.Manager;
import org.junit.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests JUnit pour la stratégie d'héritage JOINED.
 * Chaque classe a sa propre table. Les sous-tables sont joinées à la table parente
 * via une clé étrangère = clé primaire partagée.
 */
public class JoinedTest {

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
    public void testPersisterDeveloppeur() {
        em.getTransaction().begin();

        Developpeur dev = new Developpeur(
                "Dupont", "Jean", "jean.dupont@example.com",
                LocalDate.of(2020, 1, 15),
                "Java", "Backend", 5
        );
        em.persist(dev);

        em.getTransaction().commit();

        assertNotNull("L'ID du développeur doit être généré", dev.getId());
    }

    @Test
    public void testPersisterManager() {
        em.getTransaction().begin();

        Manager mgr = new Manager(
                "Martin", "Sophie", "sophie.martin@example.com",
                LocalDate.of(2018, 6, 1),
                "IT", 8, 4500.0
        );
        em.persist(mgr);

        em.getTransaction().commit();

        assertNotNull("L'ID du manager doit être généré", mgr.getId());
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests READ (Requêtes polymorphiques + JOIN)
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testRequetePolymorphique_TousLesEmployes() {
        em.getTransaction().begin();
        em.persist(new Developpeur("Leroy", "Alice", "alice@example.com",
                LocalDate.of(2021, 3, 10), "Python", "Data", 3));
        em.persist(new Manager("Blanc", "Marc", "marc@example.com",
                LocalDate.of(2017, 9, 1), "RH", 15, 6000.0));
        em.getTransaction().commit();
        em.clear();

        // Requête sur la classe parente Employe → doit ramener Développeurs ET Managers
        List<Employe> employes = em.createQuery("SELECT e FROM Employe e", Employe.class).getResultList();

        assertFalse("La liste des employés ne doit pas être vide", employes.isEmpty());
        boolean aDev = employes.stream().anyMatch(e -> e instanceof Developpeur);
        boolean aMgr = employes.stream().anyMatch(e -> e instanceof Manager);
        assertTrue("Doit contenir au moins un Développeur", aDev);
        assertTrue("Doit contenir au moins un Manager", aMgr);
    }

    @Test
    public void testRequeteParSousType_Developpeur() {
        em.getTransaction().begin();
        em.persist(new Developpeur("Moreau", "Paul", "paul@example.com",
                LocalDate.of(2022, 2, 20), "JavaScript", "Frontend", 2));
        em.getTransaction().commit();
        em.clear();

        List<Developpeur> devs = em.createQuery("SELECT d FROM Developpeur d", Developpeur.class).getResultList();

        assertFalse("La liste des développeurs ne doit pas être vide", devs.isEmpty());
        devs.forEach(d -> assertNotNull("Chaque développeur doit avoir un langage", d.getLangage()));
    }

    @Test
    public void testRequeteParSousType_Manager() {
        em.getTransaction().begin();
        em.persist(new Manager("Renard", "Claire", "claire@example.com",
                LocalDate.of(2015, 4, 5), "Finance", 20, 8000.0));
        em.getTransaction().commit();
        em.clear();

        List<Manager> managers = em.createQuery("SELECT m FROM Manager m", Manager.class).getResultList();

        assertFalse("La liste des managers ne doit pas être vide", managers.isEmpty());
        managers.forEach(m -> assertTrue("Le bonus doit être positif", m.getBonus() > 0));
    }

    @Test
    public void testFindParId_Developpeur() {
        em.getTransaction().begin();
        Developpeur dev = new Developpeur("Simon", "Emma", "emma@example.com",
                LocalDate.of(2023, 7, 11), "Kotlin", "Mobile", 4);
        em.persist(dev);
        em.getTransaction().commit();

        Long id = dev.getId();
        em.clear();

        Developpeur found = em.find(Developpeur.class, id);
        assertNotNull("Le développeur doit être retrouvé par son ID", found);
        assertEquals("Le nom doit correspondre", "Simon", found.getNom());
        assertEquals("Le prénom doit correspondre", "Emma", found.getPrenom());
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests UPDATE (Mise à jour)
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testMettreAJourBonus_Manager() {
        em.getTransaction().begin();
        Manager mgr = new Manager("Petit", "Luc", "luc@example.com",
                LocalDate.of(2016, 1, 1), "Marketing", 5, 3000.0);
        em.persist(mgr);
        em.getTransaction().commit();

        Long id = mgr.getId();
        em.clear();

        // Mise à jour du bonus
        em.getTransaction().begin();
        Manager found = em.find(Manager.class, id);
        found.setBonus(5000.0);
        em.getTransaction().commit();
        em.clear();

        Manager updated = em.find(Manager.class, id);
        assertEquals("Le bonus mis à jour doit être 5000.0", 5000.0, updated.getBonus(), 0.001);
    }

    @Test
    public void testMettreAJourLangage_Developpeur() {
        em.getTransaction().begin();
        Developpeur dev = new Developpeur("Bernard", "Tom", "tom@example.com",
                LocalDate.of(2021, 5, 15), "PHP", "Backend", 1);
        em.persist(dev);
        em.getTransaction().commit();

        Long id = dev.getId();
        em.clear();

        em.getTransaction().begin();
        Developpeur found = em.find(Developpeur.class, id);
        found.setLangage("Java");
        em.getTransaction().commit();
        em.clear();

        Developpeur updated = em.find(Developpeur.class, id);
        assertEquals("Le langage doit être mis à jour", "Java", updated.getLangage());
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests DELETE (Suppression)
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testSupprimerDeveloppeur() {
        em.getTransaction().begin();
        Developpeur dev = new Developpeur("Lefebvre", "Iris", "iris@example.com",
                LocalDate.of(2022, 8, 8), "Go", "Backend", 2);
        em.persist(dev);
        em.getTransaction().commit();

        Long id = dev.getId();
        em.clear();

        em.getTransaction().begin();
        Developpeur found = em.find(Developpeur.class, id);
        em.remove(found);
        em.getTransaction().commit();
        em.clear();

        Developpeur deleted = em.find(Developpeur.class, id);
        assertNull("Le développeur supprimé ne doit plus exister", deleted);
        // Vérifier aussi que la ligne parente (employes) est supprimée
        Employe deletedParent = em.find(Employe.class, id);
        assertNull("L'entrée parente dans employes doit aussi être supprimée", deletedParent);
    }

    @Test
    public void testSupprimerManager() {
        em.getTransaction().begin();
        Manager mgr = new Manager("Girard", "Noel", "noel@example.com",
                LocalDate.of(2014, 12, 24), "Logistique", 30, 7000.0);
        em.persist(mgr);
        em.getTransaction().commit();

        Long id = mgr.getId();
        em.clear();

        em.getTransaction().begin();
        Manager found = em.find(Manager.class, id);
        em.remove(found);
        em.getTransaction().commit();
        em.clear();

        assertNull("Le manager supprimé ne doit plus exister", em.find(Manager.class, id));
    }

    // ─────────────────────────────────────────────────────────────────
    // Tests des attributs spécifiques
    // ─────────────────────────────────────────────────────────────────

    @Test
    public void testAttributsSpecifiquesDeveloppeur() {
        em.getTransaction().begin();
        Developpeur dev = new Developpeur("Roy", "Lea", "lea@example.com",
                LocalDate.of(2023, 1, 1), "Rust", "Systèmes", 6);
        em.persist(dev);
        em.getTransaction().commit();

        Long id = dev.getId();
        em.clear();

        Developpeur found = em.find(Developpeur.class, id);
        assertEquals("Rust", found.getLangage());
        assertEquals("Systèmes", found.getSpecialite());
        assertEquals(Integer.valueOf(6), found.getAnneeExperience());
    }

    @Test
    public void testAttributsSpecifiquesManager() {
        em.getTransaction().begin();
        Manager mgr = new Manager("Faure", "Felix", "felix@example.com",
                LocalDate.of(2012, 3, 20), "Production", 50, 10000.0);
        em.persist(mgr);
        em.getTransaction().commit();

        Long id = mgr.getId();
        em.clear();

        Manager found = em.find(Manager.class, id);
        assertEquals("Production", found.getService());
        assertEquals(Integer.valueOf(50), found.getNombreSubordonnes());
        assertEquals(10000.0, found.getBonus(), 0.001);
    }
}
