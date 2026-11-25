package br.com.igreja.voluntarios.ministry;

import br.com.igreja.voluntarios.organization.Branch;
import br.com.igreja.voluntarios.organization.Church;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "ministries", uniqueConstraints =
        @UniqueConstraint(name = "uk_ministry_church_name", columnNames = {"church_id", "name"}))
public class Ministry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String nome;

    @Column(name = "description", length = 2000)
    private String descricao;

    @Column(name = "active", nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "church_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ministry_church"))
    private Church church;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", foreignKey = @ForeignKey(name = "fk_ministry_branch"))
    private Branch branch;

    protected Ministry() {
        // Construtor exigido pelo JPA.
    }

    public Ministry(String nome, String descricao, Church church, Branch branch) {
        updateDetails(nome, descricao);
        this.church = church;
        this.branch = branch;
    }

    public void updateDetails(String nome, String descricao) {
        this.nome = nome.trim();
        this.descricao = blankToNull(descricao);
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Church getChurch() { return church; }
    public Branch getBranch() { return branch; }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
