package cargobicycle.platform;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@NoArgsConstructor
@Data
public class Booking {
    private int customerId;
    private int customerName;
    private int providerId;
    private int providerName;
    private int bicycleId;
    private int bicycleDescription;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private OffsetDateTime fromDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private OffsetDateTime toDate;
}
