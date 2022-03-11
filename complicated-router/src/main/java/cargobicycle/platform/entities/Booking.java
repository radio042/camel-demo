package cargobicycle.platform.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Booking {
    private int customerId;
    private int providerId;
    private int bicycleId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private OffsetDateTime fromDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private OffsetDateTime toDate;
}
