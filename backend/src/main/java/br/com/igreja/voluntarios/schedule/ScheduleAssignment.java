package br.com.igreja.voluntarios.schedule;

import br.com.igreja.voluntarios.ministry.Ministry;
import br.com.igreja.voluntarios.ministry.MinistryRole;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "schedule_assignments", uniqueConstraints =
        @UniqueConstraint(name = "uk_schedule_event_volunteer", columnNames = {"service_event_id", "volunteer_id"}))
public class ScheduleAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_event_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_schedule_event"))
    private ServiceEvent serviceEvent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ministry_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_schedule_ministry"))
    private Ministry ministry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ministry_role_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_schedule_role"))
    private MinistryRole ministryRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volunteer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_schedule_volunteer"))
    private Volunteer volunteer;

    @Enumerated(EnumType.STRING)
    @Column(name = "confirmation_status", nullable = false, length = 20)
    private ConfirmationStatus statusConfirmacao = ConfirmationStatus.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "church_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_schedule_church"))
    private Church church;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", foreignKey = @ForeignKey(name = "fk_schedule_branch"))
    private Branch branch;

    protected ScheduleAssignment() { }

    public ScheduleAssignment(ServiceEvent event, Ministry ministry, MinistryRole role, Volunteer volunteer,
            Church church, Branch branch) {
        this.serviceEvent = event;
        this.ministry = ministry;
        this.ministryRole = role;
        this.volunteer = volunteer;
        this.church = church;
        this.branch = branch;
    }

    public void setStatusConfirmacao(ConfirmationStatus status) { this.statusConfirmacao = status; }
    public Long getId() { return id; }
    public ServiceEvent getServiceEvent() { return serviceEvent; }
    public Ministry getMinistry() { return ministry; }
    public MinistryRole getMinistryRole() { return ministryRole; }
    public Volunteer getVolunteer() { return volunteer; }
    public ConfirmationStatus getStatusConfirmacao() { return statusConfirmacao; }
    public Church getChurch() { return church; }
    public Branch getBranch() { return branch; }
}
