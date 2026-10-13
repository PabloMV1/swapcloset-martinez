package es.safareyes.swapcloset.repository.spec;

import es.safareyes.swapcloset.entity.*;
import org.springframework.data.jpa.domain.Specification;

/** Q7: filtros opcionales y combinables del buscador de prendas (RF-03). */
public final class PrendaSpecs {

    private PrendaSpecs() {
    }

    /** Condición base: solo prendas activas y disponibles (excluye BORRADOR y retiradas). */
    public static Specification<Prenda> visibles() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("activa")),
                cb.equal(root.get("disponibilidad"), Disponibilidad.DISPONIBLE));
    }

    public static Specification<Prenda> categoria(Long categoriaId) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    public static Specification<Prenda> talla(String talla) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("talla")), talla.toLowerCase());
    }

    public static Specification<Prenda> estado(EstadoPrenda estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<Prenda> estilo(Long estiloId) {
        return (root, query, cb) -> cb.equal(root.get("estilo").get("id"), estiloId);
    }

    public static Specification<Prenda> ciudad(String ciudad) {
        return (root, query, cb) -> cb.equal(
                cb.lower(root.get("propietaria").get("ciudad")), ciudad.toLowerCase());
    }

    /** Una prenda AMBAS vale para pedir intercambio o préstamo. */
    public static Specification<Prenda> modalidad(Modalidad modalidad) {
        return (root, query, cb) -> modalidad == Modalidad.AMBAS
                ? cb.equal(root.get("modalidad"), Modalidad.AMBAS)
                : root.get("modalidad").in(modalidad, Modalidad.AMBAS);
    }

    /** Combina solo los filtros que vengan informados. */
    public static Specification<Prenda> filtrar(Long categoriaId, String talla, EstadoPrenda estado,
                                                Long estiloId, String ciudad, Modalidad modalidad) {
        Specification<Prenda> spec = visibles();
        if (categoriaId != null) spec = spec.and(categoria(categoriaId));
        if (talla != null && !talla.isBlank()) spec = spec.and(talla(talla.trim()));
        if (estado != null) spec = spec.and(estado(estado));
        if (estiloId != null) spec = spec.and(estilo(estiloId));
        if (ciudad != null && !ciudad.isBlank()) spec = spec.and(ciudad(ciudad.trim()));
        if (modalidad != null) spec = spec.and(modalidad(modalidad));
        return spec;
    }
}
