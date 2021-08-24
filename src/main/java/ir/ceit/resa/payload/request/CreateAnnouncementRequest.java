package ir.ceit.resa.payload.request;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

public class CreateAnnouncementRequest {

    @JsonProperty("timestamp")
    private Date creationDate;

    @NotBlank
    @JsonProperty("message")
    private String message;

    @NotBlank
    @Size(max = 120)
    @JsonProperty("writer")
    private String writer;

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

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }
}
