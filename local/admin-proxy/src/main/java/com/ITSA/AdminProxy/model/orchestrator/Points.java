package com.ITSA.AdminProxy.model.orchestrator;



import java.time.Instant;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

 @Setter
 @Getter
@AllArgsConstructor
public class Points {
    public Points() {
    }
    private String userId;
    private String pointsId;
    private Integer points;
    private String bank;
    private Instant createdAt;
    private Instant updatedAt;

  

    

   
}
