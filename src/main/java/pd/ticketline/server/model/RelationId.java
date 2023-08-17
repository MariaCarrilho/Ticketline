package pd.ticketline.server.model;

import jakarta.persistence.Embeddable;


import java.io.Serializable;

@Embeddable
public class RelationId implements Serializable {
    private Sit sit;
    private Reservation reservation;
}
