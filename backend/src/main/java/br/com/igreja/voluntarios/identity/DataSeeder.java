package br.com.igreja.voluntarios.identity;

import br.com.igreja.voluntarios.organization.Branch;
import br.com.igreja.voluntarios.organization.BranchRepository;
import br.com.igreja.voluntarios.organization.Church;
import br.com.igreja.voluntarios.organization.ChurchRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {
    private final AppUserRepository users;
    private final ChurchRepository churches;
    private final BranchRepository branches;
    private final PasswordEncoder encoder;
    private final String superAdminPassword;
    private final String churchAdminPassword;
    private final boolean enabled;

    public DataSeeder(AppUserRepository users, ChurchRepository churches, BranchRepository branches,
            PasswordEncoder encoder,
            @Value("${SEED_SUPER_ADMIN_PASSWORD:admin123}") String superAdminPassword,
            @Value("${SEED_CHURCH_ADMIN_PASSWORD:poiema123}") String churchAdminPassword,
            @Value("${SEED_ENABLED:true}") boolean enabled) {
        this.users = users; this.churches = churches; this.branches = branches; this.encoder = encoder;
        this.superAdminPassword = superAdminPassword; this.churchAdminPassword = churchAdminPassword;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled || users.count() != 0) return;
        Church church = churches.findFirstByName("Igreja Poiema")
                .orElseGet(() -> churches.save(new Church("Igreja Poiema", null, null, null)));
        Branch branch = branches.findAllByChurch_IdOrderByNameAsc(church.getId()).stream()
                .filter(item -> item.getName().equalsIgnoreCase("Sede"))
                .findFirst().orElseGet(() -> branches.save(new Branch("Sede", null, null, church)));
        users.save(new AppUser("Administrador da plataforma", "admin@sistema.com",
                encoder.encode(superAdminPassword), UserRole.SUPER_ADMIN, null, null, null));
        users.save(new AppUser("Liderança Igreja Poiema", "lider@poiema.com",
                encoder.encode(churchAdminPassword), UserRole.CHURCH_ADMIN, church, branch, null));
    }
}
