package br.com.igreja.voluntarios.ministry;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "ministry_roles", uniqueConstraints =
        @UniqueConstraint(name = "uk_ministry_role_id_ministry", columnNames = {"id", "ministry_id"}))
public class MinistryRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ministry_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ministry_role_ministry"))
    private Ministry ministry;

    protected MinistryRole() {
        // Construtor exigido pelo JPA.
    }

    public MinistryRole(String nome, Ministry ministry) {
        this.nome = nome.trim();
        this.ministry = ministry;
    }

    public void rename(String nome) {
        this.nome = nome.trim();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Ministry getMinistry() {
        return ministry;
    }
}
