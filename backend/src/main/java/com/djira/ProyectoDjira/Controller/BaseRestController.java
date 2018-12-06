package com.djira.ProyectoDjira.Controller;

import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.djira.ProyectoDjira.Dto.ResponseMessageDTO;
import com.djira.ProyectoDjira.Service.common.exception.APIException;
import com.djira.ProyectoDjira.Service.common.exception.ServiceException;

/**
 * Controlador base. Contiene el manejo de expceciones controladas y no
 * controladas
 * 
 * 
 *
 */
@RestController
public class BaseRestController {

	static Logger log = Logger.getLogger(BaseRestController.class);

	public final static String API_BASE_URL = "api";
	public final static String OK = "Ok";
	public final static String ERROR = "Error";

	private @Autowired HttpServletRequest request;

	
	@ExceptionHandler(APIException.class)
	public ResponseEntity<ResponseMessageDTO> serviceHandler(APIException ex) {
		log.error("ServiceException " + ex.getMessage());
		ResponseMessageDTO messageDTO = new ResponseMessageDTO(HttpStatus.BAD_REQUEST.name(), ex.getMessage());
		return new ResponseEntity<ResponseMessageDTO>(messageDTO, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ResponseMessageDTO> serviceHandler(ServiceException ex) {
		log.error("ServiceException " + ex.getMessage());
		ResponseEntity<ResponseMessageDTO> respuesta = new ResponseEntity<ResponseMessageDTO>(HttpStatus.BAD_REQUEST);
		ResponseMessageDTO messageDTO = new ResponseMessageDTO(HttpStatus.BAD_REQUEST.name(), ex.getMessage());
		return new ResponseEntity<ResponseMessageDTO>(messageDTO, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ResponseMessageDTO> nullPointerHandler(NullPointerException ex) {
		log.error("NullPointerException " + ex.getMessage());
		ResponseMessageDTO messageDTO = new ResponseMessageDTO(HttpStatus.INTERNAL_SERVER_ERROR.name(),
				ex.getMessage());
		return new ResponseEntity<ResponseMessageDTO>(messageDTO, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(NumberFormatException.class)
	public ResponseEntity<ResponseMessageDTO> numberFormatHandler(NumberFormatException ex) {
		log.error("NumberFormatException " + ex.getMessage());
		ResponseMessageDTO messageDTO = new ResponseMessageDTO(HttpStatus.INTERNAL_SERVER_ERROR.name(),
				ex.getMessage());
		return new ResponseEntity<ResponseMessageDTO>(messageDTO, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseMessageDTO> serviceHandler(Exception ex) {
		log.error("Exception " + ex.getMessage());
		ex.printStackTrace();

		ResponseMessageDTO messageDTO = new ResponseMessageDTO(HttpStatus.INTERNAL_SERVER_ERROR.name(),
				ex.getMessage());
		return new ResponseEntity<ResponseMessageDTO>(messageDTO, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(SecurityException.class)
	public ResponseEntity<ResponseMessageDTO> serviceHandler(SecurityException ex) {
		ResponseMessageDTO messageDTO = new ResponseMessageDTO(HttpStatus.FORBIDDEN.name(), ex.getMessage());
		return new ResponseEntity<ResponseMessageDTO>(messageDTO, HttpStatus.FORBIDDEN);
	}
	
}
