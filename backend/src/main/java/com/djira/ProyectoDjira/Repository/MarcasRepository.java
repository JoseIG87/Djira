package com.djira.ProyectoDjira.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.djira.ProyectoDjira.Domain.Marcas;

@Repository
public interface MarcasRepository extends MongoRepository<Marcas, String> {
	
	@Query("{ 'nombre' : ?0,  'tipo' : ?1}")
	Marcas findByNombreAndTipo(String nombre, String tipo);
	
	@Query("{ 'tipo' : ?0 }")
	List<Marcas> findByTipo(String tipo);
	
}
