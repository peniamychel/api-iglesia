package com.mcmm.model.payload;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
//@NoArgsConstructor
@Builder
public class ApiResponseException {
    @Builder.Default
    private boolean success = false;
    @Builder.Default
    private Date timestamp = new Date();

    private Object message;
    private String url;

    public String getUrl() {
        return url != null ? url.replace("uri=", "") : null;
    }
}
