package cargobicycle.platform;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Getter
@Setter
@Builder
public class Booking {
    private final int customerId;
    private final int customerName;
    private final int providerId;
    private final int providerName;
    private final int bicycleId;
    private final int bicycleDescription;
    private final OffsetDateTime fromDate;
    private final OffsetDateTime toDate;
}
