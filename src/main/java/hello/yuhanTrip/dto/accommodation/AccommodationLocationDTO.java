package hello.yuhanTrip.dto.accommodation;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccommodationLocationDTO {

    private Long id;
    private String title;
    private double mapx;
    private double mapy;

}
