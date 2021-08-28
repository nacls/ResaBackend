package ir.ceit.resa.repository;

import ir.ceit.resa.model.BoardMembership;
import ir.ceit.resa.model.BoardMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardMembershipRepository extends JpaRepository<BoardMembership, BoardMembershipId> {


}
