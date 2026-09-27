package com.piedraazul.identidad.infraestructura.persistencia;

import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.dominio.Usuario;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final SpringDataUsuarioJpaRepository jpa;

    public UsuarioRepositoryAdapter(SpringDataUsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioJpaEntity saved = jpa.save(toJpa(usuario));
        return toDomain(saved);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return jpa.findByUsername(username).map(this::toDomain);
    }

    @Override
    public boolean existePorUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public List<Usuario> listarTodos() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    private Usuario toDomain(UsuarioJpaEntity e) {
        return Usuario.reconstituir(
                e.getId(),
                e.getUsername(),
                e.getPasswordHash(),
                e.getRol(),
                e.getPersonaId(),
                e.isActivo()
        );
    }

    private UsuarioJpaEntity toJpa(Usuario u) {
        return new UsuarioJpaEntity(
                u.getId(),
                u.getUsername(),
                u.getPasswordHash(),
                u.getRol(),
                u.getPersonaId(),
                u.estaActivo()
        );
    }
}
