package com.example.model.tableperclass;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Entity
@Table(name = "livres")
public class Livre extends Produit {

    @NotBlank(message = "L'auteur est obligatoire")
    private String auteur;

    @NotBlank(message = "L'ISBN est obligatoire")
    private String isbn;

    @Positive(message = "Le nombre de pages doit être positif")
    private Integer nombrePages;

    private String editeur;

    // Constructeur par défaut requis par JPA
    public Livre() {
    }

    public Livre(String nom, Double prix, String description, String auteur,
                 String isbn, Integer nombrePages, String editeur) {
        super(nom, prix, description);
        this.auteur = auteur;
        this.isbn = isbn;
        this.nombrePages = nombrePages;
        this.editeur = editeur;
    }

    // Getters et Setters
    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getNombrePages() {
        return nombrePages;
    }

    public void setNombrePages(Integer nombrePages) {
        this.nombrePages = nombrePages;
    }

    public String getEditeur() {
        return editeur;
    }

    public void setEditeur(String editeur) {
        this.editeur = editeur;
    }

    @Override
    public String toString() {
        return "Livre{" +
                "id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", prix=" + getPrix() +
                ", description='" + getDescription() + '\'' +
                ", dateCreation=" + getDateCreation() +
                ", auteur='" + auteur + '\'' +
                ", isbn='" + isbn + '\'' +
                ", nombrePages=" + nombrePages +
                ", editeur='" + editeur + '\'' +
                '}';
    }
}
