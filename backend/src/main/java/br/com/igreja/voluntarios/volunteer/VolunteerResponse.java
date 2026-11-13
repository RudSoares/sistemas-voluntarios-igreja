package br.com.igreja.voluntarios.volunteer;

import java.util.List;

public record VolunteerResponse(
        Long id,
        String nomeCompleto,
        String telefone,
        String email,
        boolean ativo,
        List<String> disponibilidadeDias,
        String observacoes) {
}
