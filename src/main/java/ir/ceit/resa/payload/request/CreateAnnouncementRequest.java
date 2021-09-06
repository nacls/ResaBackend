package ir.ceit.resa.payload.request;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import java.util.Date;

public class CreateAnnouncementRequest {

    @JsonProperty("timestamp")
    private Date creationDate;

    @NotBlank
    @JsonProperty("message")
    private String message;

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
