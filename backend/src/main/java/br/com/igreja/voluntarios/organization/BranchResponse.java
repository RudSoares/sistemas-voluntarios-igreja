package br.com.igreja.voluntarios.organization;
public record BranchResponse(Long id, String name, String address, String city, Long churchId, boolean active) {
    static BranchResponse from(Branch item) { return new BranchResponse(item.getId(), item.getName(), item.getAddress(), item.getCity(), item.getChurch().getId(), item.isActive()); }
}
