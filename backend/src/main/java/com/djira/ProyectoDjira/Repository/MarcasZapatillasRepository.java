package com.djira.ProyectoDjira.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.djira.ProyectoDjira.Domain.MarcasZapatillas;

public interface MarcasZapatillasRepository extends MongoRepository<MarcasZapatillas, String> {
	
	
	@Query("{ 'nombre' : ?0 }")
	MarcasZapatillas findByNombre(String nombre);
}
