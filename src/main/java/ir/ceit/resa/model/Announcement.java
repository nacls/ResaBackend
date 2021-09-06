package ir.ceit.resa.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name = "announcements")
public class Announcement implements Comparable<Announcement> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("timestamp")
    private Date creationDate;

    @NotBlank
    @JsonProperty("message")
    private String message;

    @NotBlank
    @Size(max = 120)
    @JsonProperty("writer")
    private String writer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "boardId", nullable = false)
    private Board board;


    Announcement() {

    }

    public Announcement(Date creationDate, String message, String writer, Board board) {
        this.creationDate = creationDate;
        this.message = message;
        this.writer = writer;
        this.board = board;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    @Override
    public int compareTo(Announcement o) {
        if (getCreationDate() == null) {
            return (o.getCreationDate() == null) ? 0 : 1;
        }
        if (o.getCreationDate() == null) {
            return -1;
        }

        return (getCreationDate().compareTo(o.getCreationDate()))*-1;
    }
}
