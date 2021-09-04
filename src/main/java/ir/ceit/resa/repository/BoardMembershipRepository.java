package ir.ceit.resa.repository;

import ir.ceit.resa.model.BoardMembership;
import ir.ceit.resa.model.BoardMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import javax.transaction.Transactional;

public interface BoardMembershipRepository extends JpaRepository<BoardMembership, BoardMembershipId> {
    @Modifying
    @Transactional
    void delete(BoardMembership boardMembership);
}
