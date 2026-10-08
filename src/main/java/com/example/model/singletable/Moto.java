package com.example.model.singletable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("MOTO")
public class Moto extends Vehicule {

    @NotNull(message = "La cylindrée est obligatoire")
    @Min(value = 50, message = "La cylindrée minimum est de 50cc")
    @Max(value = 2000, message = "La cylindrée maximum est de 2000cc")
    private Integer cylindree;

    private String typeTransmission;

    // Constructeur par défaut requis par JPA
    public Moto() {
    }

    public Moto(String marque, String modele, LocalDate anneeFabrication, Double prix,
                Integer cylindree, String typeTransmission) {
        super(marque, modele, anneeFabrication, prix);
        this.cylindree = cylindree;
        this.typeTransmission = typeTransmission;
    }

    // Getters et Setters
    public Integer getCylindree() {
        return cylindree;
    }

    public void setCylindree(Integer cylindree) {
        this.cylindree = cylindree;
    }

    public String getTypeTransmission() {
        return typeTransmission;
    }

    public void setTypeTransmission(String typeTransmission) {
        this.typeTransmission = typeTransmission;
    }

    @Override
    public String toString() {
        return "Moto{" +
                "id=" + getId() +
                ", marque='" + getMarque() + '\'' +
                ", modele='" + getModele() + '\'' +
                ", anneeFabrication=" + getAnneeFabrication() +
                ", prix=" + getPrix() +
                ", cylindree=" + cylindree +
                ", typeTransmission='" + typeTransmission + '\'' +
                '}';
    }
}

