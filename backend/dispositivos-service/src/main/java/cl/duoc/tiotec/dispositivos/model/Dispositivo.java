package cl.duoc.tiotec.dispositivos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// #tabla dispositivo en la base de datos
/**
 * Dispositivo ficticio de prueba que se evalua en la app. Solo guarda un nombre
 * y el sistema operativo: la foto del dispositivo se queda en el telefono.
 */
@Entity
@Table(name = "dispositivo", uniqueConstraints = @UniqueConstraint(name = "uk_dispositivo_nombre", columnNames = "nombre"))
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SistemaOperativo sistemaOperativo;

    /** Un dispositivo archivado se conserva pero ya no se puede editar. */
    @Column(nullable = false)
    private boolean archivado;

    /** Milisegundos desde epoch (igual que los campos de fecha de la app). */
    @Column(nullable = false)
    private long creadoEn;

    @Column(nullable = false)
    private long actualizadoEn;

    protected Dispositivo() {
        // requerido por JPA
    }

    public Dispositivo(String nombre, SistemaOperativo sistemaOperativo, long ahora) {
        this.nombre = nombre;
        this.sistemaOperativo = sistemaOperativo;
        this.archivado = false;
        this.creadoEn = ahora;
        this.actualizadoEn = ahora;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public SistemaOperativo getSistemaOperativo() { return sistemaOperativo; }
    public void setSistemaOperativo(SistemaOperativo sistemaOperativo) { this.sistemaOperativo = sistemaOperativo; }
    public boolean isArchivado() { return archivado; }
    public void setArchivado(boolean archivado) { this.archivado = archivado; }
    public long getCreadoEn() { return creadoEn; }
    public long getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(long actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
