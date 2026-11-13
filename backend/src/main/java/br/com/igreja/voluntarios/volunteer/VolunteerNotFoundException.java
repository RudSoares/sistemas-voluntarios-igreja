package br.com.igreja.voluntarios.volunteer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VolunteerNotFoundException extends RuntimeException {

    public VolunteerNotFoundException(Long id) {
        super("Voluntário não encontrado: " + id);
    }
}
