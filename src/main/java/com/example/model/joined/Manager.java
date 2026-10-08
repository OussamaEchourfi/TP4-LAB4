package com.example.model.joined;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "managers")
public class Manager extends Employe {

    private String service;
    private Integer nombreSubordonnes;
    private Double bonus;

    // Constructeur par défaut requis par JPA
    public Manager() {
    }

    public Manager(String nom, String prenom, String email, LocalDate dateEmbauche,
                   String service, Integer nombreSubordonnes, Double bonus) {
        super(nom, prenom, email, dateEmbauche);
        this.service = service;
        this.nombreSubordonnes = nombreSubordonnes;
        this.bonus = bonus;
    }

    // Getters et Setters
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer getNombreSubordonnes() {
        return nombreSubordonnes;
    }

    public void setNombreSubordonnes(Integer nombreSubordonnes) {
        this.nombreSubordonnes = nombreSubordonnes;
    }

    public Double getBonus() {
        return bonus;
    }

    public void setBonus(Double bonus) {
        this.bonus = bonus;
    }

    @Override
    public String toString() {
        return "Manager{" +
                "id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", dateEmbauche=" + getDateEmbauche() +
                ", service='" + service + '\'' +
                ", nombreSubordonnes=" + nombreSubordonnes +
                ", bonus=" + bonus +
                '}';
    }
}

