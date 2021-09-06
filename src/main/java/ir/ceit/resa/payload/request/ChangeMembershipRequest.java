package ir.ceit.resa.payload.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import ir.ceit.resa.model.EMembership;

import javax.validation.constraints.NotBlank;

public class ChangeMembershipRequest {

    @NotBlank
    @JsonProperty("boardId")
    private String boardId;

    @NotBlank
    @JsonProperty("username")
    private String username;

    @JsonProperty("membership")
    private EMembership membership;

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public EMembership getMembership() {
        return membership;
    }

    public void setMembership(EMembership membership) {
        this.membership = membership;
    }
}
