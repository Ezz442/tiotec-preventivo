package cl.duoc.tiotec.solicitudes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Registro de cada cambio de estado de una solicitud (relacion 1 a N). */
@Entity
@Table(name = "historial_solicitud")
public class HistorialSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(nullable = false)
    private long fecha;

    @Column(nullable = false, length = 100)
    private String nota;

    protected HistorialSolicitud() {
        // requerido por JPA
    }

    HistorialSolicitud(Solicitud solicitud, EstadoSolicitud estado, long fecha, String nota) {
        this.solicitud = solicitud;
        this.estado = estado;
        this.fecha = fecha;
        this.nota = nota;
    }

    public Long getId() { return id; }
    public EstadoSolicitud getEstado() { return estado; }
    public long getFecha() { return fecha; }
    public String getNota() { return nota; }
}
