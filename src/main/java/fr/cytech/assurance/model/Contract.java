package fr.cytech.assurance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "contract")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Contract() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}