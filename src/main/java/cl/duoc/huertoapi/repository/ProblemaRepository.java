package cl.duoc.huertoapi.repository;

import cl.duoc.huertoapi.model.Problema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemaRepository extends JpaRepository<Problema, Long> {

    List<Problema> findByReportadoPorId(String reportadoPorId);

    long countBySectorId(Long sectorId);
}
