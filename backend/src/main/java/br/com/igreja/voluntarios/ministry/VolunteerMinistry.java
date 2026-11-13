package br.com.igreja.voluntarios.ministry;

import br.com.igreja.voluntarios.volunteer.Volunteer;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "volunteer_ministries", uniqueConstraints =
        @UniqueConstraint(
                name = "uk_volunteer_ministry_role",
                columnNames = {"volunteer_id", "ministry_id", "ministry_role_id"}))
public class VolunteerMinistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volunteer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_volunteer_ministry_volunteer"))
    private Volunteer volunteer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ministry_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_volunteer_ministry_ministry"))
    private Ministry ministry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ministry_role_id", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_volunteer_ministry_role_id"))
    private MinistryRole role;

    protected VolunteerMinistry() {
        // Construtor exigido pelo JPA.
    }

    public VolunteerMinistry(Volunteer volunteer, Ministry ministry, MinistryRole role) {
        this.volunteer = volunteer;
        this.ministry = ministry;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public Ministry getMinistry() {
        return ministry;
    }

    public MinistryRole getRole() {
        return role;
    }
}
