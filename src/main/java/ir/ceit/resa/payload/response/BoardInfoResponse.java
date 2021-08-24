package ir.ceit.resa.payload.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import ir.ceit.resa.model.Announcement;
import ir.ceit.resa.model.EMembership;

public class BoardInfoResponse {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("boardId")
    private String boardId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("creator_username")
    private String creatorUsername;

    @JsonProperty("faculty")
    private String faculty;

    @JsonProperty("latest_announcement")
    private Announcement latestAnnouncement;

    @JsonProperty("user_membership")
    private EMembership userMembership;


    public BoardInfoResponse(Integer id, String boardId,
                             String description, String category,
                             String creatorUsername, String faculty) {
        this.id = id;
        this.boardId = boardId;
        this.description = description;
        this.category = category;
        this.creatorUsername = creatorUsername;
        this.faculty = faculty;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }
}
