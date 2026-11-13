package br.com.igreja.voluntarios.ministry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "ministries", uniqueConstraints =
        @UniqueConstraint(name = "uk_ministry_name", columnNames = "name"))
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

    protected Ministry() {
        // Construtor exigido pelo JPA.
    }

    public Ministry(String nome, String descricao) {
        updateDetails(nome, descricao);
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

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
