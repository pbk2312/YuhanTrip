package hello.yuhanTrip.dto.accommodation;

import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.domain.accommodation.Room;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AccommodationReservationInfoDTO {

    private Accommodation accommodation;
    private Map<Room, List<Reservation>> roomReservations = new HashMap<>();

    // Getters and Setters

    public Accommodation getAccommodation() {
        return accommodation;
    }

    public void setAccommodation(Accommodation accommodation) {
        this.accommodation = accommodation;
    }

    public Map<Room, List<Reservation>> getRoomReservations() {
        return roomReservations;
    }

    public void addRoomReservation(Room room, List<Reservation> reservations) {
        this.roomReservations.put(room, reservations);
    }
}
