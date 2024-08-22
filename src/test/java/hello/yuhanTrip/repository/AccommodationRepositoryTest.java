package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Room;
import hello.yuhanTrip.domain.Reservation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
public class AccommodationRepositoryTest {

    @Autowired
    private AccommodationRepository accommodationRepository;

    @BeforeEach
    void setUp() {
        // Accommodation, Room 및 Reservation 데이터 설정
        Accommodation accommodation = new Accommodation();
        accommodation.setAddr1("123 Street");
        accommodation.setAreacode("123");

        Room room1 = new Room();
        room1.setRoomNo("101");
        room1.setMaxOccupancy(2);
        room1.setAccommodation(accommodation);

        Room room2 = new Room();
        room2.setRoomNo("102");
        room2.setMaxOccupancy(4);
        room2.setAccommodation(accommodation);

        Reservation reservation = new Reservation();
        reservation.setRoom(room1);
        reservation.setCheckInDate(LocalDate.now().minusDays(1));
        reservation.setCheckOutDate(LocalDate.now().plusDays(1));

        accommodation.setRooms(Arrays.asList(room1, room2));

        accommodationRepository.save(accommodation);
    }

    @Test
    void testFindByAreacode() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Accommodation> accommodations = accommodationRepository.findByAreacode("123", pageRequest);

        assertThat(accommodations).isNotEmpty();
        assertThat(accommodations.getContent().get(0).getAreacode()).isEqualTo("123");
    }


}
