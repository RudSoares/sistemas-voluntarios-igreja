package br.com.igreja.voluntarios.identity;

import br.com.igreja.voluntarios.organization.Branch;
import br.com.igreja.voluntarios.organization.Church;
import br.com.igreja.voluntarios.volunteer.Volunteer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "app_users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_app_user_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_app_user_volunteer", columnNames = "volunteer_id")})
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150) private String name;
    @Column(nullable = false, length = 254) private String email;
    @Column(name = "password_hash", nullable = false, length = 100) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private UserRole role;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", foreignKey = @ForeignKey(name = "fk_app_user_church"))
    private Church church;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", foreignKey = @ForeignKey(name = "fk_app_user_branch"))
    private Branch branch;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id",
            foreignKey = @ForeignKey(name = "fk_app_user_volunteer"))
    private Volunteer volunteer;
    @Column(nullable = false) private boolean active = true;

    protected AppUser() { }
    public AppUser(String name, String email, String passwordHash, UserRole role, Church church, Branch branch, Volunteer volunteer) {
        this.name = name.trim(); this.email = email.trim().toLowerCase(); this.passwordHash = passwordHash;
        this.role = role; this.church = church; this.branch = branch; this.volunteer = volunteer;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public Church getChurch() { return church; }
    public Branch getBranch() { return branch; }
    public Volunteer getVolunteer() { return volunteer; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public void updateIdentity(String name, String email) { this.name = name.trim(); this.email = email.trim().toLowerCase(); }
}
