package fr.cytech.malinassur.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public AppUser() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}