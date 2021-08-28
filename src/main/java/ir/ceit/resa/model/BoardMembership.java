package ir.ceit.resa.model;

import javax.persistence.*;

@Entity
@Table(name = "users_boards")
@AssociationOverrides({
        @AssociationOverride(name = "primaryKey.user",
                joinColumns = @JoinColumn(name = "USER_ID")),
        @AssociationOverride(name = "primaryKey.board",
                joinColumns = @JoinColumn(name = "BOARD_ID")) })
public class BoardMembership {


    private BoardMembershipId primaryKey = new BoardMembershipId();

    private EMembership status;

    @EmbeddedId
    public BoardMembershipId getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(BoardMembershipId primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Transient
    public User getUser() {
        return getPrimaryKey().getUser();
    }

    public void setUser(User user) {
        getPrimaryKey().setUser(user);
    }

    @Transient
    public Board getBoard() {
        return getPrimaryKey().getBoard();
    }

    public void setBoard(Board board) {
        getPrimaryKey().setBoard(board);
    }

    public EMembership getStatus() {
        return status;
    }

    public void setStatus(EMembership status) {
        this.status = status;
    }
}
