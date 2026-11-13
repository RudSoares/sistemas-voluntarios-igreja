package br.com.igreja.voluntarios.schedule;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_events")
public class ServiceEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(name = "event_date", nullable = false)
    private LocalDate dataEvento;

    @Column(name = "event_time", nullable = false)
    private LocalTime horario;

    @Column(name = "day_or_shift", nullable = false, length = 40)
    private String turnoOuDiaSemana;

    @Column(length = 2000)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.AGENDADO;

    protected ServiceEvent() { }

    public ServiceEvent(String titulo, LocalDate dataEvento, LocalTime horario, String turnoOuDiaSemana, String observacoes) {
        update(titulo, dataEvento, horario, turnoOuDiaSemana, observacoes);
    }

    public void update(String titulo, LocalDate dataEvento, LocalTime horario, String turnoOuDiaSemana, String observacoes) {
        this.titulo = titulo.trim();
        this.dataEvento = dataEvento;
        this.horario = horario;
        this.turnoOuDiaSemana = turnoOuDiaSemana.trim().toUpperCase();
        this.observacoes = observacoes == null || observacoes.isBlank() ? null : observacoes.trim();
    }

    public void setStatus(EventStatus status) { this.status = status; }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public LocalDate getDataEvento() { return dataEvento; }
    public LocalTime getHorario() { return horario; }
    public String getTurnoOuDiaSemana() { return turnoOuDiaSemana; }
    public String getObservacoes() { return observacoes; }
    public EventStatus getStatus() { return status; }
}
