package br.com.igreja.voluntarios.organization;
public record ChurchResponse(Long id, String name, String cnpj, String city, String state, boolean active) {
    static ChurchResponse from(Church item) { return new ChurchResponse(item.getId(), item.getName(), item.getCnpj(), item.getCity(), item.getState(), item.isActive()); }
}
