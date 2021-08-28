package ir.ceit.resa.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "boards",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "board_name")})
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @NotBlank
    @Size(max = 40)
    @Column(name = "board_name")
    @JsonProperty("board_name")
    private String boardId;

    @NotBlank
    @JsonProperty("description")
    private String description;

    @NotBlank
    @Size(max = 120)
    @JsonProperty("category")
    private String category;

    @NotBlank
    @Size(max = 20)
    @JsonProperty("creator_username")
    private String creatorUsername;

    @Size(max = 100)
    @JsonProperty("faculty")
    private String faculty;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Announcement> announcements;

    @OneToMany(mappedBy = "primaryKey.board",
            cascade = CascadeType.ALL)
    private Set<BoardMembership> boardMemberships = new HashSet<BoardMembership>();



    public Board() {
    }

    public Board(String boardId, String description,
                 String category, String creatorUsername,
                 String faculty) {
        this.boardId = boardId;
        this.description = description;
        this.category = category;
        this.creatorUsername = creatorUsername;
        this.faculty = faculty;
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

    public Set<Announcement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(Set<Announcement> announcements) {
        this.announcements = announcements;
    }


    public Set<BoardMembership> getBoardMemberships() {
        return boardMemberships;
    }

    public void setBoardMemberships(Set<BoardMembership> boardMemberships) {
        this.boardMemberships = boardMemberships;
    }

    public void addBoardMembership(BoardMembership boardMembership) {
        this.boardMemberships.add(boardMembership);
    }

    public void deleteBoardMembership(BoardMembership boardMembership) {
        this.boardMemberships.remove(boardMembership);
    }
}
