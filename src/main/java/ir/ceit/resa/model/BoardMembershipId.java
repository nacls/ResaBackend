package ir.ceit.resa.model;


import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BoardMembershipId implements Serializable {

    private User user;
    private Board board;

    @ManyToOne(cascade = CascadeType.ALL)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
            return true;

        if(obj == null || obj.getClass()!= this.getClass())
            return false;

        BoardMembershipId boardMembership = (BoardMembershipId) obj;

        return (boardMembership.user == this.user && boardMembership.board == this.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, board);
    }
}
