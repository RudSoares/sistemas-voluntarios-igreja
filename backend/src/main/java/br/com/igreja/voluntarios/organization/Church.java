package br.com.igreja.voluntarios.organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "churches")
public class Church {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 150) private String name;
    @Column(length = 18) private String cnpj;
    @Column(length = 100) private String city;
    @Column(length = 2) private String state;
    @Column(nullable = false) private boolean active = true;
    protected Church() { }
    public Church(String name, String cnpj, String city, String state) { update(name, cnpj, city, state); }
    public void update(String name, String cnpj, String city, String state) {
        this.name = name.trim(); this.cnpj = blank(cnpj); this.city = blank(city); this.state = blank(state);
    }
    public void setActive(boolean active) { this.active = active; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCnpj() { return cnpj; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public boolean isActive() { return active; }
    private static String blank(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
