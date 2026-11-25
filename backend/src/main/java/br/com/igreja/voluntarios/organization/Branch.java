package br.com.igreja.voluntarios.organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "branches")
public class Branch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 150) private String name;
    @Column(length = 255) private String address;
    @Column(length = 100) private String city;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "church_id", nullable = false, foreignKey = @ForeignKey(name = "fk_branch_church"))
    private Church church;
    @Column(nullable = false) private boolean active = true;
    protected Branch() { }
    public Branch(String name, String address, String city, Church church) { update(name, address, city); this.church = church; }
    public void update(String name, String address, String city) {
        this.name = name.trim(); this.address = blank(address); this.city = blank(city);
    }
    public void setActive(boolean active) { this.active = active; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public Church getChurch() { return church; }
    public boolean isActive() { return active; }
    private static String blank(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
