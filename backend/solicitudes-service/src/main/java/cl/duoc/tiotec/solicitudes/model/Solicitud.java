package cl.duoc.tiotec.solicitudes.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

// #tabla solicitud en la base de datos
/**
 * Solicitud de asistencia simulada. No guarda texto libre ni datos personales:
 * solo un alias ficticio y una categoria.
 */
@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** DEMO-001, DEMO-002... se asigna despues de guardar, a partir del id. */
    @Column(unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 30)
    private String alias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    /** Milisegundos desde epoch (igual que los campos de fecha de la app). */
    @Column(nullable = false)
    private long creadaEn;

    @Column(nullable = false)
    private long actualizadaEn;

    // #relacion solicitud con su historial
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("fecha ASC, id ASC")
    private List<HistorialSolicitud> historial = new ArrayList<>();

    protected Solicitud() {
        // requerido por JPA
    }

    public Solicitud(String alias, Categoria categoria, EstadoSolicitud estado, long ahora) {
        this.alias = alias;
        this.categoria = categoria;
        this.estado = estado;
        this.creadaEn = ahora;
        this.actualizadaEn = ahora;
    }

    public HistorialSolicitud agregarHistorial(EstadoSolicitud estadoRegistrado, long fecha, String nota) {
        HistorialSolicitud registro = new HistorialSolicitud(this, estadoRegistrado, fecha, nota);
        historial.add(registro);
        return registro;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
    public long getCreadaEn() { return creadaEn; }
    public long getActualizadaEn() { return actualizadaEn; }
    public void setActualizadaEn(long actualizadaEn) { this.actualizadaEn = actualizadaEn; }
    public List<HistorialSolicitud> getHistorial() { return historial; }
}
