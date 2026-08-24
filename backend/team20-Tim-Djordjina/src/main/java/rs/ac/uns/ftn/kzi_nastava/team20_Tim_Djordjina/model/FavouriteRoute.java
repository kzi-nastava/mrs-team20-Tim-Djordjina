package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "favourite_routes")
@Getter
@Setter
public class FavouriteRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String label;

    @Column(nullable = false)
    private String pickupAddress;
    @Column(nullable = false)
    private Double pickupLatitude;
    @Column(nullable = false)
    private Double pickupLongitude;

    @Column(nullable = false)
    private String destinationAddress;
    @Column(nullable = false)
    private Double destinationLatitude;
    @Column(nullable = false)
    private Double destinationLongitude;

    @OneToMany(mappedBy = "favouriteRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder ASC")
    private List<FavouriteRouteStop> stops = new ArrayList<>();

    public void addStop(FavouriteRouteStop stop) {
        stop.setFavouriteRoute(this);
        stops.add(stop);
    }


}
