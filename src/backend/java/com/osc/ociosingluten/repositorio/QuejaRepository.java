package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface QuejaRepository extends JpaRepository<Consulta, Integer> {

    @Transactional
    Consulta save(Consulta img);

    @Transactional
    Consulta findByEmail(Consulta img);

    @Transactional
    void deleteByIdQueja(int iD);
}