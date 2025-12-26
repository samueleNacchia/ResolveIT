package it.unisa.resolveIt.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CategoriaStub {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID_C;
}
