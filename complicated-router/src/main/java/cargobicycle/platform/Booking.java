package cargobicycle.platform;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Getter
@Builder
public class Booking {
    private final int customerId;
    private final int providerId;
    private final int bicycleId;
    private final OffsetDateTime fromDate;
    private final OffsetDateTime toDate;
}
