package ir.ceit.resa.payload.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import ir.ceit.resa.model.Announcement;
import ir.ceit.resa.model.EMembership;

public class BoardInfoResponse implements Comparable<BoardInfoResponse> {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("boardId")
    private String boardId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("creator_username")
    private String creatorFullName;

    @JsonProperty("faculty")
    private String faculty;

    @JsonProperty("latest_announcement")
    private Announcement latestAnnouncement;

    @JsonProperty("user_membership")
    private EMembership userMembership;


    public BoardInfoResponse(Long id, String boardId,
                             String description, String category,
                             String creatorUsername, String faculty, EMembership userMembership,
                             Announcement latestAnnouncement) {
        this.id = id;
        this.boardId = boardId;
        this.description = description;
        this.category = category;
        this.creatorFullName = creatorUsername;
        this.faculty = faculty;
        this.userMembership = userMembership;
        this.latestAnnouncement = latestAnnouncement;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getCreatorFullName() {
        return creatorFullName;
    }

    public void setCreatorFullName(String creatorFullName) {
        this.creatorFullName = creatorFullName;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public Announcement getLatestAnnouncement() {
        return latestAnnouncement;
    }

    public void setLatestAnnouncement(Announcement latestAnnouncement) {
        this.latestAnnouncement = latestAnnouncement;
    }

    public EMembership getUserMembership() {
        return userMembership;
    }

    public void setUserMembership(EMembership userMembership) {
        this.userMembership = userMembership;
    }


    @Override
    public int compareTo(BoardInfoResponse o) {
        if (getLatestAnnouncement() == null) {
            return (o.getLatestAnnouncement() == null) ? 0 : 1;
        }
        if (o.getLatestAnnouncement() == null) {
            return -1;
        }
        return getLatestAnnouncement().compareTo(o.getLatestAnnouncement());
    }
}
