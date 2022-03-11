package cargobicycle.platform.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BookingForProviders {
    private String customerName;
    private int providerId;
    private int bicycleId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private OffsetDateTime fromDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private OffsetDateTime toDate;
}
