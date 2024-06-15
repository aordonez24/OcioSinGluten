package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Imagen;
import com.osc.ociosingluten.modelo.Queja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface QuejaRepository extends JpaRepository<Queja, Integer> {

    @Transactional
    Queja save(Queja img);

    @Transactional
    Queja findByEmail(Queja img);

    @Transactional
    void deleteByIdQueja(int iD);
}