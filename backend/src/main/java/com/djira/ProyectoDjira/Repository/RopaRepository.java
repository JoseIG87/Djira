package com.djira.ProyectoDjira.Repository;

import com.djira.ProyectoDjira.Domain.Ropa;
import com.djira.ProyectoDjira.Dto.MarcasDTO;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RopaRepository extends MongoRepository<Ropa, String>{
	
	@Query("{ 'tipo' : ?0 }")
	List<Ropa> findByTipo(String tipo);
	
	Page<Ropa> findByTipo(@Param("tipo") String tipo, Pageable pageable);
	
	@Query("{ 'tipo' : ?2, 'precio' : {$gt : ?0, $lt : ?1}, 'marca' : {$in : ?3}}")
	List<Ropa> findByPrecioBetweenAndTipo(BigDecimal precioMin, BigDecimal precioMax, String tipo, String[] marcas, Pageable pageable);
	
	@Query("{ 'tipo' : ?2, 'precio' : {$gt : ?0, $lt : ?1}, 'marca' : {$in : ?3}}")
	List<Ropa> findByPrecioBetweenAndTipo(BigDecimal precioMin, BigDecimal precioMax, String tipo, String[] marcas);
	
}
