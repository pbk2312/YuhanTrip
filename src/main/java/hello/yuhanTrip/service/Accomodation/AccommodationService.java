package hello.yuhanTrip.service.Accomodation;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.AccommodationApplyStatus;
import hello.yuhanTrip.domain.Room;
import hello.yuhanTrip.dto.AccommodationRegisterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface AccommodationService {

    Accommodation registerAccommodation(Long memberId, AccommodationRegisterDTO dto) throws IOException;

    Accommodation getAccommodationInfo(Long id);

    Room getRoomInfo(Long id);

    Page<Accommodation> getAccommodations(int page, int size);

    Page<Accommodation> getAccommodationsByAreaCode(String areaCode, int page, int size);

    Page<Accommodation> getAvailableAccommodations(String areaCode, LocalDate checkInDate, LocalDate checkOutDate, int numGuests, int page, int size, String sortBy);


    Page<Accommodation> getAvailableAccommodationsSortedByRatingAndReview(Pageable pageable);

    List<Room> getAvailableRoomsByAccommodation(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate);



    Page<Accommodation> getAllAccommodationsOrderByPriceDesc(int page, int size);

    Page<Accommodation> getAllAccommodationsOrderByPriceAsc(int page, int size);

    Page<Accommodation> searchByTitle(String title, Pageable pageable);

    Page<Accommodation> getAvailableAccommodationsSearchByTitle(String title, String areaCode,
                                                   LocalDate checkInDate, LocalDate checkOutDate, int numGuests,
                                                   String sortBy, Pageable pageable);

}
