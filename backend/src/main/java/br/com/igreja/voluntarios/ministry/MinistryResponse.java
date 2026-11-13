package br.com.igreja.voluntarios.ministry;

public record MinistryResponse(
        Long id,
        String nome,
        String descricao,
        boolean ativo) {
}
