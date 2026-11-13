package br.com.igreja.voluntarios.volunteer;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "volunteers")
public class Volunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(name = "phone", length = 40)
    private String telefone;

    @Column(name = "email", length = 254)
    private String email;

    @Column(name = "active", nullable = false)
    private boolean ativo = true;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "volunteer_availability_days",
            joinColumns = @JoinColumn(name = "volunteer_id"))
    @Column(name = "availability_day", nullable = false, length = 40)
    private Set<String> disponibilidadeDias = new LinkedHashSet<>();

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observacoes;

    protected Volunteer() {
        // Construtor exigido pelo JPA.
    }

    public Volunteer(
            String nomeCompleto,
            String telefone,
            String email,
            Set<String> disponibilidadeDias,
            String observacoes) {
        updateDetails(nomeCompleto, telefone, email, disponibilidadeDias, observacoes);
    }

    public void updateDetails(
            String nomeCompleto,
            String telefone,
            String email,
            Set<String> disponibilidadeDias,
            String observacoes) {
        this.nomeCompleto = nomeCompleto.trim();
        this.telefone = blankToNull(telefone);
        this.email = blankToNull(email);
        this.disponibilidadeDias = disponibilidadeDias == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(disponibilidadeDias);
        this.observacoes = blankToNull(observacoes);
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Set<String> getDisponibilidadeDias() {
        return disponibilidadeDias;
    }

    public String getObservacoes() {
        return observacoes;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
