package cl.duoc.huertoapi.repository;

import cl.duoc.huertoapi.model.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CultivoRepository extends JpaRepository<Cultivo, Long> {

    List<Cultivo> findBySectorId(Long sectorId);

    long countBySectorId(Long sectorId);
}
