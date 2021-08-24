package ir.ceit.resa.payload.request;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreateBoardRequest {

    @NotBlank
    @Size(max = 40)
    @JsonProperty("boardId")
    private String boardId;

    @NotBlank
    @JsonProperty("description")
    private String description;

    @NotBlank
    @Size(max = 120)
    @JsonProperty("category")
    private String category;

    @Size(max = 100)
    @JsonProperty("faculty")
    private String faculty;


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

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }
}
